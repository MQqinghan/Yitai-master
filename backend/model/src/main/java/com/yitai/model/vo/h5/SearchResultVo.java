package com.yitai.model.vo.h5;

import com.yitai.model.entity.product.ProductSku;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Elasticsearch搜索结果VO")
public class SearchResultVo {

    @Schema(description = "搜索结果列表")
    private List<ProductSku> list;

    @Schema(description = "总记录数")
    private Long total;
}
