package com.Aniket.billing.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JwtAuthFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if(path.startsWith("/auth") || path.startsWith("/actuator")
                || path.equals("/tenants") || path.matches("/tenants/[^/]+"))
        {
            filterChain.doFilter(request,response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.extractClaims(token);
            request.setAttribute("username", claims.getSubject());
            request.setAttribute("tenantId", claims.get("tenantId", String.class));
            request.setAttribute("role", claims.get("role", String.class));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid or expired token");
            return;
        }
        filterChain.doFilter(request,response);
    }
}
