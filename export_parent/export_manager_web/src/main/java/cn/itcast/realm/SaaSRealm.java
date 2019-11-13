package cn.itcast.realm;

import cn.itcast.domain.system.Module;
import cn.itcast.domain.system.User;
import cn.itcast.service.system.ModuleService;
import cn.itcast.service.system.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SaaSRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;
    @Autowired
    private ModuleService moduleService;

    /**
     * 认证
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
//        判断email和password是否正确
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String email = token.getUsername();
        User user = userService.findByEmail(email);
        if(user!=null){
            String password = new String(token.getPassword()); //页面上传过来的明文的
            String password_page = new Md5Hash(password, user.getUserName(),2).toString();
            if(user.getPassword().equals(password_page)){
//                p1: 主角   p2: 密码  p3:当前类名
                return new SimpleAuthenticationInfo(user,password,getName());
            }
        }
        return null;  //如果一旦return null，在登录方法那里就会跑异常
    }

    /**
     * 授权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
//         在此方法中告诉shiro框架当前登录人的权限
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
//        查询当前登录人的权限（菜单）

//        Subject subject = SecurityUtils.getSubject();
//        User user = (User) subject.getPrincipal();
        User user = (User) principalCollection.getPrimaryPrincipal();
        List<Module> moduleList = moduleService.findByUser(user);
        for (Module module : moduleList) {
            info.addStringPermission(module.getName());
        }
        return info;
    }
}
