package cn.itcast.controller.system;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.system.Dept;
import cn.itcast.service.system.DeptService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/system/dept")
public class DeptController extends BaseController {

    @Autowired
    private DeptService deptService;
    @RequestMapping(value = "/list",name = "部门列表数据展示")
    public String findAll( @RequestParam(value = "page",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        PageInfo<Dept> page = deptService.findPage(companyId,pageNum,pageSize);
        request.setAttribute("page",page);

        return "system/dept/dept-list";
    }

    @RequestMapping(value = "/toAdd",name = "进入到部门新增页面")
    @RequiresPermissions("新增部门")
    public String toAdd(){
        List<Dept> deptList = deptService.findAll(companyId);
        request.setAttribute("deptList",deptList);
        return "system/dept/dept-add";

//        return "redirect:/dept/list.do";
    }


    @RequestMapping(value = "/edit",name = "保存部门信息方法")
    public String edit(Dept dept){
//        因为新增和修改公用了一个页面，此方法有新增和修改的功能
////                判断依据是dept对象中的id是否为空 “” null
        if(dept.getParent().getId().equals("")){
            dept.getParent().setId(null);
        }
        if(StringUtils.isEmpty(dept.getId())){  //id为空 表示新增
            dept.setId(UUID.randomUUID().toString());
            dept.setCompanyId(companyId);
            dept.setCompanyName(companyName);
            deptService.save(dept);
        }else{
            deptService.update(dept);

        }
        return "redirect:/system/dept/list.do";
    }
    @RequestMapping(value = "/toUpdate",name = "进入到部门修改页面")
    public String toUpdate( String id){
       Dept dept = deptService.findById(id);
        request.setAttribute("dept",dept);

        List<Dept> deptList = deptService.findAll(companyId);
        request.setAttribute("deptList",deptList);

        return "system/dept/dept-add";
    }

    @RequestMapping(value = "/delete",name = "删除部门信息方法")
    public String delete(String id){
        deptService.deleteById(id);
        return "redirect:/system/dept/list.do";
    }

}
