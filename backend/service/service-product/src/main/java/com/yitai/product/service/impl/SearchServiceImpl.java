package com.yitai.product.service.impl;

import com.alibaba.fastjson2.JSON;
import com.yitai.model.dto.h5.ProductSkuDto;
import com.yitai.model.entity.product.ProductSku;
import com.yitai.model.vo.h5.SearchResultVo;
import com.yitai.product.mapper.CategoryMapper;
import com.yitai.product.service.SearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public SearchResultVo search(ProductSkuDto dto, Integer page, Integer limit) {
        SearchResultVo resultVo = new SearchResultVo();
        try {
            // 1. 构建搜索请求
            SearchRequest searchRequest = new SearchRequest("product_index");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 2. 构建布尔查询
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

            // 关键词搜索（使用 match 查询配合 ik_max_word 分词器，提高召回率）
            if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
                BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
                boolean hasCategoryMapping = false;
                
                // 1. 文本匹配：使用 ik_max_word 分词器，提高中文召回率
                // skuName 权重最高(3.0)，productName 次之(2.0)，brandName 最低(1.0)
                keywordQuery.should(QueryBuilders.matchQuery("skuName", dto.getKeyword())
                        .analyzer("ik_max_word")
                        .boost(3.0f))
                        .should(QueryBuilders.matchQuery("productName", dto.getKeyword())
                                .analyzer("ik_max_word")
                                .boost(2.0f))
                        .should(QueryBuilders.matchQuery("brandName", dto.getKeyword())
                                .analyzer("ik_max_word")
                                .boost(1.0f));
                
                // 2. 分类匹配：查询包含关键词的分类，扩展搜索结果
                List<Long> categoryIds = new ArrayList<>();
                
                // 先尝试从数据库查询包含关键词的分类名称
                List<java.util.Map<String, Object>> matchedCategories = categoryMapper.selectCategoryIdsByKeyword(dto.getKeyword());
                if (matchedCategories != null && !matchedCategories.isEmpty()) {
                    categoryIds = matchedCategories.stream()
                            .map(m -> ((Number) m.get("id")).longValue())
                            .toList();
                }
                
                // 如果数据库没找到，使用常用搜索词映射（兜底策略）
                if (categoryIds.isEmpty()) {
                    categoryIds = getKeywordCategoryMapping(dto.getKeyword());
                    if (!categoryIds.isEmpty()) {
                        hasCategoryMapping = true;  // 标记为有明确分类映射的关键词
                    }
                }
                
                // 3. 根据情况添加分类条件
                if (!categoryIds.isEmpty()) {
                    if (hasCategoryMapping) {
                        // 有明确分类映射的关键词（如"穿搭"、"美妆"、"数码"）
                        // 将分类条件加入 should，这样即使文本不匹配，分类匹配也能满足条件
                        keywordQuery.should(QueryBuilders.termsQuery("category1Id", categoryIds).boost(2.0f));
                        // 同时使用 filter 严格限制一级分类，防止不相关商品混入
                        boolQuery.filter(QueryBuilders.termsQuery("category1Id", categoryIds));
                        keywordQuery.minimumShouldMatch(1);
                    } else {
                        // 数据库查询到的分类，使用 should 提高评分权重
                        keywordQuery.should(QueryBuilders.termsQuery("category1Id", categoryIds).boost(1.5f));
                        keywordQuery.should(QueryBuilders.termsQuery("category2Id", categoryIds).boost(1.5f));
                        keywordQuery.should(QueryBuilders.termsQuery("category3Id", categoryIds).boost(1.5f));
                        keywordQuery.minimumShouldMatch(1);
                    }
                } else {
                    // 没有分类匹配，只依赖文本匹配
                    keywordQuery.minimumShouldMatch(1);
                }
                
                boolQuery.must(keywordQuery);
            }

            // 品牌过滤
            if (dto.getBrandId() != null) {
                boolQuery.filter(QueryBuilders.termQuery("brandId", dto.getBrandId()));
            }

            // 一级分类过滤
            if (dto.getCategory1Id() != null) {
                boolQuery.filter(QueryBuilders.termQuery("category1Id", dto.getCategory1Id()));
            }
            // 二级分类过滤
            if (dto.getCategory2Id() != null) {
                boolQuery.filter(QueryBuilders.termQuery("category2Id", dto.getCategory2Id()));
            }
            // 三级分类过滤
            if (dto.getCategory3Id() != null) {
                boolQuery.filter(QueryBuilders.termQuery("category3Id", dto.getCategory3Id()));
            }

            // 价格区间过滤（支撑“XX元以内/预算”类导购需求）
            if (dto.getMinPrice() != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("salePrice").gte(dto.getMinPrice().doubleValue()));
            }
            if (dto.getMaxPrice() != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("salePrice").lte(dto.getMaxPrice().doubleValue()));
            }

            // 将构建好的查询条件设置到 sourceBuilder（关键！）
            sourceBuilder.query(boolQuery);
            // 3. 高亮设置（关键词标红）
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName").preTags("<em style='color:red'>").postTags("</em>");
            highlightBuilder.field("productName").preTags("<em style='color:red'>").postTags("</em>");
            sourceBuilder.highlighter(highlightBuilder);

            // 4. 排序策略：优先按相关性评分（关键词匹配度），其次按销量/价格
            // 注意：ES 默认会按 _score（相关性评分）排序，这里添加二级排序
            if (dto.getOrder() == 1) {
                // 综合排序：先按相关性评分降序，再按销量降序
                sourceBuilder.sort("_score", SortOrder.DESC);
                sourceBuilder.sort("saleNum", SortOrder.DESC);
            } else if (dto.getOrder() == 2) {
                // 价格升序：先按相关性评分降序，再按价格升序
                sourceBuilder.sort("_score", SortOrder.DESC);
                sourceBuilder.sort("salePrice", SortOrder.ASC);
            } else if (dto.getOrder() == 3) {
                // 价格降序：先按相关性评分降序，再按价格降序
                sourceBuilder.sort("_score", SortOrder.DESC);
                sourceBuilder.sort("salePrice", SortOrder.DESC);
            } else {
                // 默认：按相关性评分降序
                sourceBuilder.sort("_score", SortOrder.DESC);
            }

            // 5. 分页
            sourceBuilder.from((page - 1) * limit).size(limit);

            // 6. 执行搜索
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            // 7. 解析结果
            List<ProductSku> list = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                String source = hit.getSourceAsString();
                ProductSku sku = JSON.parseObject(source, ProductSku.class);

                // 处理高亮字段
                if (hit.getHighlightFields().containsKey("skuName")) {
                    sku.setSkuName(hit.getHighlightFields().get("skuName").fragments()[0].toString());
                }
                if (hit.getHighlightFields().containsKey("productName")) {
                    sku.setProductName(hit.getHighlightFields().get("productName").fragments()[0].toString());
                }
                list.add(sku);
            }

            resultVo.setList(list);
            resultVo.setTotal(searchResponse.getHits().getTotalHits().value);

        } catch (Exception e) {
            throw new RuntimeException("搜索异常：" + e.getMessage(), e);
        }
        return resultVo;
    }

    /**
     * 常用搜索关键词到分类ID的映射（兜底策略）
     * 当数据库中没有匹配的分类名称时使用
     */
    private List<Long> getKeywordCategoryMapping(String keyword) {
        List<Long> ids = new ArrayList<>();

        // 注意：以下一级分类 ID 必须与本库 category 表一致（generate_test_data.sql）：
        // 数码电子=1，服装服饰=2，食品饮料=3，家居家装=4，美妆护肤=5，运动户外=6 ...
        switch (keyword) {
            case "穿搭":
            case "服装":
            case "衣服":
                // 服装服饰（一级分类 2）
                ids.add(2L);
                break;
            case "美妆":
            case "化妆":
            case "彩妆":
            case "护肤":
                // 美妆护肤（一级分类 5）
                ids.add(5L);
                break;
            case "数码":
            case "电子":
            case "手机":
            case "电脑":
                // 数码电子（一级分类 1）
                ids.add(1L);
                break;
            case "家居":
            case "日用":
                // 家居家装（一级分类 4）
                ids.add(4L);
                break;
            default:
                // 未知关键词，不添加分类匹配
                break;
        }

        return ids;
    }
}
