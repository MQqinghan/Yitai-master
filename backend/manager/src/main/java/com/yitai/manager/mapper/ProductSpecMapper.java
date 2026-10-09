package com.yitai.manager.mapper;

import com.yitai.model.entity.product.ProductSpec;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductSpecMapper {


    //添加
    void save(ProductSpec productSpec);

    //修改
    void update(ProductSpec productSpec);

    //删除
    void delete(Long id);
    //列表
    List<ProductSpec> findAll();
}
