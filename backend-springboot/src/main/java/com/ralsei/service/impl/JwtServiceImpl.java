package com.ralsei.service.impl;

import com.ralsei.model.Account;
import com.ralsei.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JwtService {

    @Override
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        // Lấy list thô ra trước
        List<?> rawRoles = claims.get("roles", List.class);

        if (rawRoles == null) {
            return List.of();
        }

        // Ép kiểu an toàn bằng Stream để tránh lỗi Unchecked Conversion
        return rawRoles.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration; // milliseconds

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    @Override
    public String generateToken(Account account) {
        return generateToken(new HashMap<>(), account);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, Account account) {
        // Nếu anh đã lấy sẵn Roles từ trước và map vào UserDetails / GrantedAuthorities
        // (Giả sử Account của anh implement UserDetails hoặc anh có một DTO riêng)
        // List<String> roles = account.getAuthorities().stream()
        // .map(GrantedAuthority::getAuthority)
        // .collect(Collectors.toList());

        extraClaims.put("accountId", account.getAccountId());
        // extraClaims.put("roles", roles); // <--- Nhét danh sách thật vào đây

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(account.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(Account account) {
        // Có thể triển khai tương tự, lưu vào DB sau
        return Jwts.builder()
                .setSubject(account.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}