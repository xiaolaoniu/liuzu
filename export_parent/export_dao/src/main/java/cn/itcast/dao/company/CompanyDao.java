package cn.itcast.dao.company;

import cn.itcast.domain.company.Company;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CompanyDao {

    public List<Company> findAll();

    void insert(Company company);

    Company selectByPrimaryKey(String id);

    void updateByPrimaryKey(Company company);

    void deleteByPrimaryKey(String id);

    Long findCount();

    List<Company> findPage(@Param("start") int start, @Param("size")int size);
}
