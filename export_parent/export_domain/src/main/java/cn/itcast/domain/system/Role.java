package cn.itcast.domain.system;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class Role implements Serializable {

    private String id    ;
    private String name    ;
    private String remark    ;
    private String companyId  ;
    private String companyName;
    private Integer orderNo      ;
    private String createBy      ;
    private String createDept;
    private Date createTime;
    private String updateBy      ;
    private Date updateTime;
}
