package com.yitai.manager.service;

import com.yitai.model.dto.product.CategoryBrandDto;
import com.yitai.model.entity.product.Brand;
import com.yitai.model.entity.product.CategoryBrand;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface CategoryBrandService {

    //分类品牌条件分页查询
    PageInfo<CategoryBrand> findByPage(Integer page, Integer limit, CategoryBrandDto categoryBrandDto);

    //添加
    void save(CategoryBrand categoryBrand);
    //修改
    void updateById(CategoryBrand categoryBrand);

    //删除（逻辑删除）
    void deleteById(Long id);

    //根据分类id查询对应品牌数据
    List<Brand> findBrandByCategoryId(Long categoryId);
}
