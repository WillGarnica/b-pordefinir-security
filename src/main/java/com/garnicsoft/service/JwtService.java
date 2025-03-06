package com.garnicsoft.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final String secretKey;

  public JwtService(@Value("${spring.jwt.secret.key}") String secretKey) {
    this.secretKey = secretKey;
  }

  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(String email, Map<String, Object> claims, Date expirationDate) {
    claims = claims == null ? Map.of() : claims;
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(expirationDate)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(String email, Map<String, Object> claims) {
    return generateToken(
        email, claims, new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30))); // 30 days
  }

  public String generateAuthToken(String email, Map<String, Object> claims) {
    return generateToken(
        email, claims, new Date(System.currentTimeMillis() + (1000 * 60 * 60))); // 1 hour
  }

  public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public boolean isTokenValid(String token, String userEmail) {
    return extractEmail(token).equals(userEmail) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractClaim(token, Claims::getExpiration).before(new Date());
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    Claims claims =
        Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    return claimsResolver.apply(claims);
  }
}
