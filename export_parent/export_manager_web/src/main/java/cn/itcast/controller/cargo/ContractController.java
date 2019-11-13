package cn.itcast.controller.cargo;

import cn.itcast.controller.BaseController;
import cn.itcast.domain.cargo.Contract;
import cn.itcast.domain.cargo.ContractExample;
import cn.itcast.service.cargo.ContractProductService;
import cn.itcast.service.cargo.ContractService;
import cn.itcast.utils.DownloadUtil;
import cn.itcast.vo.ContractProductVo;
import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.sun.xml.internal.rngom.digested.DTextPattern;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cargo/contract")
public class ContractController extends BaseController {

    @Reference
    private ContractService contractService;
    @Reference
    private ContractProductService contractProductService;

    @RequestMapping(value = "/list", name = "显示购销合同列表数据")
    public String list(@RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "pageSize", defaultValue = "10") int size) {
//        需要companyId
//        ContractExample构建查询条件     where company_id=?  and create_by=当前登录人 order by create_time desc
        ContractExample contractExample = new ContractExample();  //构建example用来存放构建的条件
        ContractExample.Criteria criteria = contractExample.createCriteria(); //创建内部类，各种条件都是放到内部类中
        Integer degree = user.getDegree();
//        系统管理员 :1  能看当前公司的所有数据   where company_id=?
//        总经理 :2     能看当前部门以及下级部门
//        部门经理 :3  能看当前部门     where company_id=?  and create_dept=当前登录人的部门id
//        普通员工 :4  只能看自己的     where company_id=?  and create_by=当前登录人

//        1、系统管理能看当前公司的所有数据  laowang    where company_id=?
//        2、部门总经理能看当前部门以及下级部门   dalaowang 销售总部的总经理   where company_id=? and create_dept like '所在部门id%'
//        3、部门经理能看当前部门    dawang 销售部北京分部的部门经理   where company_id=? and create_dept = '所在部门id'
//        4、普通员工只能看自己的     xiaowang xiaoli  销售部北京分部的普通员工  where company_id=? and create_by = '当前登录人id'
        criteria.andCompanyIdEqualTo(companyId);
        switch (degree) {
            case 1: {
                //不用再追加其他的条件
                break;
            }
            case 2: {
                criteria.andCreateDeptLike(user.getDeptId() + "%");
                break;
            }
            case 3: {
                criteria.andCreateDeptEqualTo(user.getDeptId());
                break;
            }
            case 4: {
                criteria.andCreateByEqualTo(user.getId());
                break;
            }
            default: {
                break;
            }
        }
        contractExample.setOrderByClause("create_time desc");
        PageInfo pageInfo = contractService.findAll(contractExample, page, size);
        request.setAttribute("page", pageInfo);
        return "cargo/contract/contract-list";
    }

    @RequestMapping("/toAdd")
    public String toAdd() {
        return "cargo/contract/contract-add";
    }

    @RequestMapping("/toUpdate")
    public String toUpdate(String id) {
        Contract contract = contractService.findById(id);
        request.setAttribute("contract", contract);
        return "cargo/contract/contract-add";
    }

    @RequestMapping("/edit")
    public String edit(Contract contract) {
        if (StringUtils.isEmpty(contract.getId())) {
            contract.setId(UUID.randomUUID().toString());
            contract.setState(0);//新增的合同状态为草稿 对应的值是0
            contract.setCompanyId(companyId);
            contract.setCompanyName(companyName);
            contract.setCreateBy(createBy);
            contract.setCreateDept(user.getDeptId());   // 在细颗粒度控制时需要此字段
            contract.setCreateTime(new Date());
            contractService.save(contract);
        } else {
            contract.setUpdateBy(createBy);
            contract.setUpdateTime(new Date());
            contractService.update(contract);
        }
        return "redirect:/cargo/contract/list.do";
    }

    @RequestMapping(value = "/print", name = "今天出货表打印页面")
    public String print() {
        return "cargo/print/contract-print";
    }

    @Autowired
    private DownloadUtil downloadUtil;

    @RequestMapping(value = "/printExcel", name = "出货表打印方法")
    public void printExcel(String inputDate) throws Exception { //inputDate格式yyyy-MM 2015-01
//        查询出货表的数据
        List<ContractProductVo> contractProductVoList = contractProductService.findContractProductVoByShipTime(inputDate, companyId);

        Workbook workbook = new XSSFWorkbook(); //创建了一个全新（里面什么都没有）的工作薄
        Sheet sheet = workbook.createSheet("出货表");  //创建了一个全新（里面什么都没有）的工作表
        //        Column
        Row row = sheet.createRow(0); //创建第一行
        for (int i = 0; i < 9; i++) {   //在第一行上创建9个单元格
            row.createCell(i);
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 8)); //合并单元格
        Row bigTitleRow = sheet.getRow(0);
        bigTitleRow.setHeightInPoints(36);  //行高
        Cell bigCell = bigTitleRow.getCell(1);
        bigCell.setCellValue(inputDate.replace("-0", "年").replace("-", "年") + "月份出货表");  //2019-01-->2019年1月份出货表   2019-10-->2019年10月份出货表
        bigCell.setCellStyle(bigTitle(workbook)); //设置样式
