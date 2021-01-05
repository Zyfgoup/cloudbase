package com.zyfgoup.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * jwt工具类
 */
public class JwtUtils {

    private static Logger log = LogManager.getLogger(JwtUtils.class);
    private static final String SECRET = "f4e2e52034348f86b67cde581c0f9eb5";
    private static final long EXPIRE = 3600;
    private static final String HEADER = "Authorization";

    /**
     * 生成jwt token
     */
    public static String generateToken(String jsonUser) {
        Date nowDate = new Date();
        //过期时间
        Date expireDate = new Date(nowDate.getTime() + EXPIRE * 1000);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(jsonUser)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    public static  Claims getClaimByToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (Exception e){
            log.debug("解析token失败",e);
            return null;
        }
    }

    /**
     * token是否过期
     * @return  true：过期
     */
    public static  boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    public static long getExpire(){
        return EXPIRE;
    }
}
