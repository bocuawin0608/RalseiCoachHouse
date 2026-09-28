package com.ralsei.customer.security;

import java.util.List;

public interface JwtService {
    String extractUsername(String token);
    boolean isTokenExpired(String token);
    List<String> extractRoles(String token);
    Integer extractAccountId(String token);
}
