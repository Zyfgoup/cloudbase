package com.zyfgoup.jwt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zyfgoup.entity.Authority;
import com.zyfgoup.entity.Result;
import com.zyfgoup.entity.AuthUser;
import com.zyfgoup.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.StringUtils;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 授权  但是授权在gateway做了 其实这里并没有什么用
 * 除了退出 登录的请求 会走到这个filter里 但是在gateway 把授权做了 所以这个filter其实没有什么作用
 * 但是也是写好了  在单个springboot项目中也是可以用的
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

        //如果token为空
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

    /**
     * 从请求头里获取我们定义的token  构建一个SpringSecurity需要用来验证的token
     * Security会根据这个token里的权限  然后匹配对应的请求资源所需要的权限（使用注解 或者 配置类里配置）
     * @param tokenHeader
     * @return
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String tokenHeader) {
        Claims claim = JwtUtils.getClaimByToken(tokenHeader);
        String userid = claim.getSubject();
        //去redis找是否有  校验是否有效
        String redisToken = redisTemplate.opsForValue().get("JWT" + userid + ":");
        if ("".equals(redisToken) || !redisToken.equals(tokenHeader)) {
            log.error("token不合法，检测不过关");
            throw new AccountExpiredException("Token 无效");
        }
        //校验超时
        if (JwtUtils.isTokenExpired(claim.getExpiration())) {
            throw new AccountExpiredException("Token 已过期");
        }

        if (!StringUtils.isEmpty(userid)) {

            //从 redis拿权限
            String authStr = redisTemplate.opsForValue().get("JWT" + userid + ":" + ":Authorities");
            List<Authority> authorities = JSON.parseArray(authStr , Authority.class);

            //要构建成set
            Set<SimpleGrantedAuthority> authoritiesSet = new HashSet<>();
            authorities.stream().forEach(authority -> authoritiesSet.add(new SimpleGrantedAuthority(authority.getAuthority())));

            AuthUser user = new AuthUser("xxxx", "x123456789",authoritiesSet);
            user.setId(Integer.valueOf(userid));
            return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
        }
        return null;
    }

}
