package com.Aniket.billing.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            "this-is-a-very-long-secret-key-for-hs256-signing-1234".getBytes()
    );

    public String generateToken(String username, String tenantId , String role){
        long EXPIRATION_MS = 1000 * 60 * 60 * 10;
        return Jwts.builder()
                .subject(username)
                .claim("tenantId", tenantId)
                .claim("role",role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+ EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    public io.jsonwebtoken.Claims extractClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
