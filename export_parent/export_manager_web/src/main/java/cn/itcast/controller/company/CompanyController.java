package cn.itcast.controller.company;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.company.Company;
import cn.itcast.service.company.CompanyService;
import cn.itcast.vo.PageBean;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/company")
public class CompanyController extends BaseController {

    @Reference
    private CompanyService companyService;
    @RequestMapping(value = "/list",name = "企业列表数据展示")
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

    @RequestMapping(value = "/toAdd",name = "进入到企业新增页面")
    public String toAdd(){
        return "company/company-add";

//        return "redirect:/company/list.do";
    }


    @RequestMapping(value = "/edit",name = "保存企业信息方法")
    public String edit(Company company){
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
    @RequestMapping(value = "/toUpdate",name = "进入到企业修改页面")
    public String toUpdate( String id){
       Company company = companyService.findById(id);
        request.setAttribute("company",company);
        return "company/company-add";
    }

    @RequestMapping(value = "/delete/{id}",name = "删除企业信息方法")
    public String delete(@PathVariable("id") String id){
        companyService.deleteById(id);
        return "redirect:/company/list.do";
    }

}
