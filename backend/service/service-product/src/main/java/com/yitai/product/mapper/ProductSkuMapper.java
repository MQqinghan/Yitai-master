package com.yitai.product.mapper;

import com.yitai.model.dto.h5.ProductSkuDto;
import com.yitai.model.entity.product.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductSkuMapper {
    //2 根据销量排序，获取前10条记录
    List<ProductSku> selectProductSkuBySale();

    //分页查询
    List<ProductSku> findByPage(ProductSkuDto productSkuDto);

    //2 根据skuId获取sku信息
    ProductSku getById(Long skuId);

    //根据商品id获取商品所有sku列表
    List<ProductSku> findByProductId(Long productId);

    //更新SKU销量
    void updateSaleNum(@Param("skuId") Long skuId, @Param("num") Integer num);

    //扣减库存
    int deductStock(@Param("skuId") Long skuId, @Param("num") Integer num);

    //根据类目ID列表查询商品(排除指定SKU)
    List<ProductSku> selectByCategoryIdsWithExclude(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("excludedSkuIds") List<Long> excludedSkuIds,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    //统计SKU列表的类目分布
    List<java.util.Map<String, Object>> selectCategoryStatsBySkuIds(@Param("skuIds") List<Long> skuIds);

    //查询热门商品(按销量排序)
    List<ProductSku> selectHotProductSkus(@Param("offset") Integer offset, @Param("limit") Integer limit);

    //根据关键词查询商品类目分布
    List<java.util.Map<String, Object>> selectCategoryByKeyword(@Param("keyword") String keyword);

    //查询所有上架商品（用于同步到Elasticsearch）
    List<ProductSku> findOnlineProducts();

    //根据SKU ID列表批量查询商品信息
    List<ProductSku> selectByIds(@Param("skuIds") List<Long> skuIds);
    
    // 根据最近收藏商品的三级类目查询商品（排除指定SKU）
    List<ProductSku> selectByCategory3IdWithExclude(
            @Param("category3Id") Long category3Id,
            @Param("excludedSkuIds") List<Long> excludedSkuIds,
            @Param("limit") Integer limit
    );
    
    // 根据二级类目查询商品（排除指定SKU和三级类目）
    List<ProductSku> selectByCategory2IdWithExclude(
            @Param("category2Id") Long category2Id,
            @Param("excludedCategory3Id") Long excludedCategory3Id,
            @Param("excludedSkuIds") List<Long> excludedSkuIds,
            @Param("limit") Integer limit
    );
    
    // 根据一级类目查询商品（排除指定SKU和二级类目）
    List<ProductSku> selectByCategory1IdWithExclude(
            @Param("category1Id") Long category1Id,
            @Param("excludedCategory2Id") Long excludedCategory2Id,
            @Param("excludedSkuIds") List<Long> excludedSkuIds,
            @Param("limit") Integer limit
    );
    
    // 按销量降序、ID降序查询商品（排除指定SKU），用于兜底补充
    List<ProductSku> selectBySaleAndIdWithExclude(
            @Param("excludedSkuIds") List<Long> excludedSkuIds,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );
}
