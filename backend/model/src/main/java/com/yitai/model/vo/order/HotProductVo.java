package com.yitai.model.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "热销商品信息")
public class HotProductVo {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品缩略图")
    private String thumbImg;

    @Schema(description = "销售数量")
    private Integer totalSales;

    @Schema(description = "销售总额")
    private BigDecimal totalAmount;

    @Schema(description = "排名")
    private Integer rank;
}
