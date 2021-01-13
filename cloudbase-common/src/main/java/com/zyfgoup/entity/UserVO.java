package com.zyfgoup.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Zyfgoup
 * @Date 2021/1/5 18:52
 * @Description 在前端展示的用户信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    private Integer id;
    private String name;
    private String[] roles;
    private String avatar;

}
