package com.yitai.product.service;

import com.yitai.model.dto.h5.ProductSkuDto;
import com.yitai.model.vo.h5.SearchResultVo;

public interface SearchService {
    /**
     * 基于 Elasticsearch 的智能搜索
     * @param dto 搜索条件
     * @param page 页码
     * @param limit 每页数量
     * @return 搜索结果
     */
    SearchResultVo search(ProductSkuDto dto, Integer page, Integer limit);
}
