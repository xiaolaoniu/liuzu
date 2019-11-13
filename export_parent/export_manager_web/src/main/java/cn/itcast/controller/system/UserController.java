package cn.itcast.controller.system;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.system.Dept;
import cn.itcast.domain.system.Role;
import cn.itcast.domain.system.User;
import cn.itcast.service.system.DeptService;
import cn.itcast.service.system.RoleService;
import cn.itcast.service.system.UserService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/system/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DeptService deptService;
    @RequestMapping(value = "/list",name = "用户列表数据展示")
    public String findAll( @RequestParam(value = "page",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        PageInfo<User> page = userService.findPage(companyId,pageNum,pageSize);
        request.setAttribute("page",page);
        return "system/user/user-list";
    }

    @RequestMapping(value = "/toAdd",name = "进入到用户新增页面")
    public String toAdd(){
        List<Dept> deptList = deptService.findAll(companyId);
        request.setAttribute("deptList",deptList);
        return "system/user/user-add";

//        return "redirect:/user/list.do";
    }


    @RequestMapping(value = "/edit",name = "保存用户信息方法")
    public String edit(User user){
//        因为新增和修改公用了一个页面，此方法有新增和修改的功能
////                判断依据是user对象中的id是否为空 “” null
        if(StringUtils.isEmpty(user.getId())){  //id为空 表示新增
            user.setId(UUID.randomUUID().toString());
            user.setCompanyId(companyId);
            user.setCompanyName(companyName);
            user.setCreateBy(createBy);
            user.setCreateDept(createDept);
            user.setCreateTime(new Date());
            userService.save(user);
        }else{
            user.setUpdateBy(createBy);
            user.setUpdateTime(new Date());
            userService.update(user);
        }
        return "redirect:/system/user/list.do";
    }
    @RequestMapping(value = "/toUpdate",name = "进入到用户修改页面")
    public String toUpdate( String id){
       User user = userService.findById(id);
        request.setAttribute("user",user);

        List<Dept> deptList = deptService.findAll(companyId);
        request.setAttribute("deptList",deptList);

        return "system/user/user-add";
    }

    @RequestMapping(value = "/delete",name = "删除用户信息方法")
    public String delete(String id){
        userService.deleteById(id);
        return "redirect:/system/user/list.do";
    }

    @RequestMapping(value = "/roleList",name = "进入用户分配角色页面")
    public String roleList(String id){
        User user = userService.findById(id);
        request.setAttribute("user",user);
//        查询所有角色
        List<Role> roleList = roleService.findAll(companyId);
        request.setAttribute("roleList",roleList);
//        userRoleStr 此用户所拥有的角色的id字符串
        StringBuffer userRoleStr = new StringBuffer("");
        List<Role> userRoleList = roleService.findByUserId(id); //根据用户id查询角色
        for (Role role : userRoleList) {
            userRoleStr.append(role.getId()).append(",");
        }
        request.setAttribute("userRoleStr",userRoleStr.toString());

        return "system/user/user-role";
    }
    @RequestMapping(value = "/changeRole",name = "给用户分配角色方法")
    public String changeRole(String userid,String roleIds){ //roleIds=132345345,4656576
      userService.changeRole(userid,roleIds);
        return "redirect:/system/user/list.do";
    }

}
