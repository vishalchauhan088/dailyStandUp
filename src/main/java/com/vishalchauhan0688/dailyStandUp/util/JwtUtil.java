package com.vishalchauhan0688.dailyStandUp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.expiration}")
    private long expiration;

    private SecretKey getKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims parseToken(String token){
        return Jwts.parser()
                .verifyWith(this.getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public String extractEmail(String token){
        return this.parseToken(token).getSubject();
    }
    public Set<String> extractRoles(String token){
        Object role = parseToken(token).get("roles");
        if(role instanceof List<?> roles ){
            return roles.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    public String generateToken(String email, List<String> roles) {
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+ expiration) )
                .signWith(this.getKey())
                .compact();


    }

    public boolean validateToken(String token){
        try{
            this.parseToken(token);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}
