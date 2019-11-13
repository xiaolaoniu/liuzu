package cn.itcast.domain.system;

import lombok.Data;

import java.io.Serializable;

@Data
public class Dept implements Serializable {
    private String id;
    private String deptName;
//    private String parentId;
    private Integer state;
    private String companyId;
    private String companyName;
    private Dept parent; ///select * from pe_dept where id=parentId 根据子部门查询父部门
}
