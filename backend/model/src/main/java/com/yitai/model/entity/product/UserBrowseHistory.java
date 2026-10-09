package com.yitai.model.entity.product;

import com.yitai.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户浏览历史")
public class UserBrowseHistory extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商品skuID")
    private Long skuId;

    @Schema(description = "浏览次数")
    private Integer browseCount;

}
