package com.ralsei.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

import com.ralsei.model.Account;

import io.jsonwebtoken.Claims;
/**
 * Provides the business service contract for jwt.
 */
public interface JwtService {
// Sinh access token
String generateToken(Account account);
// Sinh access token với claims bổ sung
String generateToken(Map<String, Object> extraClaims, Account account);
// Sinh refresh token
String generateRefreshToken(Account account);
// Lấy username (phone) từ token
String extractUsername(String token);
// Lấy 1 claim bất kỳ từ token
<T> T extractClaim(String token, Function<Claims, T> claimsResolver);
// Kiểm tra token có hợp lệ không
boolean isTokenValid(String token, UserDetails userDetails);
// Kiểm tra token có hết hạn chưa
boolean isTokenExpired(String token);
List<String> extractRoles(String token);
Integer extractAccountId(String token);
}