package cn.itcast.dao.system;

import cn.itcast.domain.system.Dept;
import cn.itcast.domain.system.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDao {

    public List<User> findAll(String companyId);

    void insert(User user);

    User selectByPrimaryKey(String id);

    void updateByPrimaryKey(User user);

    void deleteByPrimaryKey(String id);

    void deleteUserAndRoleByUserId(String userid);

    void insertUserAndRole(@Param("userid") String userid,@Param("roleid") String rId);

    User findByEmail(String email);
}
