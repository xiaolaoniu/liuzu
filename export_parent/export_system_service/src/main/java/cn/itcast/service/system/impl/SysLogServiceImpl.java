package cn.itcast.service.system.impl;

import cn.itcast.dao.system.SysLogDao;
import cn.itcast.domain.system.SysLog;
import cn.itcast.service.system.SysLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysLogServiceImpl implements SysLogService {

    @Autowired
    private SysLogDao sysLogDao;

    @Override
    public void save(SysLog log) {
        sysLogDao.save(log);
    }

    @Override
    public PageInfo findPage(String companyId, int page, int size) {
        PageHelper.startPage(page,size);
        List<SysLog> list = sysLogDao.findAll(companyId);
        return new PageInfo(list);
    }
}
