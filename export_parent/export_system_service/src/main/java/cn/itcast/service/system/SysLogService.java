package cn.itcast.service.system;

import cn.itcast.domain.system.SysLog;
import com.github.pagehelper.PageInfo;

public interface SysLogService {
    //保存
    public void save(SysLog log);
        //分页查询
    public PageInfo findPage(String companyId, int page, int size);
}