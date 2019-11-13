package cn.itcast.controller.cargo;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.cargo.*;
import cn.itcast.service.cargo.ContractProductService;
import cn.itcast.service.cargo.ContractService;
import cn.itcast.service.cargo.ExtCproductService;
import cn.itcast.service.cargo.FactoryService;
import cn.itcast.utils.FileUploadUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 附件管理Controller
 */
@Controller
@RequestMapping("/cargo/extCproduct")
public class ExtCproductController extends BaseController {

    @Reference
    private ContractService contractService;

    @Reference
    private FactoryService factoryService;

    @Reference
    private ContractProductService contractProductService;
    @Reference
    private ExtCproductService extCproductService;

    @RequestMapping(value = "/list",name = "显示货物下附件列表数据")
    public String list(String contractId,String contractProductId,@RequestParam(value = "page",defaultValue = "1") int page, @RequestParam(value = "pageSize",defaultValue = "10")int size){
//        String contractId 合同id,String contractProductId 货物id
        //        查询生产附件的工厂
//        select * from co_factory where ctype='附件'
        FactoryExample example = new FactoryExample();
        example.createCriteria().andCtypeEqualTo("附件");
        List<Factory> factoryList = factoryService.findAll(example);
        request.setAttribute("factoryList",factoryList);

//        查询当前货物下的附件
//        select * from co_ext_cproduct where product_id=contractProductId
//
        ExtCproductExample example1 = new ExtCproductExample();
        example1.createCriteria().andContractProductIdEqualTo(contractProductId);
        PageInfo pageInfo = extCproductService.findAll(example1, page, size);
        request.setAttribute("page",pageInfo);

        request.setAttribute("contractId",contractId); //保存附件时需要的合同id
        request.setAttribute("contractProductId",contractProductId); //保存附件时需要的货物id

        return "cargo/extc/extc-list";
    }


    @RequestMapping(value = "/toUpdate",name = "跳转到修改货物下的附件页面")
    public String toUpdate(String id, String contractId ,String contractProductId){
//        contractId=cf87d48d-ab7f-4c55-8b8c-fb92f4b25e8b&contractProductId=faaeec10-97fe-4342-ba8e-81c98ee3ea71
//        页面上需要的数据：1、当前附件对象
        ExtCproduct extCproduct = extCproductService.findById(id);
        request.setAttribute("extCproduct",extCproduct);
//        页面上需要的数据：2、生产附件的厂家
        FactoryExample example = new FactoryExample();
        example.createCriteria().andCtypeEqualTo("附件");
        List<Factory> factoryList = factoryService.findAll(example);
        request.setAttribute("factoryList",factoryList);

        request.setAttribute("contractId",contractId); //保存附件时需要的合同id
        request.setAttribute("contractProductId",contractProductId); //保存附件时需要的货物id
        return "cargo/extc/extc-update";
    }

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @RequestMapping(value = "/edit",name = "货物下附件的保存")
    public String edit(ExtCproduct extCproduct, MultipartFile productPhoto){
        String imagePath = null;
        try {
             imagePath = fileUploadUtil.upload(productPhoto);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            imagePath="";
        }
        extCproduct.setProductImage(imagePath); //把图片的路径保存到数据库

        if(StringUtils.isEmpty(extCproduct.getId())){
            extCproduct.setId(UUID.randomUUID().toString());
            extCproduct.setCompanyId(companyId);
            extCproduct.setCompanyName(companyName);
            extCproduct.setCreateBy(createBy);
            extCproduct.setCreateTime(new Date());
            extCproductService.save(extCproduct);
        }else{
            extCproduct.setUpdateBy(createBy);
            extCproduct.setUpdateTime(new Date());
            extCproductService.update(extCproduct);
        }
//        cargo/extCproduct/list.do?contractId=cf87d48d-ab7f-4c55-8b8c-fb92f4b25e8b&contractProductId=faaeec10-97fe-4342-ba8e-81c98ee3ea71
        return "redirect:/cargo/extCproduct/list.do?contractId="+extCproduct.getContractId()+"&contractProductId="+extCproduct.getContractProductId();
    }

    @RequestMapping(value = "/delete",name = "删除货物下的附件")
    public String delete(String id,String contractId,String contractProductId){//contractId这个id只是用来重定向时使用
        extCproductService.delete(id);
        return "redirect:/cargo/extCproduct/list.do?contractId="+contractId+"&contractProductId="+contractProductId;
    }


}
