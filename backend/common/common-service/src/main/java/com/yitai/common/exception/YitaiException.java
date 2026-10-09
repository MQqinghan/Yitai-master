package com.yitai.common.exception;

import com.yitai.model.vo.common.ResultCodeEnum;
import lombok.Data;

@Data/*Lombok在编译阶段自动生成getter、setter等方法*/
public class YitaiException extends RuntimeException{
    // 异常状态码（比如201=登录失败、9999=系统错误）
    private Integer code;
    // 异常提示信息（比如"用户名或密码错误"）
    private String message;
    // 关联枚举类：把状态码和信息封装成枚举，避免硬编码
    private ResultCodeEnum resultCodeEnum;/*为了操作方便，加上枚举类*/
    // 3. 构造方法：接收枚举对象，自动赋值code和message
    public YitaiException(ResultCodeEnum resultCodeEnum) {
        this.resultCodeEnum = resultCodeEnum;
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }
}
