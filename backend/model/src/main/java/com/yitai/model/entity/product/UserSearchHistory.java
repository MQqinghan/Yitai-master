package com.yitai.model.entity.product;

import com.yitai.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户搜索历史")
public class UserSearchHistory extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "搜索次数")
    private Integer searchCount;

}
