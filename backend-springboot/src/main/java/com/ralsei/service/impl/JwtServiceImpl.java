package com.ralsei.service.impl;

import com.ralsei.model.Account;
import com.ralsei.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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
        // extraClaims lúc này ĐÃ có sẵn key "roles" được nhét từ AuthServiceImpl rồi.
        // Chỉ cần bổ sung thêm accountId nếu cần (mặc dù AuthServiceImpl cũng đã nhét
        // nó rồi,
        // nhưng để chắc chắn thì giữ lại dòng này cũng không sao).
        extraClaims.putIfAbsent("accountId", account.getAccountId());

        // Đừng tự tạo roles giả định ở đây nữa! Xóa các dòng gây lỗi đi.

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
        // Đọc chuỗi text thường trực tiếp bằng UTF-8, không decode Base64 bậy bạ nữa!
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
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