package cn.itcast.service.cargo.impl;

import cn.itcast.dao.cargo.ContractDao;
import cn.itcast.dao.cargo.ContractProductDao;
import cn.itcast.dao.cargo.ExtCproductDao;
import cn.itcast.domain.cargo.Contract;
import cn.itcast.domain.cargo.ContractProduct;
import cn.itcast.domain.cargo.ContractProductExample;
import cn.itcast.domain.cargo.ExtCproduct;
import cn.itcast.service.cargo.ContractProductService;
import cn.itcast.vo.ContractProductVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.netty.util.internal.UnstableApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContractProductServiceImpl implements ContractProductService {

    @Autowired
    private ContractProductDao contractProductDao;
    @Autowired
    private ContractDao contractDao;
    @Autowired
    private ExtCproductDao extCproductDao;
    @Override
    public void save(ContractProduct contractProduct) {
//        1、计算当前货物的小计金额 单价*数量
        double amount = contractProduct.getPrice() * contractProduct.getCnumber();
        contractProduct.setAmount(amount);
        contractProductDao.insertSelective(contractProduct);

        String contractId = contractProduct.getContractId();//合同id
//        根据合同id查询合同对象
        Contract contract = contractDao.selectByPrimaryKey(contractId);
//        2、修改合同上的货物数量   +1
        contract.setProNum(contract.getProNum()+1);
//        3、修改合同上的总金额     累加小计金额
        contract.setTotalAmount(contract.getTotalAmount()+amount);
        contractDao.updateByPrimaryKeySelective(contract);
    }

    @Override
    public void update(ContractProduct contractProduct) {

        ContractProduct contractProduct_old = contractProductDao.selectByPrimaryKey(contractProduct.getId());
        Double amount_old = contractProduct_old.getAmount();
//        1、计算当前货物的小计金额 单价*数量
        double amount_new = contractProduct.getPrice() * contractProduct.getCnumber();
        contractProduct.setAmount(amount_new);
        contractProductDao.updateByPrimaryKeySelective(contractProduct);

        String contractId = contractProduct.getContractId();//合同id
//        根据合同id查询合同对象
        Contract contract = contractDao.selectByPrimaryKey(contractId);
 //        2、修改合同上的总金额     合同原总金额-货物的原小计金额+货物的现小计金额
        contract.setTotalAmount(contract.getTotalAmount()-amount_old+amount_new);
        contractDao.updateByPrimaryKeySelective(contract);

    }

    @Override
    public void delete(String id) {

//        1、删除当前货物
//        contractProductDao.deleteByPrimaryKey(id);
        ContractProduct contractProduct = contractProductDao.selectByPrimaryKey(id);//根据货物id查询货物对象
//        2、删除当前货物下的附件
        List<ExtCproduct> extCproducts = contractProduct.getExtCproducts();//根据货物直接货物此货物下的所有附件

        Double extTotalAmount = 0.0;  //准备接收所有附件的小计金额
        for (ExtCproduct extCproduct : extCproducts) {
            extTotalAmount+=extCproduct.getAmount();
            extCproductDao.deleteByPrimaryKey(extCproduct.getId());
        }
        String contractId = contractProduct.getContractId();
        Contract contract = contractDao.selectByPrimaryKey(contractId);
//        3、修改合同上的货物数量 -1
        contract.setProNum(contract.getProNum()-1);
//        4、修改合同上的附件数量 -附件的数量
        contract.setExtNum(contract.getExtNum()-extCproducts.size());
//        5、修改合同上的总金额 原金额-货物的小计金额-此货物下所有附件的小计金额
        contract.setTotalAmount(contract.getTotalAmount()-contractProduct.getAmount()-extTotalAmount);
        contractDao.updateByPrimaryKeySelective(contract);
        //        1、删除当前货物
        contractProductDao.deleteByPrimaryKey(id);

    }

    @Override
    public ContractProduct findById(String id) {
        return contractProductDao.selectByPrimaryKey(id);
    }

    @Override
    public PageInfo findAll(ContractProductExample example, int page, int size) {
        PageHelper.startPage(page,size);
        List<ContractProduct> list = contractProductDao.selectByExample(example);
        return new PageInfo(list);
    }

    @Override
    public void saveList(List<ContractProduct> productList) {
        for (ContractProduct contractProduct : productList) {
           this.save(contractProduct); //因为需要影响到合同上的数据，所以调用save方法
        }
    }

    @Override
    public List<ContractProductVo> findContractProductVoByShipTime(String inputDate, String companyId) {
        return contractProductDao.findContractProductVoByShipTime(inputDate,companyId);
    }
}
