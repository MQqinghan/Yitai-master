package com.yitai.manager.mapper;

import com.yitai.model.entity.product.Brand;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BrandMapper {
    //列表（查询所有品牌）
    List<Brand> findByPage();
    //添加
    void save(Brand brand);

    //修改
    void updateById(Brand brand);
    //逻辑删除
    void deleteById(Long id);
}
