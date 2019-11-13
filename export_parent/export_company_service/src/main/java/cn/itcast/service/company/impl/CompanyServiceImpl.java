package cn.itcast.service.company.impl;

import cn.itcast.dao.company.CompanyDao;
import cn.itcast.domain.company.Company;
import cn.itcast.service.company.CompanyService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {
    @Autowired
    private CompanyDao companyDao;
    @Override
    public List<Company> findAll() {
        return companyDao.findAll();
    }

    @Override
    public void save(Company company) {
        companyDao.insert(company);
    }

    @Override
    public Company findById(String id) {
        return companyDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Company company) {
        companyDao.updateByPrimaryKey(company);
    }

    @Override
    public void deleteById(String id) {
        companyDao.deleteByPrimaryKey(id);
    }

    /*@Override
    public PageBean findPage(int pageNum, int pageSize) {
        Long total = companyDao.findCount();//select count(id) from ss_company
//        起始位置有一个计算公式 （当前页码-1）*每页显示条数
        List<Company> list = companyDao.findPage((pageNum-1)*pageSize,pageSize);//select * from ss_company limit 其实位置，显示的条数pageSize
        return new PageBean(pageNum,pageSize,total,list);
    }*/
    @Override
    public PageInfo findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize); //为了保证线程安全，PageHelper.startPage方法一定要紧跟一个查询方法
//        List<Company> list = companyDao.findAll();
        Page list = (Page) companyDao.findAll();
        PageInfo pageInfo = new PageInfo(list,5);
        return pageInfo;
    }
}
