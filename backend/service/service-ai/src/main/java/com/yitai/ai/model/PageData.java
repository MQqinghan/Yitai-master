package com.yitai.ai.model;

import com.yitai.model.entity.product.ProductSku;
import lombok.Data;

import java.util.List;

/**
 * 商品分页结果（对应 PageHelper PageInfo 的常用字段）。
 */
@Data
public class PageData {
    private long total;
    private int pageNum;
    private int pages;
    private List<ProductSku> list;
}
