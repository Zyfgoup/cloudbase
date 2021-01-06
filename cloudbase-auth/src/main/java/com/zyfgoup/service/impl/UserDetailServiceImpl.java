package com.zyfgoup.service.impl;

import com.zyfgoup.entity.AuthUser;
import com.zyfgoup.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author Zyfgoup
 * @Date 2020/12/30 18:17
 * @Description
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //模拟是否有该用户
        if(!"xxxx".equals(username)){
            throw new UsernameNotFoundException("用户名不存在");
        }

        //模拟从数据库获取对应用户的对象
        String enPass = passwordEncoder.encode("a123456789");
        User user = new User(username,enPass);

        Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();

        //但其实只能二选一 要么是角色要么是资源uri  在gateway里进行匹配的时候 跟匹配uri的
        //这里都放进去只是模拟

        //模拟获取角色
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_"+"ADMIN");
        grantedAuthorities.add(grantedAuthority);

        //模拟获取对应的url权限
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("/consumer/test/get/**");
        grantedAuthorities.add(authority);

        AuthUser authUser = new AuthUser(user.getUsername(), user.getPassword(), grantedAuthorities);

        authUser.setId(10000);
        return authUser;
    }
}
