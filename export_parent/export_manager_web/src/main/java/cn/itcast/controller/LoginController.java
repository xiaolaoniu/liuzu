package cn.itcast.controller;


import cn.itcast.domain.system.Module;
import cn.itcast.domain.system.User;
import cn.itcast.service.system.ModuleService;
import cn.itcast.service.system.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class LoginController{

    @Autowired
    private UserService userService;

    @Autowired
    private ModuleService moduleService;
	@RequestMapping("/login")
	public String login(HttpServletRequest request, HttpSession session,String email, String password) {
//	    先根据email查询用户，判断是否能查到，如果能查到再匹配password
//	    select  * from pe_user where email=?
       /* User user = userService.findByEmail(email);
        if(user==null){
            request.setAttribute("error","用户名或密码错误");
//            重定向（浏览器重新发起了请求） 请求转发
            return "forward:/login.jsp";
        }
        String password_db = user.getPassword();  //密文
//        把明文加密和数据库做比较
        String password_page = new Md5Hash(password, user.getUserName(), 2).toString();
        if(!password_db.equals(password_page)){
            request.setAttribute("error","用户名或密码错误");
//            重定向（浏览器重新发起了请求） 请求转发
            return "forward:/login.jsp";
        }*/

       if(StringUtils.isEmpty(email)||StringUtils.isEmpty(password)){
           request.setAttribute("error","用户名或密码错误");
//            重定向（浏览器重新发起了请求） 请求转发
           return "forward:/login.jsp";
       }

//       shiro的认证3步骤：1、获取主题 2、创建令牌  3、开始认证
//        1、获取主题
        Subject subject = SecurityUtils.getSubject();
//        2、创建令牌 存有个人信息(email,加密后的密码)
        UsernamePasswordToken token = new UsernamePasswordToken(email,password);
//        3、开始认证
        try {
            subject.login(token);//AuthenticationToken
        } catch (AuthenticationException e) {
            request.setAttribute("error","用户名或密码错误");
//            重定向（浏览器重新发起了请求） 请求转发
            return "forward:/login.jsp";
        }

        User user = (User) subject.getPrincipal(); //从主题中获取主角
        //email和密码是正确的
        session.setAttribute("loginUser",user);
//        查询当前登录人的菜单
        List<Module> moduleList = moduleService.findByUser(user);
        session.setAttribute("modules",moduleList);

		return "home/main";
	}
    //退出
    @RequestMapping(value = "/logout",name="用户登出")
    public String logout(){
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "forward:login.jsp";
    }
    @RequestMapping("/home")
    public String home(){
	    return "home/home";
    }
}
