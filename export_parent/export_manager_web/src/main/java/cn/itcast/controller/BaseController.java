package cn.itcast.controller;

import cn.itcast.domain.system.User;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BaseController {

    protected HttpServletRequest request;

    protected HttpServletResponse response;

    protected HttpSession session;

    protected String  companyId;
    protected String  companyName;

    protected String createBy;
    protected String createDept;

    protected User user;

    @ModelAttribute //在执行Controller中的任何方法时 都会先执行此方法
    public void initRequestAndResponseAndSession(HttpServletRequest request,HttpServletResponse response,HttpSession session){
        System.out.println("initRequestAndResponseAndSession");
        this.request=request;
        this.response=response;
        this.session=session;
        user = (User) session.getAttribute("loginUser");
        this.companyId=user.getCompanyId();
        this.companyName=user.getCompanyName();
        this.createBy=user.getId();
        this.createDept=user.getDeptId();
    }

}
