package com.zyfgoup.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Zyfgoup
 * @Date 2020/12/18 14:29
 * @Description
 */
@RestController
public class ProviderController {

    @GetMapping("/get/{i}")
    public Integer test(@PathVariable("i") Integer i){
        System.out.println("11111111111");
        int res = 10/0;
        return i;
    }
}
