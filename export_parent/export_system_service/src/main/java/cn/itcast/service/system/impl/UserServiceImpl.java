package cn.itcast.service.system.impl;

import cn.itcast.dao.system.UserDao;
import cn.itcast.domain.system.User;
import cn.itcast.service.system.UserService;
import cn.itcast.utils.MailUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Override
    public List<User> findAll(String companyId) {
        return userDao.findAll(companyId);
    }

    @Override
    public void save(User user) {
        String password1 = user.getPassword(); //明文
        String password = user.getPassword(); //明文

        password = new Md5Hash(password,user.getUserName(),2).toString(); //可以加盐  ,加几次

        user.setPassword(password); //密文
        userDao.insert(user);

//        发送邮件
        try {
            MailUtil.sendMsg(user.getEmail(),"恭喜您成员SaaS货代云平台的用户","恭喜您成员SaaS货代云平台的用户，您的登录密码是："+password1);
        } catch (Exception e) {
//            e.printStackTrace();
        }
//把发送邮件的任务放入到MQ中
//        String to =user.getEmail();
//        String subject ="恭喜您成员SaaS货代云平台的用户";
//        String content ="恭喜您成员SaaS货代云平台的用户，您的登录密码是："+password1;
        Map<String,String> map = new HashMap();
        map.put("to",user.getEmail());
        map.put("subject","恭喜您成员SaaS货代云平台的用户");
        map.put("content","恭喜您成员SaaS货代云平台的用户，您的登录密码是："+password1);
        amqpTemplate.convertAndSend("user.insert.email",map);
    }

    @Override
    public User findById(String id) {
        return userDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(User user) {
        userDao.updateByPrimaryKey(user);
    }

    @Override
    public void deleteById(String id) {
        userDao.deleteByPrimaryKey(id);
    }

    /*@Override
    public PageBean findPage(int pageNum, int pageSize) {
        Long total = userDao.findCount();//select count(id) from ss_user
//        起始位置有一个计算公式 （当前页码-1）*每页显示条数
        List<User> list = userDao.findPage((pageNum-1)*pageSize,pageSize);//select * from ss_user limit 其实位置，显示的条数pageSize
        return new PageBean(pageNum,pageSize,total,list);
    }*/
    @Override
    public PageInfo findPage(String companyId,int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize); //为了保证线程安全，PageHelper.startPage方法一定要紧跟一个查询方法
//        List<User> list = userDao.findAll();
        Page list = (Page) userDao.findAll(companyId);
        PageInfo pageInfo = new PageInfo(list,5);
        return pageInfo;
    }

    @Override
    public void changeRole(String userid, String roleIds) { //roleIds 246643,574543
//        先删除此用户之前的角色
//        delete from pe_role_user where user_id=?
        userDao.deleteUserAndRoleByUserId(userid);
//      然后在重新添加数据到中间表  pe_role_user
//        insert into pe_role_user (user_id,role_id) values (userid,roleid)
        String[] rIds = roleIds.split(",");
        for (String rId : rIds) {
            userDao.insertUserAndRole(userid,rId);
        }
    }

    @Override
    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public static void main(String[] args) {
        String xiaowang = new Md5Hash("123456", "admin", 2).toString();
        System.out.println(xiaowang); //69e36cdd747d5c0d89dda29b236cf4f1
//        https://www.cmd5.com/  TODO 2019年10月24日说5天后
    }
}
