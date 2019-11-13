package cn.itcast.controller.company;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.company.Company;
import cn.itcast.service.company.CompanyService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/company")
public class CompanyController11 extends BaseController {

    @Reference
    private CompanyService companyService;
    @GetMapping(value = "/",name = "企业列表数据展示")
//    @RequiresPermissions("企业管理") //访问此方法必须有“企业管理”权限
    public String findAll( @RequestParam(value = "page",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "2")int pageSize){
        System.out.println("findAll方法");
//        List<Company> list = companyService.findAll();
//        request.setAttribute("list",list);
//        PageBean<Company> page = companyService.findPage(pageNum,pageSize);
        PageInfo<Company> page = companyService.findPage(pageNum,pageSize);
        request.setAttribute("page",page);
        return "company/company-list";
    }




    @PostMapping(value = "/",name = "保存企业信息方法")
    public String add(Company company){
//        因为新增和修改公用了一个页面，此方法有新增和修改的功能
////                判断依据是company对象中的id是否为空 “” null
        if(StringUtils.isEmpty(company.getId())){  //id为空 表示新增
            company.setId(UUID.randomUUID().toString());
            companyService.save(company);
        }else{
            companyService.update(company);
        }
        return "redirect:/company/list.do";
    }
    @PutMapping(value = "/",name = "保存企业信息方法")
    public String update(Company company){
//        因为新增和修改公用了一个页面，此方法有新增和修改的功能
////                判断依据是company对象中的id是否为空 “” null
        if(StringUtils.isEmpty(company.getId())){  //id为空 表示新增
            company.setId(UUID.randomUUID().toString());
            companyService.save(company);
        }else{
            companyService.update(company);
        }
        return "redirect:/company/list.do";
    }


    @DeleteMapping(value = "/",name = "删除企业信息方法")
    public String delete(String id){
        companyService.deleteById(id);
        return "redirect:/company/list.do";
    }

}
