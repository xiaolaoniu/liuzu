package cn.itcast.controller.cargo;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.cargo.ContractExample;
import cn.itcast.domain.cargo.Export;
import cn.itcast.domain.cargo.ExportExample;
import cn.itcast.domain.cargo.ExportProduct;
import cn.itcast.service.cargo.ContractService;
import cn.itcast.service.cargo.ExportProductService;
import cn.itcast.service.cargo.ExportService;
import cn.itcast.utils.BeanMapUtils;
import cn.itcast.utils.DownloadUtil;
import cn.itcast.vo.ExportProductVo;
import cn.itcast.vo.ExportResult;
import cn.itcast.vo.ExportVo;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Controller
@RequestMapping("/cargo/export")
public class ExportController extends BaseController {

    @Reference
    private ContractService contractService;
    @Reference
    private ExportService exportService;
    @Reference
    private ExportProductService exportProductService;

    @RequestMapping(value = "/contractList",name = "展示待报运的购销合同列表")//select * from co_contract where  company_id=? state=1 order by create_time
    public String contractList(@RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "pageSize", defaultValue = "10") int size){
        ContractExample example = new ContractExample();
        example.createCriteria().andCompanyIdEqualTo(companyId).andStateEqualTo(1);
        example.setOrderByClause("create_time desc");
        PageInfo pageInfo = contractService.findAll(example, page, size);
        request.setAttribute("page",pageInfo);
        return "cargo/export/export-contractList";
    }


    @RequestMapping(value = "/list",name = "报运单列表数据")//select * from co_contract where  company_id=? state=1 order by create_time
    public String list(@RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "pageSize", defaultValue = "10") int size){
        ExportExample example = new ExportExample();
        example.createCriteria().andCompanyIdEqualTo(companyId);
        example.setOrderByClause("create_time desc");
        PageInfo pageInfo = exportService.findAll(example, page, size);
        request.setAttribute("page",pageInfo);
        return "cargo/export/export-list";
    }


    @RequestMapping(value = "/toExport",name = "进入生成报运单的页面")
    public String toExport(String id){
        request.setAttribute("id",id);
        return "cargo/export/export-toExport";
    }
    @RequestMapping(value = "/edit",name = "生成报运单的方法")
    public String edit(Export export){
        if(StringUtils.isEmpty(export.getId())){
            export.setId(UUID.randomUUID().toString());
            export.setState(0);//新增的报运单状态为草稿 对应的值是0
            export.setCompanyId(companyId);
            export.setCompanyName(companyName);
            export.setCreateBy(createBy);
            export.setCreateDept(user.getDeptId());   // 在细颗粒度控制时需要此字段
            export.setCreateTime(new Date());
            export.setInputDate(new Date());
            exportService.save(export);
        }else{
            export.setUpdateBy(createBy);
            export.setUpdateTime(new Date());
            exportService.update(export);
        }

        return "redirect:/cargo/export/list.do";
    }


    @RequestMapping(value = "/toUpdate",name = "进入修改报运单的页面")
    public String toUpdate(String id){
//        页面上需要的数据：1、报运单对象 2、报运单下货物列表list
        Export export = exportService.findById(id);
        request.setAttribute("export",export);

        List<ExportProduct> exportProducts = exportProductService.findByExportId(id);

        request.setAttribute("eps",exportProducts);
        return "cargo/export/export-update";
    }


    @RequestMapping(value = "/exportE",name = "海关电子报运")
    public String exportE(String id){
        Export export = exportService.findById(id);
        ExportVo exportVo = new ExportVo();
        BeanUtils.copyProperties(export,exportVo);
//        此时的exportVo还少两个很关键的属性 exportId List<ExportProductVo> products
        exportVo.setExportId(id);
//        根据报运单id查询报运单货物
        List<ExportProduct> exportProductList = exportProductService.findByExportId(id);
        for (ExportProduct ep : exportProductList) {
            ExportProductVo epVo = new ExportProductVo();
            BeanUtils.copyProperties(ep,epVo);
//            epVo上两个属性  eid;  exportProductId;
            epVo.setExportProductId(ep.getId());
            epVo.setEid(exportVo.getId());
            exportVo.getProducts().add(epVo);
        }
        //        ip:port/ws/export/ep/ 提交数据
//       ip:port/ws/export/ep/{id} 获取结果
//        调用海关的两个方法
        WebClient.create("http://127.0.0.1:9090/ws/export/ep/").type(MediaType.APPLICATION_XML).post(exportVo);  //提交数据
//        获取数据
        ExportResult exportResult = WebClient.create("http://127.0.0.1:9090/ws/export/ep/" + id).accept(MediaType.APPLICATION_XML).get(ExportResult.class);
        exportService.updateE(exportResult);//从海关获取数据后修改报运单和报运单货物数据

        return "redirect:/cargo/export/list.do";
    }

    @Autowired
    private DownloadUtil downloadUtil;

    @RequestMapping("/exportPdf")
    public void exportPdf(String id) throws Exception{
//        第一步：读取模板
        String templatePath = session.getServletContext().getRealPath("/make/pdf/export.jasper");
        InputStream inputStream = new FileInputStream(templatePath);
//      第二步：准备数据 包含两种数据 1、报运单  2、此报运单下的货物数据
        Export export = exportService.findById(id);//1、报运单
        List<ExportProduct> exportProductList = exportProductService.findByExportId(id);//2、此报运单下的货物数据

        Map parameter = BeanMapUtils.beanToMap(export); //把export对象转成map

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(exportProductList);//构建pdf导出需要的集合数据
        JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parameter, dataSource);//模板和数据结合
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();//准备一个字节数组的缓存流
        byte[] bytes = JasperExportManager.exportReportToPdf(jasperPrint); //把文档转成字节
        byteArrayOutputStream.write(bytes);  //把字节写入流中
        downloadUtil.download(byteArrayOutputStream,response,"报运单.pdf");//导出

    }

}
