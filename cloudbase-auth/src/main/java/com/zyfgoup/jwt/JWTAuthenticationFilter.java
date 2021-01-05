package com.zyfgoup.jwt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zyfgoup.entity.Result;
import com.zyfgoup.entity.AuthUser;
import com.zyfgoup.entity.User;
import com.zyfgoup.entity.UserVO;
import com.zyfgoup.utils.JwtUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 认证
 */
@Slf4j
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    private StringRedisTemplate redisTemplate;


    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, StringRedisTemplate redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 从请求拿到账号密码 然后走到定义的userDetailsServiceImpl的方法
     * loadUserByUsername中 根据username 去拿到数据库的user 构建成一个实例
     * 然后和token比对  密码使用配置里定义好的加密方式
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)  {
        //将json数据转化为User对象
        User user = jsonToUser(request);

        String username = user.getUsername();
        String password = user.getPassword();
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * 上面方法验证成功后便执行到这里  生成jwttoken 返回即可
     * @param request
     * @param response
     * @param chain
     * @param authResult
     * @throws IOException
     * @throws ServletException
     */
    @SneakyThrows
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        AuthUser authUser = (AuthUser) authResult.getPrincipal();
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(authUser,userVO);
        //生成token 根据User生成
        String jwtToken = JwtUtils.generateToken(JSON.toJSONString(userVO));
        //application/json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Authorization",jwtToken);
        //将Authorization在响应首部暴露出来
        response.setHeader("Access-control-Expose-Headers", "Authorization");

        //token
        String key = "JWT" + userVO.getId() + ":";
        //权限
        String authKey = key + ":Authorities";

        //JwtUtils.getExpire()  配置文件配置的过期时间  使用config配置中心 可以动态改
        redisTemplate.opsForValue().set(key,jwtToken,JwtUtils.getExpire(),TimeUnit.SECONDS);

        redisTemplate.opsForValue().set(authKey, JSONObject.toJSONString(authUser.getAuthorities()), JwtUtils.getExpire() , TimeUnit.SECONDS);

        response.getWriter().write(JSONObject.toJSONString(Result.succ(jwtToken)));
    }


    /**
     * 认证失败 则到这里 根据异常判断是账号不存在 还是密码错误
     * @param request
     * @param response
     * @param failed
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.error("登录失败",failed);
        Result result = null;
        int status = 401;
        if (failed instanceof BadCredentialsException){
            result = Result.fail(401,null, "用户名或者密码不正确");
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(JSONObject.toJSONString(result));
    }


    private User jsonToUser(HttpServletRequest request){
        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jb.append(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        User user = JSON.parseObject(jb.toString(),User.class);
        return user;
    }
}