//----------------------------大标题完成
        sheet.setColumnWidth(1, 26 * 256); //设置列宽
        sheet.setColumnWidth(2, 11 * 256); //设置列宽
        sheet.setColumnWidth(3, 29 * 256); //设置列宽
        sheet.setColumnWidth(4, 15 * 256); //设置列宽
        sheet.setColumnWidth(5, 15 * 256); //设置列宽
        sheet.setColumnWidth(6, 15 * 256); //设置列宽
        sheet.setColumnWidth(7, 15 * 256); //设置列宽
        sheet.setColumnWidth(8, 15 * 256); //设置列宽

        Row titleRow = sheet.createRow(1);
//        客户	合同号	货号	数量	工厂	工厂交期	船期	贸易条款

        String[] titles = new String[]{"客户","合同号","货号","数量","工厂","工厂交期","船期","贸易条款"};
        Cell cell = null;
        for (int i = 1; i <= 8; i++) {
            cell = titleRow.createCell(i);
            cell.setCellStyle(title(workbook)); //设置小标题的样式
            cell.setCellValue(titles[i-1]);  //设置小标题的值
        }
    //----------------小标题完成
//        客户	合同号	货号	数量	工厂	工厂交期	船期	贸易条款
        int rowIndex=2;
        Row row1 = null;
        for (ContractProductVo productVo : contractProductVoList) {
            row1 = sheet.createRow(rowIndex);

            cell = row1.createCell(1);
            cell.setCellValue(productVo.getCustomName());
            cell.setCellStyle(text(workbook));

            cell = row1.createCell(2);
            cell.setCellValue(productVo.getContractNo());
            cell.setCellStyle(text(workbook));

            cell = row1.createCell(3);
            cell.setCellValue(productVo.getProductNo());
            cell.setCellStyle(text(workbook));

            cell = row1.createCell(4);
            cell.setCellValue(productVo.getCnumber());
            cell.setCellStyle(text(workbook));

            cell = row1.createCell(5);
            cell.setCellValue(productVo.getFactoryName());
            cell.setCellStyle(text(workbook));

            cell = row1.createCell(6);
            cell.setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(productVo.getDeliveryPeriod()));
            cell.setCellStyle(text(workbook));

            cell = row1.createCell(7);
            cell.setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(productVo.getShipTime()));
            cell.setCellStyle(text(workbook));

            cell = row1.createCell(8);
            cell.setCellValue(productVo.getTradeTerms());
            cell.setCellStyle(text(workbook));

            rowIndex++;
        }
        //数据填充完成
