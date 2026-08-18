package com.inventory.officeInventory.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key = Keys.hmacShaKeyFor(
            "my-secret-key-for-office-inventory-management-123456"
                    .getBytes()
    );

    private final long expiration = 1000 * 60 * 60; // 1 hour

    public String generateToken(Authentication authentication) {

        return Jwts.builder()
                .subject(authentication.getName())
                .claim(
                        "role",
                        authentication.getAuthorities()
                                .stream()
                                .findFirst()
                                .map(Object::toString)
                                .orElse("")
                )
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + expiration)
                )
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {

        return getClaims(token)
                .getSubject();
    }

    public boolean isTokenValid(String token) {

        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}