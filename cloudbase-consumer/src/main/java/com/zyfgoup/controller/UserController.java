package com.zyfgoup.controller;

import com.zyfgoup.entity.Result;
import com.zyfgoup.entity.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author Zyfgoup
 * @Date 2021/1/13 14:11
 * @Description
 */
@RestController
@RequestMapping("/consumer")
public class UserController {


    @GetMapping("/user/info")
    public Result getInfo(HttpServletRequest request){
        //应该根据token拿用户信息
        //String authorization = request.getHeader("Authorization");

        String[] roles = {"admin"};
        //这里写死直接返回了
        UserVO super_admin = UserVO.builder().name("Super Admin").avatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
                .roles(roles).id(10000).build();
        return Result.succ(super_admin);
    }
}
