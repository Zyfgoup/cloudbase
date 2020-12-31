package com.zyfgoup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Author Zyfgoup
 * @Date 2020/12/18 14:40
 * @Description
 */
@Configuration
public class RibbonConfig {

    @Bean
    //@LoadBalanced
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
