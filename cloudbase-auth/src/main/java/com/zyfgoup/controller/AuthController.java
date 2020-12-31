package com.zyfgoup.controller;

import com.zyfgoup.entity.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Zyfgoup
 * @Date 2020/12/30 22:11
 * @Description
 */
@RestController
public class AuthController {

    @PostMapping("/auth/test")
    public String get(@RequestBody User user){
        return user.getPassword();
    }
}
