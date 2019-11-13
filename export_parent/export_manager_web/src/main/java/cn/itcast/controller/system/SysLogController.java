package cn.itcast.controller.system;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.system.Dept;
import cn.itcast.domain.system.SysLog;
import cn.itcast.service.system.DeptService;
import cn.itcast.service.system.SysLogService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/system/log")
public class SysLogController extends BaseController {

    @Autowired
    private SysLogService sysLogService;
    @RequestMapping(value = "/list",name = "日志列表数据展示")
    public String findAll( @RequestParam(value = "page",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        PageInfo<SysLog> page = sysLogService.findPage(companyId,pageNum,pageSize);
        request.setAttribute("page",page);
        return "system/log/log-list";
    }


}
