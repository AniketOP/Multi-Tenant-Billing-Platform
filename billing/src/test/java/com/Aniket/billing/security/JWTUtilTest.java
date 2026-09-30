package com.Aniket.billing.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JWTUtilTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long!!";
    private static final String OTHER_SECRET = "a-completely-different-secret-key-32-bytes-min!!";

    private final JWTUtil jwtUtil = new JWTUtil(SECRET);

    @Test
    void generateThenExtract_roundTripsSubjectTenantAndRole() {
        String token = jwtUtil.generateToken("rahul_admin", "tenant::t1", "ADMIN");

        Claims claims = jwtUtil.extractClaims(token);

        assertEquals("rahul_admin", claims.getSubject());
        assertEquals("tenant::t1", claims.get("tenantId", String.class));
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    void generateToken_expiresAboutTenHoursAfterIssue() {
        Claims claims = jwtUtil.extractClaims(jwtUtil.generateToken("u", "tenant::t1", "ADMIN"));

        long diffMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        assertTrue(diffMs >= 36_000_000L - 1500 && diffMs <= 36_000_000L + 1500,
                "expected ~10h, got " + diffMs + " ms");
    }

    @Test
    void extractClaims_tokenSignedWithDifferentSecret_throws() {
        String forged = new JWTUtil(OTHER_SECRET).generateToken("u", "tenant::t1", "ADMIN");

        assertThrows(JwtException.class, () -> jwtUtil.extractClaims(forged));
    }

    @Test
    void extractClaims_expiredToken_throwsExpired() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        String expired = Jwts.builder()
                .subject("u")
                .claim("tenantId", "tenant::t1")
                .claim("role", "ADMIN")
                .issuedAt(new Date(System.currentTimeMillis() - 2 * 3600_000L))
                .expiration(new Date(System.currentTimeMillis() - 3600_000L))
                .signWith(key)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.extractClaims(expired));
    }

    @Test
    void extractClaims_garbage_throws() {
        assertThrows(JwtException.class, () -> jwtUtil.extractClaims("not.a.jwt"));
        assertThrows(Exception.class, () -> jwtUtil.extractClaims(""));
    }

    @Test
    void extractClaims_payloadSwappedToAnotherTenant_throws() {
        String t1 = jwtUtil.generateToken("u", "tenant::t1", "ADMIN");
        String t2 = jwtUtil.generateToken("u", "tenant::t2", "ADMIN");
        String[] a = t1.split("\\.");
        String[] b = t2.split("\\.");
        String tampered = a[0] + "." + b[1] + "." + a[2]; // t2's claims, t1's signature

        assertThrows(JwtException.class, () -> jwtUtil.extractClaims(tampered));
    }
}