//        导出一个excel  下载一个文件
//         涉及到一个流：文件输出流：
//        两个头1、文件的打开方式（inline直接在浏览器中打开，attachment：以附件方式下载）
//        2、文件的mime类型  常见的文件可以省略
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); //创建一个字节数组缓存流
        workbook.write(byteArrayOutputStream);//把workbook中的内容写入到字节流中
        downloadUtil.download(byteArrayOutputStream,response,"出货表.xlsx");
    }



    @RequestMapping(value = "/printExcelWithTemplate", name = "根据模板出货表打印方法")
    public void printExcelWithTemplate(String inputDate) throws Exception { //inputDate格式yyyy-MM 2015-01

        List<ContractProductVo> contractProductVoList = contractProductService.findContractProductVoByShipTime(inputDate, companyId);
//        session.getServletContext().getRealPath("");//获取项目的根目录

        String templatePath = session.getServletContext().getRealPath("/make/xlsprint/tOUTPRODUCT.xlsx");
        Workbook workbook = new XSSFWorkbook(new FileInputStream(templatePath)); //创建了模板的工作薄
        Sheet sheet = workbook.getSheetAt(0);  //获取了第一个工作表

        Row bigTitleRow = sheet.getRow(0);
        Cell bigCell = bigTitleRow.getCell(1);
        bigCell.setCellValue(inputDate.replace("-0", "年").replace("-", "年") + "月份出货表");  //2019-01-->2019年1月份出货表   2019-10-->2019年10月份出货表

//        客户	合同号	货号	数量	工厂	工厂交期	船期	贸易条款

//        获取模板中的单元格样式
        Row row = sheet.getRow(2);
        CellStyle[] cellStyles = new  CellStyle[8];
        for (int i = 1; i <=8; i++) {
            cellStyles[i-1] =  row.getCell(i).getCellStyle();
        }

        int rowIndex=2;
        Row row1 = null;
        Cell cell = null;
        for (ContractProductVo productVo : contractProductVoList) {
            row1 = sheet.createRow(rowIndex);
            cell = row1.createCell(1);
            cell.setCellValue(productVo.getCustomName());
            cell.setCellStyle(cellStyles[0]);

            cell = row1.createCell(2);
            cell.setCellValue(productVo.getContractNo());
            cell.setCellStyle(cellStyles[1]);

            cell = row1.createCell(3);
            cell.setCellValue(productVo.getProductNo());
            cell.setCellStyle(cellStyles[2]);

            cell = row1.createCell(4);
            cell.setCellValue(productVo.getCnumber());
            cell.setCellStyle(cellStyles[3]);

            cell = row1.createCell(5);
            cell.setCellValue(productVo.getFactoryName());
            cell.setCellStyle(cellStyles[4]);

            cell = row1.createCell(6);
            cell.setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(productVo.getDeliveryPeriod()));
            cell.setCellStyle(cellStyles[5]);

            cell = row1.createCell(7);
            cell.setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(productVo.getShipTime()));
            cell.setCellStyle(cellStyles[6]);

            cell = row1.createCell(8);
            cell.setCellValue(productVo.getTradeTerms());
            cell.setCellStyle(cellStyles[7]);
            rowIndex++;
        }
        //数据填充完成
//        导出一个excel  下载一个文件
//         涉及到一个流：文件输出流：
//        两个头1、文件的打开方式（inline直接在浏览器中打开，attachment：以附件方式下载）
//        2、文件的mime类型  常见的文件可以省略
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); //创建一个字节数组缓存流
        workbook.write(byteArrayOutputStream);//把workbook中的内容写入到字节流中
        downloadUtil.download(byteArrayOutputStream,response,"出货表.xlsx");
    }


    @RequestMapping(value = "/printExcelWithMillion", name = "百万数据出货表打印方法")
    public void printExcelWithMillion(String inputDate) throws Exception { //inputDate格式yyyy-MM 2015-01

        List<ContractProductVo> contractProductVoList = contractProductService.findContractProductVoByShipTime(inputDate, companyId);

        Workbook workbook = new SXSSFWorkbook(); //创建了一个全新（里面什么都没有）的工作薄
        Sheet sheet = workbook.createSheet("出货表");  //创建了一个全新（里面什么都没有）的工作表
        //        Column
        Row row = sheet.createRow(0); //创建第一行
        for (int i = 0; i < 9; i++) {   //在第一行上创建9个单元格
            row.createCell(i);
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 8)); //合并单元格
        Row bigTitleRow = sheet.getRow(0);
        bigTitleRow.setHeightInPoints(36);  //行高
        Cell bigCell = bigTitleRow.getCell(1);
        bigCell.setCellValue(inputDate.replace("-0", "年").replace("-", "年") + "月份出货表");  //2019-01-->2019年1月份出货表   2019-10-->2019年10月份出货表
        bigCell.setCellStyle(bigTitle(workbook)); //设置样式
