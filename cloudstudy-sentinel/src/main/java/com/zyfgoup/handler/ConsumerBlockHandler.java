package com.zyfgoup.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @Author Zyfgoup
 * @Date 2020/12/28 16:30
 * @Description
 * 全局服务降级方法
 */
public class ConsumerBlockHandler {
    public static String handlerException1(BlockException e){
        return "全局服务降级方法1";
    }

    public static String handlerException2(BlockException e){
        return "全局服务降级方法2";
    }
}
