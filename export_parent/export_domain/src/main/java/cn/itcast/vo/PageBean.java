package cn.itcast.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageBean<T> {
    //    1、当前页码
    private int pageNum;
    //    2、每页显示条数
    private int pageSize;
    //    3、每页的数据
    private List list;
    //    4、总条数
    private Long total;
    //    5、总页数
    private int totalPage;
    //   6、上一页
    private int pre;
    //    7、下一页
    private int next;
    //    8、起始页码
    private int start;
    //    9、结束页码
    private int end;

//    页面上传的： pageNum  pageSize
//    数据库查询：total list
//    通过计算获取到的： totalPage pre  next  start  end

    public PageBean(Integer pageNum,Integer pageSize,Long total,List list){
        this.pageNum=pageNum;
        this.pageSize=pageSize;
        this.total=total;
        this.list=list;


//        10条数据 每页显示3条  分4页
//        10条数据 每页显示2条  分5页
        this.totalPage= total%pageSize==0? (int)(total/pageSize) : (int)((total/pageSize) +1);


        if(pageNum==1){
            this.pre=1;
        }else{
            this.pre=pageNum-1;
        }

        if(pageNum==totalPage){
            this.next=pageNum;
        }else{
            this.next=pageNum+1;
        }
//        start  end
        calStartAndEnd(totalPage,pageNum);
    }

    private void calStartAndEnd(int totalPage,int pageNum) {
//        目标：在页面上始终显示5个页码
//        如果总页数是否超过5
        if(totalPage<=5){
            this.start=1;
            this.end=totalPage;
        }else{
            if(pageNum<=3){
//                情况1：当前页码小于等于3
                this.start=1;
                this.end=5;
            }else if((totalPage-pageNum)<=2){
//                情况2：总页数-当前页码<=2
                this.start=totalPage-4;
                this.end=totalPage;
            }else{
                //            剩下的就是情况3：以当前页有中心前面两个页码后面两个页码
                this.start=pageNum-2;
                this.end=pageNum+2;
            }
        }

    }

}
