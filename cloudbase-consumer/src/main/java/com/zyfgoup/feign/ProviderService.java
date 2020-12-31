package com.zyfgoup.feign;

import com.zyfgoup.feign.fallback.ProviderServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author Zyfgoup
 * @Date 2020/12/28 16:45
 * @Description
 */
@Service
@FeignClient(value = "cloudbase-provider" ,fallback = ProviderServiceFallBack.class)
public interface ProviderService {

    @GetMapping("/get/{i}")
    Integer test(@PathVariable("i") Integer i);
}
