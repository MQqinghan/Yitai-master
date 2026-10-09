package com.yitai.model.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "热销商品统计搜索条件")
public class HotProductDto {

    @Schema(description = "开始时间")
    private String createTimeBegin;

    @Schema(description = "结束时间")
    private String createTimeEnd;

    @Schema(description = "排行榜数量，默认10")
    private Integer limit = 10;
}
