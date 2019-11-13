package cn.itcast.controller.log;

import cn.itcast.domain.system.SysLog;
import cn.itcast.domain.system.User;
import cn.itcast.service.system.SysLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.UUID;

@Component
@Aspect
public class LogAspect {

    @Autowired
    private SysLogService sysLogService;
    
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;

    @Around("execution(* cn.itcast.controller.*.*(..))")
    public Object insertLog(ProceedingJoinPoint pjp) throws Throwable{

        User user = (User) session.getAttribute("loginUser");
        if(user!=null){
            SysLog sysLog = new SysLog();
            sysLog.setId(UUID.randomUUID().toString());     //UUID
            sysLog.setTime(new Date());  //当前时间
            String remoteAddr = request.getRemoteAddr();//获取访问人的ip地址
            sysLog.setIp(remoteAddr);    //登录人ip、
            sysLog.setCompanyId(user.getCompanyId()); //登录人
            sysLog.setCompanyName(user.getCompanyName()); //登录人
            sysLog.setUserName(user.getUserName()); //用户名

            //获取方法签名 = 方法+方法上注解
            MethodSignature signature = (MethodSignature)pjp.getSignature();
            String name = signature.getMethod().getName();
            sysLog.setMethod(name); //方法名

    //        判断方法上是否有RequestMapping注解
            boolean isPresent = signature.getMethod().isAnnotationPresent(RequestMapping.class);
            if(isPresent){
                RequestMapping requestMapping = signature.getMethod().getAnnotation(RequestMapping.class);
                String action = requestMapping.name();
                sysLog.setAction(action); //操作 方法上注解的name值
            }
            sysLogService.save(sysLog);
        }
        return pjp.proceed();//执行原来的方法
    }

}
