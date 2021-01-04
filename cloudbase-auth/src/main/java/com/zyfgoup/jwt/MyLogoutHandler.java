package com.zyfgoup.jwt;

import com.alibaba.fastjson.JSON;
import com.zyfgoup.common.Result;
import com.zyfgoup.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author Zyfgoup
 * @Date 2020/12/31 17:06
 * @Description
 */
public class MyLogoutHandler implements LogoutSuccessHandler {

    private StringRedisTemplate redisTemplate;
    private JwtUtils jwtUtils;


    public MyLogoutHandler(RedisTemplate redisTemplate,JwtUtils jwtUtils){
        this.redisTemplate = (StringRedisTemplate) redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 处理退出
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String token = request.getHeader("Authorization");

        Claims claimByToken = jwtUtils.getClaimByToken(token);
        String userid = claimByToken.getSubject();

        //删除redis里的token和权限
        redisTemplate.delete("JWT"+userid+":");
        redisTemplate.delete("JWT"+userid+":"+":Authorities");

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JSON.toJSONString(Result.succ(200,"退出成功",null)));

    }
}
