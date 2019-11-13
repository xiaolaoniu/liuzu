package cn.itcast.service.cargo;


import cn.itcast.domain.cargo.ExportProduct;
import cn.itcast.domain.cargo.ExtEproduct;
import cn.itcast.domain.cargo.ExtEproductExample;
import com.github.pagehelper.PageInfo;


public interface ExtEproductService {

	ExtEproduct findById(String id);

	void save(ExtEproduct extEproduct);

	void update(ExtEproduct extEproduct);

	void delete(String id);

	PageInfo findAll(ExtEproductExample extEproductExample, int page, int size);
}
