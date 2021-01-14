package com.zyfgoup.controller;

import com.zyfgoup.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Zyfgoup
 * @Date 2020/12/30 22:11
 * @Description
 */
@RestController
@Api(tags = "auth")
public class AuthController {

    @Autowired
    PasswordEncoder passwordEncoder;


    /**
     *  PreAuthorize("hasAuthority('.....')")
     *  根据括号里面的权限来判断 (这个没有默认前缀)
     *  根据方法里的源码可以发现 角色和权限只能二选一  如果都放进Set集合里 只要有一个匹配的就返回true了
     *  我这里写demo 就都放进去了
     *
     *
     *  也可以用下面的hasRole 这种只能是判断角色  且权限里需要使用ROLE_这样的前缀
     * @param user
     * @return
     */
    @PostMapping("/consumer/test/123")
    @PreAuthorize("hasAuthority('/consumer/test/get/**')")
    public String get(@RequestBody User user){
        return user.getPassword();
    }

    /**
     * 模拟注册
     * @param user
     * @return
     */
    @ApiOperation(value = "auth-注册",notes = "注册")
    @PostMapping("/register")
    public String register(@RequestBody User user){
        //模拟注册
        //加密密码
        String encodePass = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePass);

        //实际项目开发中 返回Result
        return user.getPassword();
    }
}
