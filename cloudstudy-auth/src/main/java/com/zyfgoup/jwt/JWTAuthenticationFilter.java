package com.zyfgoup.jwt;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.Md5Utils;
import com.zyfgoup.common.Result;
import com.zyfgoup.entity.AuthUser;
import com.zyfgoup.utils.JwtUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 */
@Slf4j
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    private StringRedisTemplate redisTemplate;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, StringRedisTemplate redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    @SneakyThrows
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        AuthUser authUser = (AuthUser) authResult.getPrincipal();
        /**
         * 生成token
         */
        JwtUtils jwtUtils = new JwtUtils();
        String jwtToken = jwtUtils.generateToken(authUser.getId());


        //application/json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Authorization",jwtToken);
        //将Authorization在响应首部暴露出来
        response.setHeader("Access-control-Expose-Headers", "Authorization");

        //token
        String key = "JWT" + authUser.getId() + ":";
        //权限
        String authKey = key + ":Authorities";

        //jwtUtils.getExpire()  配置文件配置的过期时间  使用config配置中心 可以动态改
        redisTemplate.opsForValue().set(key,jwtToken,jwtUtils.getExpire(),TimeUnit.SECONDS);

        redisTemplate.opsForValue().set(authKey, JSONObject.toJSONString(authUser.getAuthorities()), jwtUtils.getExpire() , TimeUnit.SECONDS);

        response.getWriter().write(JSONObject.toJSONString(Result.succ(jwtToken)));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.error("登录认证失败",failed);
        Result result = null;
        int status = 401;
        if (failed instanceof UsernameNotFoundException){
            result = Result.fail(404, null,"用户不存在");
        }else if (failed instanceof BadCredentialsException){
            result = Result.fail(401,null, "用户名密码错误");
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(JSONObject.toJSONString(result));
    }
}
