package com.mentoredu.auth.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email) {
        // keep using jjwt for generation (should exist)
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = parts[1];
            // base64url decode
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            ObjectMapper mapper = new ObjectMapper();
            Map<?,?> claims = mapper.readValue(decoded, Map.class);
            Object sub = claims.get("sub");
            if (sub == null) sub = claims.get("subject");
            return sub == null ? null : sub.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;
            String payload = parts[1];
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            ObjectMapper mapper = new ObjectMapper();
            Map<?,?> claims = mapper.readValue(decoded, Map.class);
            Object expObj = claims.get("exp");
            if (expObj == null) return true; // no exp claim -> treat as valid
            long exp;
            if (expObj instanceof Number) {
                exp = ((Number) expObj).longValue();
            } else {
                exp = Long.parseLong(expObj.toString());
            }
            // exp is seconds since epoch in JWT spec
            long nowSecs = System.currentTimeMillis() / 1000L;
            return exp > nowSecs;
        } catch (Exception e) {
            return false;
        }
    }
}
