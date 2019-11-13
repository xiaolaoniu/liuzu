package cn.itcast.service.cargo.impl;

import cn.itcast.dao.cargo.*;
import cn.itcast.domain.cargo.*;
import cn.itcast.service.cargo.ExportService;
import cn.itcast.vo.ExportProductResult;
import cn.itcast.vo.ExportResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private ExportDao exportDao;

    @Autowired
    private ContractDao contractDao;
    @Autowired
    private ContractProductDao contractProductDao;
    @Autowired
    private ExtCproductDao extCproductDao;

    @Autowired
    private ExportProductDao exportProductDao;

    @Autowired
    private ExtEproductDao extEproductDao;



    @Override
    public Export findById(String id) {
        return exportDao.selectByPrimaryKey(id);
    }

    @Override
    public void save(Export export) {
//        这里需要同时保存
//                报运单----》从页面上+合同数据（合同号、货物数量、附件数量）
//                报运单货物表----》数据从合同货物表中获取
//                报运单附件表----》数据从合同附件表中获取
        String[] contractIds = export.getContractIds().split(",");
        StringBuffer customNames = new StringBuffer("");
        Integer proNums =0; //用来接收所有合同下的货物数量
        Integer extNums =0;//用来接收所有合同下的附件数量
        for (String contractId : contractIds) {
            Contract contract = contractDao.selectByPrimaryKey(contractId);
            proNums+=contract.getProNum();
            extNums+=contract.getExtNum();
            customNames.append(contract.getCustomName()).append(" ");  //用来拼接客户名称

//            查询合同的货物 select * from co_contract_product where contract_id=?
            ContractProductExample contractProductExample = new ContractProductExample();
            contractProductExample.createCriteria().andContractIdEqualTo(contractId);
            List<ContractProduct> contractProductList = contractProductDao.selectByExample(contractProductExample);
            for (ContractProduct contractProduct : contractProductList) {  //有多少个合同货物就应该保存多少个报运单货物
//                报运报运单的货物
                ExportProduct exportProduct = new ExportProduct();
                BeanUtils.copyProperties(contractProduct,exportProduct);
                exportProduct.setExportId(export.getId()); //设置报运单id
                exportProductDao.insertSelective(exportProduct);

                List<ExtCproduct> extCproducts = contractProduct.getExtCproducts();
                for (ExtCproduct extC : extCproducts) {
//                    报运单的附件
                    ExtEproduct extE = new ExtEproduct();
                    BeanUtils.copyProperties(extC,extE);
                    extE.setExportProductId(exportProduct.getId()); //设置报运单货物ID
                    extE.setExportId(export.getId());//设计报运单id
                    extEproductDao.insertSelective(extE);
                }
            }
        }
        export.setCustomerContract(customNames.toString());
        export.setExtNum(extNums);
        export.setProNum(proNums);
        exportDao.insertSelective(export);
    }

    @Override
    public void update(Export export) {
//        同时修改两个表1、报运单 2、报运单货物  TODO 有坑
        exportDao.updateByPrimaryKeySelective(export);
        List<ExportProduct> exportProducts = export.getExportProducts();
        for (ExportProduct exportProduct : exportProducts) {
            exportProductDao.updateByPrimaryKeySelective(exportProduct);
        }

    }

    @Override
    public void delete(String id) {
        exportDao.deleteByPrimaryKey(id);
    }

    @Override
    public PageInfo findAll(ExportExample example, int page, int size) {
        PageHelper.startPage(page,size);
        List<Export> list = exportDao.selectByExample(example);
        return new PageInfo(list);
    }

    @Override
    public void updateE(ExportResult exportResult) {
//        报运单 状态
        String exportId = exportResult.getExportId();
        Export export = exportDao.selectByPrimaryKey(exportId);
        export.setState(exportResult.getState());
        export.setRemark(exportResult.getRemark());
        exportDao.updateByPrimaryKeySelective(export);
//        货物
        Set<ExportProductResult> productResults = exportResult.getProducts();
        for (ExportProductResult productResult : productResults) {
            ExportProduct exportProduct = exportProductDao.selectByPrimaryKey(productResult.getExportProductId());
            exportProduct.setTax( productResult.getTax());
            exportProductDao.updateByPrimaryKeySelective(exportProduct);
        }

    }
}
