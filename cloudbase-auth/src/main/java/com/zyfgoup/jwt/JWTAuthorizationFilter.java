package com.zyfgoup.jwt;

import com.alibaba.fastjson.JSONObject;
import com.zyfgoup.common.Result;
import com.zyfgoup.entity.AuthUser;
import com.zyfgoup.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**<p>授权</p>
 * @author Mr.Yangxiufeng
 * @date 2020-10-27
 * @time 19:23
 */
@Slf4j
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private StringRedisTemplate redisTemplate;


    public JWTAuthorizationFilter(AuthenticationManager authenticationManager,StringRedisTemplate redisTemplate) {
        super(authenticationManager);
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)){
            chain.doFilter(request,response);
            return;
        }
        try {
            Authentication authentication = getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            onSuccessfulAuthentication(request , response , authentication);
            chain.doFilter(request,response);
        } catch (Exception e) {
            e.printStackTrace();
            onUnsuccessfulAuthentication(request, response , new AccountExpiredException(e.getMessage()));
        }
    }

    @Override
    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
        log.info("Token 验证成功");
    }

    @Override
    protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.error("token校验失败");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result result = Result.fail(HttpServletResponse.SC_UNAUTHORIZED , null,failed.getMessage());
        response.getWriter().write(JSONObject.toJSONString(result));
    }

    // 这里从token中获取用户信息并新建一个token
    private UsernamePasswordAuthenticationToken getAuthentication(String tokenHeader) {
        JwtUtils jwtUtils = new JwtUtils();
        Claims claim = jwtUtils.getClaimByToken(tokenHeader);
        String userid = claim.getSubject();
        //去redis找是否有  校验是否有效
        String redisToken = (String)redisTemplate.opsForValue().get("JWT"+userid+":");
        if ("".equals(redisToken)||!redisToken.equals(tokenHeader)) {
            log.error("token不合法，检测不过关");
            throw new AccountExpiredException("Token 无效");
        }
        //校验超时
        if(claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {
            throw new AccountExpiredException("Token 已过期");
        }
        if(userid == String.valueOf(10000)){
            Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();

            //模拟获取角色
            SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_"+"admin");
            grantedAuthorities.add(grantedAuthority);

            //模拟获取对应的url权限
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("/test/get/**");
            grantedAuthorities.add(authority);
            AuthUser user  = new AuthUser("zouyongfa","YONG1653823..",grantedAuthorities);
            user.setId(10000);
            return new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());
        }

        return null;
    }
}
