package cn.itcast.service.system;

import cn.itcast.domain.system.Module;
import cn.itcast.domain.system.User;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ModuleService {

    public List<Module> findAll();

    public void save(Module module);

    public Module findById(String id);

    public void update(Module module);

    public void deleteById(String id);

    public PageInfo findPage(Integer page, int size);

    List<Module> findByRoleId(String roleid);

    List<Module> findByUser(User user);
}
