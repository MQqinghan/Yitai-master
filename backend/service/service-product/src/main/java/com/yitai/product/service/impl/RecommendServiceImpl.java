package com.yitai.product.service.impl;

import com.yitai.feign.cart.CartFeignClient;
import com.yitai.feign.order.OrderFeignClient;
import com.yitai.model.entity.h5.CartInfo;
import com.yitai.model.entity.product.Product;
import com.yitai.model.entity.product.ProductSku;
import com.yitai.model.entity.product.UserBrowseHistory;
import com.yitai.model.entity.product.UserSearchHistory;
import com.yitai.product.mapper.ProductMapper;
import com.yitai.product.mapper.ProductSkuMapper;
import com.yitai.product.mapper.UserBrowseHistoryMapper;
import com.yitai.product.mapper.UserCollectMapper;
import com.yitai.product.mapper.UserSearchHistoryMapper;
import com.yitai.product.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private UserBrowseHistoryMapper browseHistoryMapper;

    @Autowired
    private UserCollectMapper userCollectMapper;

    @Autowired
    private ProductSkuMapper productSkuMapper;
    
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserSearchHistoryMapper searchHistoryMapper;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private OrderFeignClient orderFeignClient;

    /**
     * 猜你喜欢推荐算法(实时计算)
     * 权重: 浏览历史(35%) + 收藏(25%) + 搜索记录(20%) + 购物车(20%) + 购买历史(25%, 额外信号)
     * 注: 购买历史已通过 Feign(order) 接入；各维度权重为相对排序权重，不要求归一
     * 
     * 特别优化: 如果用户最近有收藏商品，优先推荐与该收藏商品同类目的商品
     * 优先级: category3 > category2 > category1
     */
    @Override
    public List<ProductSku> guessYouLike(Long userId, Integer page, Integer limit) {
        // 1. 检查用户是否有最近收藏的商品
        Long latestCollectSkuId = userCollectMapper.selectLatestCollectSkuId(userId);
        
        if (latestCollectSkuId != null) {
            // 获取最近收藏商品的详细信息
            ProductSku latestCollectSku = productSkuMapper.getById(latestCollectSkuId);
            
            if (latestCollectSku != null) {
                // 统一获取所有候选商品，然后分页
                List<ProductSku> allCandidates = getAllCandidateProducts(
                    latestCollectSku.getProductId(), 
                    userId
                );
                
                // 分页返回
                return paginateList(allCandidates, page, limit);
            }
        }
        
        // 2. 如果没有最近收藏商品，使用常规推荐算法
        List<ProductSku> allCandidates = getAllCandidateProductsByPreference(userId);
        return paginateList(allCandidates, page, limit);
    }
    
    /**
     * 获取所有候选商品（基于最近收藏）
     */
    private List<ProductSku> getAllCandidateProducts(Long productId, Long userId) {
        // 获取商品信息
        Product product = productMapper.getById(productId);
        if (product == null) {
            return new ArrayList<>();
        }
        
        // 获取需要排除的SKU ID列表
        List<Long> excludedSkuIds = getExcludedSkuIds(userId);
        // 也要排除当前收藏的商品本身
        excludedSkuIds.add(productSkuMapper.findByProductId(productId).stream()
                .map(ProductSku::getId)
                .findFirst()
                .orElse(null));
        excludedSkuIds.removeIf(id -> id == null);
        
        List<ProductSku> allCandidates = new ArrayList<>();
        
        // 1. 优先收集category3相同的商品
        if (product.getCategory3Id() != null) {
            List<ProductSku> category3Products = productSkuMapper.selectByCategory3IdWithExclude(
                    product.getCategory3Id(), excludedSkuIds, 500
            );
            allCandidates.addAll(category3Products);
            
            // 将已收集的商品加入排除列表
            excludedSkuIds.addAll(category3Products.stream()
                    .map(ProductSku::getId)
                    .collect(Collectors.toList()));
        }
        
        // 2. 收集category2相同但category3不同的商品
        if (product.getCategory2Id() != null) {
            List<ProductSku> category2Products = productSkuMapper.selectByCategory2IdWithExclude(
                    product.getCategory2Id(),
                    product.getCategory3Id(),
                    excludedSkuIds,
                    500
            );
            allCandidates.addAll(category2Products);
            
            // 将已收集的商品加入排除列表
            excludedSkuIds.addAll(category2Products.stream()
                    .map(ProductSku::getId)
                    .collect(Collectors.toList()));
        }
        
        // 3. 收集category1相同但category2不同的商品
        if (product.getCategory1Id() != null) {
            List<ProductSku> category1Products = productSkuMapper.selectByCategory1IdWithExclude(
                    product.getCategory1Id(),
                    product.getCategory2Id(),
                    excludedSkuIds,
                    500
            );
            allCandidates.addAll(category1Products);
            
            // 将已收集的商品加入排除列表
            excludedSkuIds.addAll(category1Products.stream()
                    .map(ProductSku::getId)
                    .collect(Collectors.toList()));
        }
        
        // 4. 如果候选商品不足500个，使用兜底商品补充
        if (allCandidates.size() < 500) {
            int needMore = 500 - allCandidates.size();
            // 兜底查询：获取所有商品（不排除任何），直接追加到候选列表
            List<ProductSku> fallbackProducts = getFallbackProducts(0, needMore);
            allCandidates.addAll(fallbackProducts);
        }
        
        return allCandidates;
    }
    
    /**
     * 获取所有候选商品（基于用户偏好）
     */
    private List<ProductSku> getAllCandidateProductsByPreference(Long userId) {
        // 1. 获取用户偏好类目(加权统计)
        Map<Long, Double> categoryScoreMap = calculateUserPreference(userId);

        if (categoryScoreMap.isEmpty()) {
            // 如果用户没有任何行为数据,返回热门商品
            return getFallbackProducts(0, 500);
        }

        // 2. 从偏好类目中获取商品
        List<Long> topCategoryIds = categoryScoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(5) // 取前5个偏好类目
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 3. 查询这些类目的商品(排除已购买/已收藏的)
        List<Long> excludedSkuIds = getExcludedSkuIds(userId);

        List<ProductSku> allCandidates = productSkuMapper.selectByCategoryIdsWithExclude(
                topCategoryIds, excludedSkuIds, 0, 500
        );

        // 4. 如果候选商品不足500个,使用兜底商品补充
        if (allCandidates.size() < 500) {
            List<ProductSku> fallbackProducts = getFallbackProducts(
                    0, 
                    500 - allCandidates.size()
            );
            // 过滤掉已存在的商品
            Set<Long> existingIds = allCandidates.stream()
                    .map(ProductSku::getId)
                    .collect(Collectors.toSet());
            fallbackProducts = fallbackProducts.stream()
                    .filter(p -> !existingIds.contains(p.getId()))
                    .collect(Collectors.toList());
            allCandidates.addAll(fallbackProducts);
        }

        return allCandidates;
    }
    
    /**
     * 对列表进行分页
     */
    private List<ProductSku> paginateList(List<ProductSku> allList, Integer page, Integer limit) {
        int totalSize = allList.size();
        int offset = (page - 1) * limit;
        
        // 如果offset超出范围，返回空列表
        if (offset >= totalSize) {
            return new ArrayList<>();
        }
        
        // 计算结束位置
        int endIndex = Math.min(offset + limit, totalSize);
        
        // 返回分页后的子列表
        return new ArrayList<>(allList.subList(offset, endIndex));
    }

    /**
     * 记录用户浏览历史
     */
    @Override
    public void recordBrowseHistory(Long userId, Long skuId) {
        UserBrowseHistory browseHistory = new UserBrowseHistory();
        browseHistory.setUserId(userId);
        browseHistory.setSkuId(skuId);
        browseHistoryMapper.insertOrUpdate(browseHistory);
    }

    /**
     * 记录用户搜索历史
     */
    @Override
    public void recordSearchHistory(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        UserSearchHistory searchHistory = new UserSearchHistory();
        searchHistory.setUserId(userId);
        searchHistory.setKeyword(keyword.trim());
        searchHistoryMapper.insertOrUpdate(searchHistory);
    }

    /**
     * 获取用户最近浏览的商品列表
     */
    @Override
    public List<ProductSku> getBrowseHistory(Long userId, Integer limit) {
        // 1. 查询用户最近浏览的SKU ID列表（按时间倒序）
        List<Long> skuIds = browseHistoryMapper.selectRecentSkuIdsWithTime(userId, limit);
        
        if (skuIds == null || skuIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 根据SKU ID列表批量查询商品详情
        List<ProductSku> productSkus = productSkuMapper.selectByIds(skuIds);
        
        return productSkus;
    }

    /**
     * 计算用户偏好(多维度加权)
     * 浏览(35%) + 收藏(25%) + 搜索(20%) + 购物车(20%) + 购买历史(25%)
     */
    private Map<Long, Double> calculateUserPreference(Long userId) {
        Map<Long, Double> categoryScoreMap = new HashMap<>();

        // 1. 浏览历史权重(35%)
        List<Map<String, Object>> browseCategories = browseHistoryMapper.selectTopCategories(userId, 10);
        for (Map<String, Object> item : browseCategories) {
            Long categoryId = ((Number) item.get("categoryId")).longValue();
            Long count = ((Number) item.get("browseCount")).longValue();
            categoryScoreMap.merge(categoryId, count * 0.35, Double::sum);
        }

        // 2. 收藏权重(25%)
        List<Long> collectSkuIds = userCollectMapper.selectSkuIdsByUserId(userId);
        if (!collectSkuIds.isEmpty()) {
            List<Map<String, Object>> collectCategories = productSkuMapper.selectCategoryStatsBySkuIds(collectSkuIds);
            for (Map<String, Object> item : collectCategories) {
                Long categoryId = ((Number) item.get("categoryId")).longValue();
                Long count = ((Number) item.get("count")).longValue();
                categoryScoreMap.merge(categoryId, count * 0.25, Double::sum);
            }
        }

        // 3. 搜索记录权重(20%) - 根据搜索关键词匹配商品类目
        List<Map<String, Object>> topKeywords = searchHistoryMapper.selectTopKeywords(userId, 5);
        if (!topKeywords.isEmpty()) {
            for (Map<String, Object> keywordItem : topKeywords) {
                String keyword = (String) keywordItem.get("keyword");
                Long searchCount = ((Number) keywordItem.get("searchCount")).longValue();
                
                // 先尝试通过商品名称匹配类目
                List<Map<String, Object>> keywordCategories = productSkuMapper.selectCategoryByKeyword(keyword);
                for (Map<String, Object> catItem : keywordCategories) {
                    Long categoryId = ((Number) catItem.get("categoryId")).longValue();
                    Long matchCount = ((Number) catItem.get("matchCount")).longValue();
                    categoryScoreMap.merge(categoryId, searchCount * matchCount * 0.20, Double::sum);
                }
                
                // 商品名称未匹配到类目时，不再使用硬编码关键词-类目映射
                // （硬编码三级类目ID易与实际数据错位，属无效兜底），
                // 交由其它行为信号与热门兜底保障召回
            }
        }

        // 4. 购物车权重(20%)
        try {
            List<CartInfo> cartItems = cartFeignClient.getCartList();
            if (cartItems != null && !cartItems.isEmpty()) {
                List<Long> cartSkuIds = cartItems.stream()
                        .map(CartInfo::getSkuId)
                        .collect(Collectors.toList());
                List<Map<String, Object>> cartCategories = productSkuMapper.selectCategoryStatsBySkuIds(cartSkuIds);
                for (Map<String, Object> item : cartCategories) {
                    Long categoryId = ((Number) item.get("categoryId")).longValue();
                    Long count = ((Number) item.get("count")).longValue();
                    categoryScoreMap.merge(categoryId, count * 0.20, Double::sum);
                }
            }
        } catch (Exception e) {
            // Feign调用失败不影响其他推荐逻辑
            System.err.println("获取购物车数据失败: " + e.getMessage());
        }

        // 5. 购买历史权重(25%) - 通过Feign调用order服务获取已购类目统计
        try {
            List<Map<String, Object>> purchaseStats = orderFeignClient.getPurchaseCategoryStats(userId);
            if (purchaseStats != null) {
                for (Map<String, Object> item : purchaseStats) {
                    Long categoryId = ((Number) item.get("categoryId")).longValue();
                    Long purchaseCount = ((Number) item.get("purchaseCount")).longValue();
                    categoryScoreMap.merge(categoryId, purchaseCount * 0.25, Double::sum);
                }
            }
        } catch (Exception e) {
            // Feign调用失败不影响其他推荐逻辑
            System.err.println("获取购买历史偏好失败: " + e.getMessage());
        }

        return categoryScoreMap;
    }

    /**
     * 获取需要排除的SKU ID列表(已购买、已收藏、已在购物车)
     */
    private List<Long> getExcludedSkuIds(Long userId) {
        List<Long> excludedIds = new ArrayList<>();

        // 已收藏的商品
        List<Long> collectSkuIds = userCollectMapper.selectSkuIdsByUserId(userId);
        excludedIds.addAll(collectSkuIds);

        // 已在购物车的商品(通过Feign调用cart服务)
        try {
            List<CartInfo> cartItems = cartFeignClient.getCartList();
            if (cartItems != null && !cartItems.isEmpty()) {
                List<Long> cartSkuIds = cartItems.stream()
                        .map(CartInfo::getSkuId)
                        .collect(Collectors.toList());
                excludedIds.addAll(cartSkuIds);
            }
        } catch (Exception e) {
            // Feign调用失败不影响其他逻辑
            System.err.println("获取购物车数据失败: " + e.getMessage());
        }

        // 已购买的商品（通过Feign调用order服务，避免重复推荐已购商品）
        try {
            List<Long> purchasedSkuIds = orderFeignClient.getPurchasedSkuIds(userId);
            if (purchasedSkuIds != null && !purchasedSkuIds.isEmpty()) {
                excludedIds.addAll(purchasedSkuIds);
            }
        } catch (Exception e) {
            // Feign调用失败不影响其他逻辑
            System.err.println("获取已购商品数据失败: " + e.getMessage());
        }

        return excludedIds;
    }

    /**
     * 获取热门商品(兜底策略)
     */
    private List<ProductSku> getHotProducts(Integer page, Integer limit) {
        return productSkuMapper.selectHotProductSkus((page - 1) * limit, limit);
    }

    
    /**
     * 根据最近收藏商品推荐（优先级: category3 > category2 > category1）
     * 修复版：先查询足够多的候选商品，然后在内存中分页
     */
    private List<ProductSku> getRecommendByLatestCollect(Long productId, Long userId, Integer page, Integer limit) {
        // 获取商品信息
        Product product = productMapper.getById(productId);
        if (product == null) {
            return new ArrayList<>();
        }
        
        // 获取需要排除的SKU ID列表
        List<Long> excludedSkuIds = getExcludedSkuIds(userId);
        // 也要排除当前收藏的商品本身
        excludedSkuIds.add(productSkuMapper.findByProductId(productId).stream()
                .map(ProductSku::getId)
                .findFirst()
                .orElse(null));
        excludedSkuIds.removeIf(id -> id == null);
        
        // 计算需要的商品总数（为了支持分页，需要查询足够多的商品）
        int totalOffset = (page - 1) * limit;
        int needTotal = totalOffset + limit + 50; // 多查询一些用于兜底
        
        // 收集所有候选商品
        List<ProductSku> allCandidates = new ArrayList<>();
        
        // 1. 优先收集category3相同的商品
        if (product.getCategory3Id() != null) {
            List<ProductSku> category3Products = productSkuMapper.selectByCategory3IdWithExclude(
                    product.getCategory3Id(), excludedSkuIds, 200
            );
            allCandidates.addAll(category3Products);
            excludedSkuIds.addAll(category3Products.stream()
                    .map(ProductSku::getId)
                    .collect(Collectors.toList()));
        }
        
        // 2. 收集category2相同但category3不同的商品
        if (product.getCategory2Id() != null && allCandidates.size() < needTotal) {
            List<ProductSku> category2Products = productSkuMapper.selectByCategory2IdWithExclude(
                    product.getCategory2Id(),
                    product.getCategory3Id(),
                    excludedSkuIds,
                    200
            );
            allCandidates.addAll(category2Products);
            excludedSkuIds.addAll(category2Products.stream()
                    .map(ProductSku::getId)
                    .collect(Collectors.toList()));
        }
        
        // 3. 收集category1相同但category2不同的商品
        if (product.getCategory1Id() != null && allCandidates.size() < needTotal) {
            List<ProductSku> category1Products = productSkuMapper.selectByCategory1IdWithExclude(
                    product.getCategory1Id(),
                    product.getCategory2Id(),
                    excludedSkuIds,
                    200
            );
            allCandidates.addAll(category1Products);
            excludedSkuIds.addAll(category1Products.stream()
                    .map(ProductSku::getId)
                    .collect(Collectors.toList()));
        }
        
        // 4. 如果还不够，使用兜底商品补充
        if (allCandidates.size() < needTotal) {
            int needMore = needTotal - allCandidates.size();
            // 关键修复：使用getFallbackProducts（空排除列表），直接追加不过滤
            List<ProductSku> fallbackProducts = getFallbackProducts(0, needMore);
            allCandidates.addAll(fallbackProducts);
        }
        
        // 5. 在内存中分页
        if (totalOffset >= allCandidates.size()) {
            return new ArrayList<>();
        }
        
        int endIndex = Math.min(totalOffset + limit, allCandidates.size());
        return new ArrayList<>(allCandidates.subList(totalOffset, endIndex));
    }
    
    /**
     * 常规推荐算法（基于用户偏好）
     */
    private List<ProductSku> getRecommendByPreference(Long userId, Integer page, Integer limit) {
        // 1. 获取用户偏好类目(加权统计)
        Map<Long, Double> categoryScoreMap = calculateUserPreference(userId);

        if (categoryScoreMap.isEmpty()) {
            // 如果用户没有任何行为数据,返回热门商品
            return getFallbackProducts(0, limit);
        }

        // 2. 从偏好类目中获取商品
        List<Long> topCategoryIds = categoryScoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(5) // 取前5个偏好类目
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 3. 查询这些类目的商品(排除已购买/已收藏的)
        List<Long> excludedSkuIds = getExcludedSkuIds(userId);

        List<ProductSku> recommendList = productSkuMapper.selectByCategoryIdsWithExclude(
                topCategoryIds, excludedSkuIds, page, limit
        );

        // 4. 如果推荐数量不足,使用销量和ID排序的商品兜底补充
        if (recommendList.size() < limit) {
            List<ProductSku> fallbackProducts = getFallbackProducts(
                    0, 
                    limit - recommendList.size()
            );
            // 过滤掉已存在的商品
            Set<Long> existingIds = recommendList.stream()
                    .map(ProductSku::getId)
                    .collect(Collectors.toSet());
            fallbackProducts = fallbackProducts.stream()
                    .filter(p -> !existingIds.contains(p.getId()))
                    .collect(Collectors.toList());
            recommendList.addAll(fallbackProducts);
        }

        return recommendList;
    }
    
    /**
     * 获取兜底商品（按销量降序、ID降序）
     */
    private List<ProductSku> getFallbackProducts(Integer offset, Integer limit) {
        List<Long> emptyExcludedList = new ArrayList<>();
        return productSkuMapper.selectBySaleAndIdWithExclude(emptyExcludedList, offset, limit);
    }
}
