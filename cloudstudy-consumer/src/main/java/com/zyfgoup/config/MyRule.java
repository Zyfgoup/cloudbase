package com.zyfgoup.config;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author Zyfgoup
 * @Date 2020/12/29 17:28
 * @Description
 */
@Component
public class MyRule  extends AbstractLoadBalancerRule implements IRule {
    @Override
    public Server choose(Object o) {
        List<Server> allList = this.getLoadBalancer().getAllServers();
        for (int i = 0; i < allList.size(); i++) {
            System.out.println(allList.get(i).getPort());
        }
        return allList.get(0);
    }



    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }
}
