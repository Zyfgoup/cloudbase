package com.zyfgoup.common;

import java.io.Serializable;

/**
 * @Author Zyfgoup
 * @Date 2020/12/30 20:30
 * @Description
 * 权限类
 */
public class Authority implements Serializable {
    private String authority;

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
