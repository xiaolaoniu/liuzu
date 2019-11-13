package cn.itcast.controller.system;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.system.Module;
import cn.itcast.service.system.ModuleService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/system/module")
public class ModuleController extends BaseController{

    @Autowired
    private ModuleService moduleService;
    @RequestMapping(value="/list" ,name="进入菜单管理页面")
    public String findAll(@RequestParam(value = "page",defaultValue = "1") Integer pageNum,@RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Module> pageInfo = moduleService.findPage(pageNum,pageSize);
        request.setAttribute("page",pageInfo);
        return "system/module/module-list";
    }

    @RequestMapping(value="/toAdd" ,name="进入菜单新增页面")
    public String toAdd(){
//        查询所有本菜单的菜单数据
        List<Module> moduleList = moduleService.findAll();
        request.setAttribute("menus",moduleList);
        return "system/module/module-add";
    }

    @RequestMapping(value="/edit" ,name="保存菜单方法")
    public String edit(Module module){

//        module的id如果为空就是新增，反之就是修改

        if(StringUtils.isEmpty(module.getId())){ //id为空
            //        新增时需要赋id
            module.setId(UUID.randomUUID().toString());  //id 赋值成一个随机id
            moduleService.save(module);
        }else {  //id不为空
            moduleService.update(module);
        }

        return "redirect:/system/module/list.do";  //重定向到列表数据
    }
    @RequestMapping(value="/toUpdate" ,name="进入修改菜单页面")
    public String toUpdate(String id){
//         根据id查询菜单
        Module module = moduleService.findById(id);
//        把module放入request域中
        request.setAttribute("module",module);

        List<Module> moduleList = moduleService.findAll();
        request.setAttribute("menus",moduleList);
//        修改页面和新增页面公用一套
        return "system/module/module-add";
    }

    @RequestMapping(value="/delete" ,name="删除菜单方法")
    public String delete(String id){
        moduleService.deleteById(id);
        return "redirect:/system/module/list.do";  //重定向到列表数据
    }

}
