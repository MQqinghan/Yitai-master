package com.yitai.common.exception;

import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice/*这个注解是Controller增强器，给Controller增加统一的操作和处理*/
public class GlobalExceptionHandler {

    // 全局异常处理（捕获所有未定义的异常）
    /*@ExceptionHandler这个注解可以 指定捕获Controller抛出的异常的类型*/
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e) {
        e.printStackTrace(); // 保留打印堆栈（关键，后端控制台能看到具体错误）
        // 👇 修改点1：不再返回固定的 SYSTEM_ERROR，而是拼接真实异常信息
        return Result.build(null, ResultCodeEnum.SYSTEM_ERROR.getCode(),
                "登录失败：" + e.getMessage());
    }

    // 自定义异常处理（捕获 YitaiException）
    @ExceptionHandler(YitaiException.class)
    @ResponseBody
    public Result error(YitaiException e) {
        // 修改点2：补充打印自定义异常日志（可选，但方便排查）
        e.printStackTrace();
        return Result.build(null, e.getResultCodeEnum());
    }
}
