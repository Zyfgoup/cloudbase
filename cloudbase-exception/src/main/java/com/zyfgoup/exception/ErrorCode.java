package com.zyfgoup.exception;


import org.springframework.http.HttpStatus;

/**
 * @Author Zyfgoup
 * @Date 2020/12/1 10:47
 * @Description
 * 异常处理的核心类 自定义的异常码
 * 使用枚举
 */

public enum ErrorCode {


    //枚举的类型之间用逗号隔开
    //定义很多很多的异常码
    RESOURCE_NOT_FOUND(1001,HttpStatus.BAD_REQUEST,"未找到该资源"),
    REQUEST_VALIDATION_FAILED(1002,HttpStatus.BAD_REQUEST,"请求数据格式失败");

    private final int code;
    private final HttpStatus status;
    private final String message;


    ErrorCode(int code, HttpStatus status, String message){
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                "code=" + code +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
