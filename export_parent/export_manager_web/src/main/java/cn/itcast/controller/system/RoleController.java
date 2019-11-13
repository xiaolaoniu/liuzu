package cn.itcast.controller.system;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.system.Module;
import cn.itcast.domain.system.Role;
import cn.itcast.service.system.DeptService;
import cn.itcast.service.system.ModuleService;
import cn.itcast.service.system.RoleService;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import javafx.beans.property.MapProperty;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/system/role")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private ModuleService moduleService;

    @Autowired
    private DeptService deptService;
    @RequestMapping(value = "/list",name = "角色列表数据展示")
    public String findAll( @RequestParam(value = "page",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        PageInfo<Role> page = roleService.findPage(companyId,pageNum,pageSize);
        request.setAttribute("page",page);
        return "system/role/role-list";
    }

    @RequestMapping(value = "/toAdd",name = "进入到角色新增页面")
    public String toAdd(){

        return "system/role/role-add";
//        return "redirect:/role/list.do";
    }


    @RequestMapping(value = "/edit",name = "保存角色信息方法")
    public String edit(Role role){
//        因为新增和修改公用了一个页面，此方法有新增和修改的功能
////                判断依据是role对象中的id是否为空 “” null
        if(StringUtils.isEmpty(role.getId())){  //id为空 表示新增
            role.setId(UUID.randomUUID().toString());
            role.setCompanyId(companyId);
            role.setCompanyName(companyName);
            role.setCreateBy(createBy);
            role.setCreateDept(createDept);
            role.setCreateTime(new Date());
            roleService.save(role);
        }else{
            role.setUpdateBy(createBy);
            role.setUpdateTime(new Date());
            roleService.update(role);
        }
        return "redirect:/system/role/list.do";
    }
    @RequestMapping(value = "/toUpdate",name = "进入到角色修改页面")
    public String toUpdate( String id){
       Role role = roleService.findById(id);
        request.setAttribute("role",role);

        return "system/role/role-add";
    }

    @RequestMapping(value = "/delete",name = "删除角色信息方法")
    public String delete(String id){
        roleService.deleteById(id);
        return "redirect:/system/role/list.do";
    }

    @RequestMapping(value = "/roleModule",name = "进入角色分配权限页面")
    public String roleModule(String roleid){
        Role role = roleService.findById(roleid);
        request.setAttribute("role",role);

        List<Map> ztreeNodes = this.getZtreeNodes(roleid);
        String jsonString = JSON.toJSONString(ztreeNodes);
        request.setAttribute("zNodes",jsonString);

        return "system/role/role-module";
    }
//    @RequestMapping(value = "/getZtreeNodes",name = "加载ztree需要的权限数据")
//    @ResponseBody //转json并且返回
    public  List<Map> getZtreeNodes(String roleid){

//        查询此角色拥有的菜单限数据
        List<Module> roleModuleList = moduleService.findByRoleId(roleid);

//        查询所有的模块数据：
        List<Module> moduleListAll = moduleService.findAll();
        List<Map> moduleZtreeList = new ArrayList<>();
//        构建ztree需要的数据[{id:1,pId:0,name;""},{}]
        for (Module module : moduleListAll) {
            Map map = new HashMap();
            map.put("id", module.getId());
            map.put("pId", module.getParentId());
            map.put("name", module.getName());
            if(roleModuleList.contains(module)){  //此时的包含判断的是对象的地址，我们想要的是id是否相同就行，改写对象中的equals方法
                map.put("checked", true);  // 勾选此角色之前的权限
            }
            moduleZtreeList.add(map);
        }
        return moduleZtreeList;

    }



    @RequestMapping(value = "/updateRoleModule",name = "保存角色分配权限的数据")
    public String updateRoleModule(String roleid,String moduleIds ){  //moduleIds=1,3,6,7,8

        roleService.updateRoleModule(roleid,moduleIds);

        return "redirect:/system/role/list.do"; //保存后跳转到角色的列表页面
    }

}
