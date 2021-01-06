package com.zyfgoup.controller;

import com.zyfgoup.exception.ResourceNotFoundException;
import com.zyfgoup.feign.ProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Zyfgoup
 * @Date 2020/12/28 16:47
 * @Description
 */
@RestController
@RequestMapping("/consumer")
@Slf4j
public class ConsumerFeignController {

    //测试配置中心
   @Value("${didispace.title}")
    String i;

    //测试配置文件共享
    @Value("${spring.zyf}")
    String zyf;

    @Autowired
    ProviderService providerService;

    @GetMapping("/feign/get/{i}")
    public Integer test(@PathVariable("i") Integer i){
       return  providerService.test(i);
    }

    @PostMapping("/testgateway/post")
    public Integer test1(){
        return 1;
    }

    @GetMapping("/get/testconfig")
    public String test2(){
        return i;
    }

    @GetMapping("/test/get/testconfig1")
    public String test3() throws ResourceNotFoundException {
        log.info(zyf);
        //抛出异常 测试全局异常处理
        throw new ResourceNotFoundException();
        //return zyf;
    }
}
