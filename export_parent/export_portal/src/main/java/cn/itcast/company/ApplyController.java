package cn.itcast.company;

import cn.itcast.domain.company.Company;
import cn.itcast.service.company.CompanyService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class ApplyController {

    @Reference
    private CompanyService companyService;

    @RequestMapping("/apply")
    @ResponseBody
    public String apply(Company company){
        try {
            company.setId(UUID.randomUUID().toString());
            company.setState(0);  //表示未审核状态
            companyService.save(company);
            return "1";  //表示成功
        } catch (Exception e) {
            e.printStackTrace();
            return "0"; //表示失败
        }
    }


}
