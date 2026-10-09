package com.yitai.model.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "品类销售统计")
public class CategorySalesVo {

    @Schema(description = "三级类目ID")
    private Long categoryId;

    @Schema(description = "三级类目名称")
    private String categoryName;

    @Schema(description = "销售数量")
    private Integer totalSales;

    @Schema(description = "销售总额")
    private BigDecimal totalAmount;

    @Schema(description = "销售占比（百分比）")
    private Double salesRatio;
}
