package com.yitai.product.mapper;

import com.yitai.model.entity.product.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CategoryMapper {

    //查询所有一级分类
    List<Category> selectOneCategory();

    //1 查询所有分类 返回list集合
    List<Category> findAll();
    
    //根据关键词查询分类ID列表（用于搜索扩展）
    List<Map<String, Object>> selectCategoryIdsByKeyword(@Param("keyword") String keyword);
    
    //根据父分类ID查询子分类列表
    List<Category> selectCategoryByParentId(@Param("parentId") Long parentId);
}
