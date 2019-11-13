package cn.itcast.controller.stat;

import cn.itcast.controller.BaseController;
import cn.itcast.service.stat.StatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@RequestMapping("/stat")
@Controller
public class StatController extends BaseController {

    @Reference
    private StatService statService;

    @RequestMapping("/toCharts")
    public String toCharts(String chartsType){

        return "stat/stat-"+chartsType;
    }

    @RequestMapping(value = "/factoryCharts",name = "获取每个工厂销售的方法")
    @ResponseBody
    public List<Map> factoryCharts(){

        return  statService.factoryCharts(companyId);
    }
    @RequestMapping(value = "/sellCharts",name = "获取每个产品销售数量 ")
    @ResponseBody
    public List<Map> sellCharts(){
        return  statService.sellCharts(companyId);
    }
    @RequestMapping(value = "/onlineCharts",name = "获取每个小时访问系统的数量")
    @ResponseBody
    public List<Map> onlineCharts(){
        return  statService.onlineCharts(companyId);
    }




}
