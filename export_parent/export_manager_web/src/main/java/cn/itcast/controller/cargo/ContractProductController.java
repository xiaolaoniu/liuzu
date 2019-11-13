package cn.itcast.controller.cargo;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.cargo.*;
import cn.itcast.service.cargo.ContractProductService;
import cn.itcast.service.cargo.ContractService;
import cn.itcast.service.cargo.FactoryService;
import cn.itcast.utils.FileUploadUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cargo/contractProduct")
public class ContractProductController extends BaseController {

    @Reference
    private ContractService contractService;

    @Reference
    private FactoryService factoryService;

    @Reference
    private ContractProductService contractProductService;

    @RequestMapping(value = "/list",name = "显示购销合同货物列表数据")
    public String list(String contractId,@RequestParam(value = "page",defaultValue = "1") int page, @RequestParam(value = "pageSize",defaultValue = "10")int size){
//        查询生成货物的工厂
//        select * from co_factory where ctype='货物'
        FactoryExample example = new FactoryExample();
        example.createCriteria().andCtypeEqualTo("货物");
        List<Factory> factoryList = factoryService.findAll(example);
        request.setAttribute("factoryList",factoryList);

//        查询当前合同下的货物
//        select * from co_contract_product where contract_id=contractId
//        查询货物下的附件(在映射文件中已经查询)
        ContractProductExample example1 = new ContractProductExample();
        example1.createCriteria().andContractIdEqualTo(contractId);
        PageInfo pageInfo = contractProductService.findAll(example1, page, size);
        request.setAttribute("page",pageInfo);

        request.setAttribute("contractId",contractId); //保存货物时需要的合同id

        return "cargo/product/product-list";
    }


    @RequestMapping(value = "/toUpdate",name = "修改合同下的货物")
    public String toUpdate(String id){
//        页面上需要的数据：1、当前货物对象
        ContractProduct contractProduct = contractProductService.findById(id);
        request.setAttribute("contractProduct",contractProduct);
//        页面上需要的数据：2、生产货物的厂家
        FactoryExample example = new FactoryExample();
        example.createCriteria().andCtypeEqualTo("货物");
        List<Factory> factoryList = factoryService.findAll(example);
        request.setAttribute("factoryList",factoryList);

        return "cargo/product/product-update";
    }

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @RequestMapping(value = "/edit",name = "合同下货物的保存")
    public String edit(ContractProduct contractProduct, MultipartFile productPhoto){
        String imagePath = null;
        try {
             imagePath = fileUploadUtil.upload(productPhoto);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            imagePath="";
        }
        contractProduct.setProductImage(imagePath); //把图片的路径保存到数据库

        if(StringUtils.isEmpty(contractProduct.getId())){
            contractProduct.setId(UUID.randomUUID().toString());
            contractProduct.setCompanyId(companyId);
            contractProduct.setCompanyName(companyName);
            contractProduct.setCreateBy(createBy);
            contractProduct.setCreateTime(new Date());
            contractProductService.save(contractProduct);
        }else{
            contractProduct.setUpdateBy(createBy);
            contractProduct.setUpdateTime(new Date());
            contractProductService.update(contractProduct);
        }

        return "redirect:/cargo/contractProduct/list.do?contractId="+contractProduct.getContractId();
    }

    @RequestMapping(value = "/delete",name = "删除合同下的货物")
    public String delete(String id,String contractId){//contractId这个id只是用来重定向时使用
        contractProductService.delete(id);
        return "redirect:/cargo/contractProduct/list.do?contractId="+contractId;
    }

    @RequestMapping(value = "/toImport",name = "进入到上传货物页面")
    public String toImport(String contractId){//contractId
        request.setAttribute("contractId",contractId);
        return "cargo/product/product-import";
    }
    @RequestMapping(value = "/import",name = "上传货物方法")
    public String importXls(String contractId,MultipartFile file) throws Exception{//contractId
        InputStream inputStream = file.getInputStream(); //从file获取输入流
        Workbook workbook = new XSSFWorkbook(inputStream); //创建了有内容的工作薄
        Sheet sheet = workbook.getSheetAt(0);  //获取工作表

        int lastRowIndex = sheet.getLastRowNum(); //获取当前sheet的最后一行的索引值
        ContractProduct contractProduct = null;
        Row row = null;
        List<ContractProduct> productList = new ArrayList<>();// 用来接收每个货物对象，为了能一次性调用service的方法
        for (int i = 1; i <= lastRowIndex; i++) {
            contractProduct = new ContractProduct();
            contractProduct.setId(UUID.randomUUID().toString());
            contractProduct.setCompanyId(companyId);
            contractProduct.setCompanyName(companyName);
            contractProduct.setCreateBy(createBy);
            contractProduct.setCreateTime(new Date());

            contractProduct.setContractId(contractId); //设置货物属于哪个合同

            row = sheet.getRow(i);//获取有效
//            生产厂家	货号	数量	包装单位(PCS/SETS)	装率	箱数	单价	货物描述	要求
            String factoryName = row.getCell(1).getStringCellValue(); //生产厂家
            contractProduct.setFactoryName(factoryName);
            String productNo = row.getCell(2).getStringCellValue();//货号
            contractProduct.setProductNo(productNo);
            Double cnumber = row.getCell(3).getNumericCellValue();//数量
            contractProduct.setCnumber(cnumber.intValue());
            String packingUnit = row.getCell(4).getStringCellValue();//包装单位(PCS/SETS)
            contractProduct.setPackingUnit(packingUnit);

            Double loadingRate = row.getCell(5).getNumericCellValue();//装率
            contractProduct.setLoadingRate(loadingRate.toString());

            Double boxNum = row.getCell(6).getNumericCellValue();//箱数
            contractProduct.setBoxNum(boxNum.intValue());

            Double price = row.getCell(7).getNumericCellValue();//单价
            contractProduct.setPrice(price);

            String productDesc = row.getCell(8).getStringCellValue();//货物描述
            contractProduct.setProductDesc(productDesc);

            String productRequest = row.getCell(9).getStringCellValue();//要求
            contractProduct.setProductRequest(productRequest);

            productList.add(contractProduct);
        }
        contractProductService.saveList(productList);

        return "redirect:/cargo/contractProduct/list.do?contractId="+contractId; //重定向到货物列表页面
    }


}