//----------------------------大标题完成
        sheet.setColumnWidth(1, 26 * 256); //设置列宽
        sheet.setColumnWidth(2, 11 * 256); //设置列宽
        sheet.setColumnWidth(3, 29 * 256); //设置列宽
        sheet.setColumnWidth(4, 15 * 256); //设置列宽
        sheet.setColumnWidth(5, 15 * 256); //设置列宽
        sheet.setColumnWidth(6, 15 * 256); //设置列宽
        sheet.setColumnWidth(7, 15 * 256); //设置列宽
        sheet.setColumnWidth(8, 15 * 256); //设置列宽

        Row titleRow = sheet.createRow(1);
//        客户	合同号	货号	数量	工厂	工厂交期	船期	贸易条款

        String[] titles = new String[]{"客户","合同号","货号","数量","工厂","工厂交期","船期","贸易条款"};
        Cell cell = null;
        for (int i = 1; i <= 8; i++) {
            cell = titleRow.createCell(i);
            cell.setCellStyle(title(workbook)); //设置小标题的样式
            cell.setCellValue(titles[i-1]);  //设置小标题的值
        }
        //----------------小标题完成
//        客户	合同号	货号	数量	工厂	工厂交期	船期	贸易条款
        int rowIndex=2;
        Row row1 = null;
        for (ContractProductVo productVo : contractProductVoList) {
            for (int i = 0; i < 6000; i++) {
            row1 = sheet.createRow(rowIndex);

            cell = row1.createCell(1);
            cell.setCellValue(productVo.getCustomName());
//            cell.setCellStyle(text(workbook));

            cell = row1.createCell(2);
            cell.setCellValue(productVo.getContractNo());
//            cell.setCellStyle(text(workbook));

            cell = row1.createCell(3);
            cell.setCellValue(productVo.getProductNo());
//            cell.setCellStyle(text(workbook));

            cell = row1.createCell(4);
            cell.setCellValue(productVo.getCnumber());
//            cell.setCellStyle(text(workbook));

            cell = row1.createCell(5);
            cell.setCellValue(productVo.getFactoryName());
//            cell.setCellStyle(text(workbook));

            cell = row1.createCell(6);
            cell.setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(productVo.getDeliveryPeriod()));
//            cell.setCellStyle(text(workbook));

            cell = row1.createCell(7);
            cell.setCellValue(new SimpleDateFormat("yyyy-MM-dd").format(productVo.getShipTime()));
//            cell.setCellStyle(text(workbook));

            cell = row1.createCell(8);
            cell.setCellValue(productVo.getTradeTerms());
//            cell.setCellStyle(text(workbook));
                rowIndex++;
            }

        }
        //数据填充完成
//        导出一个excel  下载一个文件
//         涉及到一个流：文件输出流：
//        两个头1、文件的打开方式（inline直接在浏览器中打开，attachment：以附件方式下载）
//        2、文件的mime类型  常见的文件可以省略
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); //创建一个字节数组缓存流
        workbook.write(byteArrayOutputStream);//把workbook中的内容写入到字节流中
        downloadUtil.download(byteArrayOutputStream,response,"出货表.xlsx");
    }

    //大标题的样式
    public CellStyle bigTitle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);//字体加粗
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);                //横向居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);        //纵向居中
        return style;
    }

    //小标题的样式
    public CellStyle title(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("黑体");
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);                //横向居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);        //纵向居中
        style.setBorderTop(BorderStyle.THIN);                        //上细线
        style.setBorderBottom(BorderStyle.THIN);                    //下细线
        style.setBorderLeft(BorderStyle.THIN);                        //左细线
        style.setBorderRight(BorderStyle.THIN);                        //右细线
        return style;
    }

    //文字样式
    public CellStyle text(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Times New Roman");
        font.setFontHeightInPoints((short) 10);

        style.setFont(font);

        style.setAlignment(HorizontalAlignment.LEFT);                //横向居左
        style.setVerticalAlignment(VerticalAlignment.CENTER);        //纵向居中
        style.setBorderTop(BorderStyle.THIN);                        //上细线
        style.setBorderBottom(BorderStyle.THIN);                    //下细线
        style.setBorderLeft(BorderStyle.THIN);                        //左细线
        style.setBorderRight(BorderStyle.THIN);                        //右细线

        return style;
    }


    @RequestMapping(value = "/submit", name = "购销合同提交")
    public String submit(String id) {
        contractService.updateState(id); //把此id对应的数据状态修改成1
        return "redirect:/cargo/contract/list.do";
    }

}
