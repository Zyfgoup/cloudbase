package com.zyfgoup.exception;

import org.springframework.http.HttpStatus;

/**
 * @Author Zyfgoup
 * @Date 2021/1/4 15:17
 * @Description
 */
public class BaseException extends Exception {
    private ErrorCode error;

    public BaseException(ErrorCode error){
        super(error.getMessage());
        this.error = error;
    }

    protected BaseException(ErrorCode error,Throwable cause){
        super(error.getMessage(),cause);
        this.error = error;

    }
    public int getErrorCode() {
        return error.getCode();
    }

    public String getErrorMsg(){
        return error.getMessage();
    }

    public HttpStatus getStatus(){
        return error.getStatus();
    }


}
