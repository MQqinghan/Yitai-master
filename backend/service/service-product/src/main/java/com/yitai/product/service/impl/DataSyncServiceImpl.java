package com.yitai.product.service.impl;

import com.alibaba.fastjson2.JSON;
import com.yitai.model.entity.product.ProductSku;
import com.yitai.product.mapper.ProductSkuMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class DataSyncServiceImpl {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ProductSkuMapper productSkuMapper;

    // 应用启动时同步数据
    @PostConstruct
    public void syncDataToEs() {
        doSync();
    }

    /**
     * 手动触发数据同步（可通过接口调用）
     */
    public void doSync() {
        System.out.println("========== 开始同步商品数据到 Elasticsearch ==========");
        try {
            // 1. 查询所有上架商品
            List<ProductSku> productSkus = productSkuMapper.findOnlineProducts();
            
            if (productSkus.isEmpty()) {
                System.out.println("没有需要同步的商品数据");
                return;
            }

            System.out.println("查询到 " + productSkus.size() + " 条上架商品");

            // 2. 构建批量插入请求
            BulkRequest bulkRequest = new BulkRequest();
            for (ProductSku sku : productSkus) {
                IndexRequest indexRequest = new IndexRequest("product_index")
                        .id(sku.getId().toString()) // 文档 ID 与数据库 id 一致
                        .source(JSON.toJSONString(sku), XContentType.JSON);
                bulkRequest.add(indexRequest);
            }

            // 3. 执行批量插入
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                System.err.println("数据同步失败：" + bulkResponse.buildFailureMessage());
            } else {
                System.out.println("✅ 成功同步 " + productSkus.size() + " 条商品数据到 Elasticsearch");
            }
            System.out.println("===================================================");
        } catch (Exception e) {
            System.err.println("数据同步异常：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
