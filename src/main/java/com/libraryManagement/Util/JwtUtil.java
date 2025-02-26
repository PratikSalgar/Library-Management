package com.libraryManagement.Util;

import com.libraryManagement.service.CustomUserDetail;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil
{
    @Value("${jwt.secret}")
    private String SecretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey generateKey()
    {
        byte[] decode = Base64.getDecoder().decode(SecretKey);
        return Keys.hmacShaKeyFor(decode);
    }

    public String extractUsername(String token)
    {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token)
    {
        return extractAllClaims(token).get("role", String.class);
    }

    public Date extractExpiration(String token)
    {
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean isTokenExpired(String token)
    {
        return extractExpiration(token).getTime() < System.currentTimeMillis();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver)
    {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token)
    {
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(CustomUserDetail customUserDetail)
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", customUserDetail.getRole().name());
        return createToken(claims, customUserDetail.getUsername());
    }

    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(generateKey())
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails)
    {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
