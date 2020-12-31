package com.zyfgoup.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.zyfgoup.handler.ConsumerBlockHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Author Zyfgoup
 * @Date 2020/12/18 14:32
 * @Description
 */
@RestController
public class ConsumerController {

    @Autowired
    RestTemplate restTemplate;

    //要完成的 带http://
    private  final static String PROVIDERSERVICEURL = "http://cloudbase-provider";


    /**
     * 使用restTamplate消费服务、使用sentinal的服务降级方法的
     * @param i
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/get/{i}")
    @SentinelResource(value = "getI",blockHandler = "getI_Handler")
    public String getI(@PathVariable("i") Integer i) throws InterruptedException {

        //Thread.sleep(800); 测试sentinal 降级规则
        //地址 返回值类型 参数在地址后面用数字表示 对应后面的可变长参数
        return String.valueOf(restTemplate.getForObject(PROVIDERSERVICEURL+"/get/{1}",Integer.class,i));
    }

    public String getI_Handler(Integer i,BlockException e){
        return "使用Sentinel实现服务降级";
    }


    /**
     * 使用全局服务降级处理类里的方法
     * @return
     */
    @GetMapping("/test/consumerBlockHandler")
    @SentinelResource(value = "global",blockHandlerClass = ConsumerBlockHandler.class,blockHandler = "handlerException1" )
    public String globalBlockHandler(){
        return "11111";
    }

}
