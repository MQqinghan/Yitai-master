package com.yitai.model.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "用户收藏VO")
public class UserCollectVo {

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "商品skuID")
    private Long skuId;

    @Schema(description = "skuName")
    private String skuName;

    @Schema(description = "缩略图路径")
    private String thumbImg;

    @Schema(description = "售价")
    private BigDecimal salePrice;

    @Schema(description = "市场价")
    private BigDecimal marketPrice;

    @Schema(description = "库存数")
    private Integer stockNum;

}
