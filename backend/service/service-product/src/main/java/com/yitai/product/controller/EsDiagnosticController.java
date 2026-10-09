package com.yitai.product.controller;

import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.product.service.impl.DataSyncServiceImpl;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch 诊断接口
 * 用于排查搜索问题
 */
@RestController
@RequestMapping("/admin/es/diagnostic")
public class EsDiagnosticController {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private DataSyncServiceImpl dataSyncService;

    /**
     * 检查 Elasticsearch 连接和索引状态
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> checkEsStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 检查 ES 连接
            boolean isConnected = restHighLevelClient.ping(RequestOptions.DEFAULT);
            result.put("esConnected", isConnected);
            
            if (!isConnected) {
                result.put("message", "无法连接到 Elasticsearch");
                return Result.build(result, ResultCodeEnum.SYSTEM_ERROR);
            }
            
            // 2. 检查 product_index 是否存在
            GetIndexRequest getIndexRequest = new GetIndexRequest("product_index");
            boolean indexExists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            result.put("indexExists", indexExists);
            
            if (!indexExists) {
                result.put("message", "product_index 索引不存在，请点击下方按钮重新同步数据");
                return Result.build(result, ResultCodeEnum.SYSTEM_ERROR);
            }
            
            // 3. 获取索引信息
            result.put("message", "Elasticsearch 连接正常，索引存在");
            result.put("success", true);
            
        } catch (IOException e) {
            result.put("esConnected", false);
            result.put("message", "检查失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Result.build(result, ResultCodeEnum.SUCCESS);
    }

    /**
     * 手动触发数据同步
     */
    @PostMapping("/sync")
    public Result<Map<String, Object>> syncData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("========== 手动触发数据同步 ==========");
            dataSyncService.doSync();
            
            result.put("success", true);
            result.put("message", "数据同步完成，请查看控制台日志");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "同步失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Result.build(result, ResultCodeEnum.SUCCESS);
    }
}
