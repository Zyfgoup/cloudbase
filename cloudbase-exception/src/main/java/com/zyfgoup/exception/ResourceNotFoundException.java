package com.zyfgoup.exception;


import javax.servlet.ServletException;

/**
 * @Author Zyfgoup
 * @Date 2020/12/1 11:12
 * @Description
 */
public class ResourceNotFoundException extends BaseException{

    //构建一个异常类只需要传入一个简单的数据即可  异常码是写死的了
    public ResourceNotFoundException(){
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
