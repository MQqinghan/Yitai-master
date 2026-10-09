package com.yitai.model.entity.product;

import com.yitai.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "ProductSku")
public class ProductSku extends BaseEntity {

	@Schema(description = "商品编号")
	private String skuCode;

	@Schema(description = "skuName")
	private String skuName;

	@Schema(description = "商品ID")
	private Long productId;

	@Schema(description = "缩略图路径")
	private String thumbImg;

	@Schema(description = "售价")
	private BigDecimal salePrice;

	@Schema(description = "市场价")
	private BigDecimal marketPrice;

	@Schema(description = "成本价")
	private BigDecimal costPrice;

	@Schema(description = "库存数")
	private Integer stockNum;

	@Schema(description = "销量")
	private Integer saleNum;

	@Schema(description = "sku规格信息json")
	private String skuSpec;

	@Schema(description = "重量")
	private String weight;

	@Schema(description = "体积")
	private String volume;

	@Schema(description = "线上状态：0-初始值，1-上架，-1-自主下架")
	private Integer status;

	// ==================== 以下为搜索相关字段（非数据库字段） ====================

	@Schema(description = "商品名称（关联product表）")
	private String productName;

	@Schema(description = "品牌名称（关联brand表）")
	private String brandName;

	@Schema(description = "一级分类ID")
	private Long category1Id;

	@Schema(description = "二级分类ID")
	private Long category2Id;

	@Schema(description = "三级分类ID")
	private Long category3Id;

	@Schema(description = "品牌ID")
	private Long brandId;

}