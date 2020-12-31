package com.zyfgoup.feign.fallback;

import com.zyfgoup.feign.ProviderService;
import org.springframework.stereotype.Component;

/**
 * @Author Zyfgoup
 * @Date 2020/12/28 17:27
 * @Description
 */
@Component
public class ProviderServiceFallBack implements ProviderService {
    @Override
    public Integer test(Integer i) {
        return 0;
    }
}
