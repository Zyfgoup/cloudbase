package com.zyfgoup.controller;

import com.zyfgoup.feign.ProviderService;
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
public class ConsumerFeignController {

    @Value("${didispace.title}")
    String i;

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
    public String test3(){
        return zyf;
    }
}
