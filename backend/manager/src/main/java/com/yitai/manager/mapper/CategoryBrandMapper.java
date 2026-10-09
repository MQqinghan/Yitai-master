package com.yitai.manager.mapper;

import com.yitai.model.dto.product.CategoryBrandDto;
import com.yitai.model.entity.product.Brand;
import com.yitai.model.entity.product.CategoryBrand;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryBrandMapper {

    //分类品牌条件分页查询
    List<CategoryBrand> findByPage(CategoryBrandDto categoryBrandDto);

    //添加
    void save(CategoryBrand categoryBrand);

    //根据分类id查询对应品牌数据
    List<Brand> findBrandByCategoryId(Long categoryId);
    //修改
    void updateById(CategoryBrand categoryBrand);
    //逻辑删除
    void deleteById(Long id);
}
