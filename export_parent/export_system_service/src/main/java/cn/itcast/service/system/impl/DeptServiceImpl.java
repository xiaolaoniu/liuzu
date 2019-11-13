package cn.itcast.service.system.impl;

import cn.itcast.dao.system.DeptDao;
import cn.itcast.domain.system.Dept;
import cn.itcast.service.system.DeptService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private DeptDao deptDao;
    @Override
    public List<Dept> findAll(String companyId) {
        return deptDao.findAll(companyId);
    }

    @Override
    public void save(Dept dept) {
        deptDao.insert(dept);
    }

    @Override
    public Dept findById(String id) {
        return deptDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Dept dept) {
        deptDao.updateByPrimaryKey(dept);
    }

    @Override
    public void deleteById(String id) {
        deptDao.deleteByPrimaryKey(id);
    }

    /*@Override
    public PageBean findPage(int pageNum, int pageSize) {
        Long total = deptDao.findCount();//select count(id) from ss_dept
//        起始位置有一个计算公式 （当前页码-1）*每页显示条数
        List<Dept> list = deptDao.findPage((pageNum-1)*pageSize,pageSize);//select * from ss_dept limit 其实位置，显示的条数pageSize
        return new PageBean(pageNum,pageSize,total,list);
    }*/
    @Override
    public PageInfo findPage(String companyId,int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize); //为了保证线程安全，PageHelper.startPage方法一定要紧跟一个查询方法
//        List<Dept> list = deptDao.findAll();
        Page list = (Page) deptDao.findAll(companyId);
        PageInfo pageInfo = new PageInfo(list,5);
        return pageInfo;
    }
}
