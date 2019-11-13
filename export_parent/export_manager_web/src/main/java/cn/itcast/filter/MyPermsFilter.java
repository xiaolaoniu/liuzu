package cn.itcast.filter;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class MyPermsFilter extends AuthorizationFilter {

    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {

        Subject subject = getSubject(request, response);
        String[] perms = (String[]) mappedValue;
//        数组中但凡当前登录人有其中的一个权限就放行
        if (perms != null && perms.length > 0) {
            for (String perm : perms) {
                if(subject.isPermitted(perm)){
                    return true;
                }
            }
        }
        return false;
    }
}