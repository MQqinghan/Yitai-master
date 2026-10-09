package com.yitai.ai.service;

import com.yitai.ai.model.AiResult;
import com.yitai.ai.model.PageData;
import com.yitai.ai.model.ToolCall;
import com.yitai.model.entity.product.ProductSku;
import com.yitai.model.vo.h5.SearchResultVo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导购 Agent 的工具执行器。
 *
 * 安全模型：只暴露【查询类】工具，绝不调用下单 / 支付 / 扣库存 / 改销量等写操作。
 * 通过负载均衡 RestTemplate 只读调用 service-product。
 */
@Component
public class ToolExecutor {

    private final RestTemplate restTemplate;
    private final HelpDocRetriever helpDocRetriever;

    /** 最近一次商品类工具产出的结构化卡片（供前端渲染可点击卡片），仅保留最近一批 */
    private final List<Map<String, Object>> productCards = new ArrayList<>();

    public ToolExecutor(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
                        HelpDocRetriever helpDocRetriever) {
        this.restTemplate = restTemplate;
        this.helpDocRetriever = helpDocRetriever;
    }

    public List<Map<String, Object>> getLastProductCards() {
        return productCards;
    }

    /** 统一入口：根据工具名分发 */
    public String execute(ToolCall tc) {
        Map<String, Object> a = (tc.getArgs() == null) ? new HashMap<>() : tc.getArgs();
        try {
            switch (tc.getTool()) {
                case "searchProducts":
                    return searchProducts(str(a, "keyword"), num(a, "categoryId"), num(a, "category2Id"),
                            dec(a, "minPrice"), dec(a, "maxPrice"), intv(a, "order", 1), 10);
                case "getProductDetail":
                    return getProductDetail(num(a, "skuId"));
                case "getStock":
                    return getStock(num(a, "skuId"));
                case "recommendProducts":
                    return recommendProducts(intv(a, "limit", 10));
                case "searchHelp":
                    return "帮助文档参考：\n" + helpDocRetriever.retrieve(str(a, "query"));
                default:
                    return "未知工具：" + tc.getTool();
            }
        } catch (Exception e) {
            return "工具执行异常：" + e.getMessage();
        }
    }

    /** 工具1：商品搜索（关键词 / 分类 / 价格区间 / 排序），只读，走 ES 搜索接口 */
    public String searchProducts(String keyword, Long categoryId, Long category2Id, BigDecimal minPrice, BigDecimal maxPrice, Integer order, int limit) {
        StringBuilder url = new StringBuilder("http://service-product/api/product/search/1/")
                .append(limit).append("?order=").append(order == null ? 1 : order);
        if (keyword != null && !keyword.isBlank()) {
            url.append("&keyword=").append(encode(keyword));
        }
        if (categoryId != null) {
            url.append("&category1Id=").append(categoryId);
        }
        if (category2Id != null) {
            url.append("&category2Id=").append(category2Id);
        }
        if (minPrice != null) {
            url.append("&minPrice=").append(minPrice);
        }
        if (maxPrice != null) {
            url.append("&maxPrice=").append(maxPrice);
        }
        try {
            AiResult<SearchResultVo> res = restTemplate.exchange(url.toString(), HttpMethod.GET, null,
                    new ParameterizedTypeReference<AiResult<SearchResultVo>>() {
                    }).getBody();
            SearchResultVo vo = (res == null) ? null : res.getData();
            List<ProductSku> list = (vo == null || vo.getList() == null) ? null : vo.getList();
            if (list == null || list.isEmpty()) {
                // 属性词兜底：当 keyword + 类目/价格组合命中 0 条时，多半是“女装/男装”这类不在商品名里的属性词导致，
                // 去掉 keyword 仅用类目+价格重试，避免追问时把卡片清空（如“我是女生，推荐女装”）。
                if (keyword != null && (categoryId != null || category2Id != null || minPrice != null || maxPrice != null)) {
                    return searchProducts(null, categoryId, category2Id, minPrice, maxPrice, order, limit);
                }
                productCards.clear();
                return "未找到符合条件的商品（可尝试放宽价格区间或换个关键词）。";
            }
            StringBuilder sb = new StringBuilder("为您找到以下商品：\n");
            int n = Math.min(5, list.size());
            productCards.clear();
            for (int i = 0; i < n; i++) {
                ProductSku s = list.get(i);
                productCards.add(toCard(s));
                sb.append(i + 1).append(". ").append(s.getSkuName())
                        .append(" 售价¥").append(s.getSalePrice());
                if (s.getStockNum() != null) {
                    sb.append(" 库存").append(s.getStockNum());
                }
                sb.append("（skuId=").append(s.getId()).append("）\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "商品搜索暂时不可用：" + e.getMessage();
        }
    }

    /** 工具2：商品详情，只读（getBySkuId 返回裸 ProductSku） */
    public String getProductDetail(Long skuId) {
        try {
            ProductSku s = restTemplate.getForObject(
                    "http://service-product/api/product/getBySkuId/" + skuId, ProductSku.class);
            if (s == null) {
                return "未找到该商品。";
            }
            productCards.clear();
            productCards.add(toCard(s));
            return "商品：" + s.getSkuName() + "，售价¥" + s.getSalePrice()
                    + "，市场价¥" + s.getMarketPrice()
                    + "，库存" + s.getStockNum() + "，品牌：" + s.getBrandName();
        } catch (Exception e) {
            return "商品详情获取失败：" + e.getMessage();
        }
    }

    /** 工具3：库存查询，只读 */
    public String getStock(Long skuId) {
        try {
            ProductSku s = restTemplate.getForObject(
                    "http://service-product/api/product/getBySkuId/" + skuId, ProductSku.class);
            if (s == null) {
                return "未找到该商品。";
            }
            return "skuId=" + skuId + " 当前库存：" + s.getStockNum();
        } catch (Exception e) {
            return "库存查询失败：" + e.getMessage();
        }
    }

    /** 工具4：热门推荐（按销量排序），只读 */
    public String recommendProducts(int limit) {
        String url = "http://service-product/api/product/1/" + limit + "?order=3";
        try {
            AiResult<PageData> res = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<AiResult<PageData>>() {
                    }).getBody();
            List<ProductSku> list = (res == null || res.getData() == null) ? null : res.getData().getList();
            if (list == null || list.isEmpty()) {
                productCards.clear();
                return "暂无推荐。";
            }
            StringBuilder sb = new StringBuilder("热门推荐：\n");
            int n = Math.min(5, list.size());
            productCards.clear();
            for (int i = 0; i < n; i++) {
                ProductSku s = list.get(i);
                productCards.add(toCard(s));
                sb.append(i + 1).append(". ").append(s.getSkuName()).append(" ¥").append(s.getSalePrice()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "推荐获取失败：" + e.getMessage();
        }
    }

    private Map<String, Object> toCard(ProductSku s) {
        Map<String, Object> card = new HashMap<>();
        card.put("skuId", s.getId());
        card.put("name", s.getSkuName());
        card.put("price", s.getSalePrice());
        return card;
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? null : String.valueOf(v);
    }

    private static Long num(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal dec(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) return null;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private static int intv(Map<String, Object> m, String k, int def) {
        Object v = m.get(k);
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return def;
        }
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}
