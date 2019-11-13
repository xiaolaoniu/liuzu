package cn.itcast.dao.system;

import cn.itcast.domain.system.Dept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DeptDao {

    public List<Dept> findAll(String companyId);

    void insert(Dept dept);

    Dept selectByPrimaryKey(String id);

    void updateByPrimaryKey(Dept dept);

    void deleteByPrimaryKey(String id);

}
