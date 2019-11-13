package cn.itcast.service.system.impl;

import cn.itcast.dao.system.RoleDao;
import cn.itcast.domain.system.Role;
import cn.itcast.service.system.RoleService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleDao roleDao;

    @Override
    public List<Role> findAll(String companyId) {
        return roleDao.findAll(companyId);
    }

    @Override
    public void save(Role role) {
        roleDao.insert(role);
    }

    @Override
    public Role findById(String id) {
        return roleDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Role role) {
        roleDao.updateByPrimaryKey(role);
    }

    @Override
    public void deleteById(String id) {
        roleDao.deleteByPrimaryKey(id);
    }

    @Override
    public PageInfo findPage(String companyId,int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize); //为了保证线程安全，PageHelper.startPage方法一定要紧跟一个查询方法
//        List<Role> list = roleDao.findAll();
        Page list = (Page) roleDao.findAll(companyId);
        PageInfo pageInfo = new PageInfo(list,5);
        return pageInfo;
    }

    @Override
    @Transactional
    public void updateRoleModule(String roleid, String moduleIds) { //moduleIds 1,3,5,6,7

//        先删除再新增
        roleDao.deleteRoleAndModuleByRoleId(roleid); //delete from pe_role_module where role_id=?

        String[] mIds = moduleIds.split(",");
        for (String mId : mIds) {
            roleDao.insertRoleAndModule(roleid,mId);
        }
//        向中间表插入数据：insert into pe_role_module(role_id,module_id) values (roleid,moduleid)
    }

    @Override
    public List<Role> findByUserId(String id) {
//        select r.* from pe_role r,pe_role_user ru
//        where r.role_id=ru.role_id
//        and ru.user_id='4a4ff02e-c69c-4ee8-b013-847f140d33be'
        return roleDao.findByUserId(id);
    }

}
