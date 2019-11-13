package cn.itcast.domain.system;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class User implements Serializable {
    private String id  ;
    private String deptId  ;  //所在部门id
    private String email   ;  //邮箱
    private String userName   ; //用户名
    private String station  ; //岗位
    private String password   ; //密码 MD5加密
    private String state   ;  //状态
    private String companyId ; //企业id
    private String companyName;//企业名称
    private String deptName   ;//部门名称
    private String managerId ; //上级领导id
    private String gender  ;  //性别
    private String telephone ; //电话号码
    private String birthday   ; //生日
    private Integer degree  ; //等级
    /*   0 作为内部控制，租户企业不能使用
        0-saas管理员
        1-企业管理员
        2-管理所有下属部门和人员  总经理
        3-管理本部门   部门经理
        4-普通员工
    */
    private Double salary  ;  //工资
    private String joinDate   ; //入职时间
    private Integer orderNo   ; // 排序号
    private String createBy   ;  //创建人
    private String createDept;   //创建人所在部门
    private Date createTime;   //创建时间
    private String updateBy   ; //更新人
    private Date updateTime;   //更新时间
    private String remark  ;   //备注
}
