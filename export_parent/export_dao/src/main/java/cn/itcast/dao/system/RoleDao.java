package cn.itcast.dao.system;

import cn.itcast.domain.system.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleDao {

    public List<Role> findAll(String companyId);

    void insert(Role role);

    Role selectByPrimaryKey(String id);

    void updateByPrimaryKey(Role role);

    void deleteByPrimaryKey(String id);

    void insertRoleAndModule(@Param("roleid") String roleid,@Param("mId") String mId);

    void deleteRoleAndModuleByRoleId(String roleid);

    List<Role> findByUserId(String id);
}
