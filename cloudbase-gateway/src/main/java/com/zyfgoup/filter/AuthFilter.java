package com.zyfgoup.filter;

import com.alibaba.fastjson.JSON;
import com.zyfgoup.entity.Authority;
import com.zyfgoup.entity.Result;
import com.zyfgoup.entity.UserVO;
import com.zyfgoup.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @Author Zyfgoup
 * @Date 2020/12/30 19:27
 * @Description
 */
@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {
    AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 白名单  登录注册注销   使用Vue-simple-upload的简单上传合并也放行
     * swagger2的也要放行 无论访问/swagger2-ui.html  还是/doc.html
     *  都是去访问/api/*\/v2api-docs/**
     */
    private static final String[] EXCLUSIONURLS = {"/api/auth/login","/api/auth/register","/api/auth/logout"
    ,"/api/consumer/upload","/api/consumer/merge","/api/*/v2/api-docs/**"};


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        log.info("request path:{}", path);

        //1、判断是否是白名单的路径， 是的话就放行
        boolean isWhite = Arrays.stream(EXCLUSIONURLS).anyMatch(exclusionurl ->
                antPathMatcher.match(exclusionurl, path));
        if (isWhite){
            return chain.filter(exchange);
        }

        //2 没有带上token 或者Token无效
        String headerToken = request.getHeaders().getFirst("Authorization");
        log.info("headerToken:{}", headerToken);
        if ( StringUtils.isEmpty(headerToken) || !verifierToken(headerToken)){
            return getVoidMono(response, 401, "token无效");
        }

        //3、判断请求的URL是否有权限
        boolean permission = hasPermission(headerToken,path);
        if (!permission){
            //gateway不能使用web依赖
            return getVoidMono(response, 403, "无访问权限");
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private boolean verifierToken(String headerToken){
            Claims claim = JwtUtils.getClaimByToken(headerToken);
            if(claim==null){
                return false;
            }
            String jsonUser = claim.getSubject();
            Integer userid = JSON.parseObject(jsonUser,UserVO.class).getId();


            //去redis找是否有  校验是否有效
            String redisToken = redisTemplate.opsForValue().get("JWT"+userid+":");
            if ("".equals(redisToken)||!redisToken.equals(headerToken)) {
                log.error("token不合法，检测不过关");
                return false;
            }
            //校验超时
            if(claim == null || JwtUtils.isTokenExpired(claim.getExpiration())) {
                // token过期了
                log.error("token已经过期");
                return false;
            }
            return true;

    }

    private boolean hasPermission(String headerToken,String path){
        Claims claim = JwtUtils.getClaimByToken(headerToken);
        if(claim==null){
            return false;
        }
        String jsonUser = claim.getSubject();
        Integer userid = JSON.parseObject(jsonUser,UserVO.class).getId();

        if (userid == null || userid == 0){
                return false;
            }

            //构建Key， 把权限放入到redis中
            String key = "JWT" + userid+ ":";
            String authKey = key + ":Authorities";

            //权限
            String authStr = redisTemplate.opsForValue().get(authKey);
            if (StringUtils.isEmpty(authStr)){
                return false;
            }

            //去掉前1个
            String[] str = path.split("/");
            StringBuilder newPath = new StringBuilder("/");
            //从第三位 因为/../../  第一个/前面也是有的 只是为空
            for (int i = 2; i <str.length-1 ; i++) {
                newPath.append(str[i]+"/");

            }
            newPath.append(str[str.length-1]);

            List<Authority> authorities = JSON.parseArray(authStr , Authority.class);
            return authorities.stream().anyMatch(authority -> antPathMatcher.match(authority.getAuthority(), newPath.toString()));

    }

    private Mono<Void> getVoidMono(ServerHttpResponse response, int i, String msg) {
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.setStatusCode(HttpStatus.OK);
        Result failed = Result.fail(i, msg,null);
        byte[] bits = JSON.toJSONString(failed).getBytes();
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        return response.writeWith(Mono.just(buffer));
    }

}
