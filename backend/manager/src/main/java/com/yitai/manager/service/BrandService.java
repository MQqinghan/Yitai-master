package com.yitai.manager.service;

import com.github.pagehelper.PageInfo;
import com.yitai.model.entity.product.Brand;

import java.util.List;

public interface BrandService {

    //列表（查询所有品牌）
    PageInfo<Brand> findByPage(Integer page, Integer limit);

    //添加
    void save(Brand brand);

    //查询所有品牌
    List<Brand> findAll();

    //修改
    void updateById(Brand brand);

    //删除（逻辑删除）
    void deleteById(Long id);
}