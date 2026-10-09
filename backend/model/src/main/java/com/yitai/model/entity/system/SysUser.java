package com.yitai.model.entity.system;

import com.yitai.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Data;

@Data
@Schema(description = "系统用户实体类")
public class SysUser extends BaseEntity {

	@Schema(description = "用户名")
	@Column(name = "username")
	private String userName;

	@Schema(description = "密码")
	private String password;

	// 核心新增：密码盐值字段
	@Schema(description = "密码盐值（用于盐值加密）")
	private String salt;

	@Schema(description = "昵称")
	private String name;

	@Schema(description = "手机号码")
	private String phone;

	@Schema(description = "图像")
	private String avatar;

	@Schema(description = "描述")
	private String description;

	@Schema(description = "状态（1：正常 0：停用）")
	private Integer status;

}
