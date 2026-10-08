package com.ralsei.service;

import com.ralsei.dto.request.auth.CustomerLoginRequest;
import com.ralsei.dto.request.auth.CustomerRegisterRequest;
import com.ralsei.dto.request.auth.RefreshTokenRequest;
import com.ralsei.dto.request.auth.StaffForgotPasswordRequest;
import com.ralsei.dto.request.auth.StaffLoginRequest;
import com.ralsei.dto.response.auth.AuthResponse;
import com.ralsei.dto.response.auth.StaffForgotPasswordResponse;

/**
 * Provides the business service contract for auth.
 */
public interface AuthService {

    AuthResponse customerLogin(CustomerLoginRequest request);

    AuthResponse customerRegister(CustomerRegisterRequest request);

    AuthResponse staffLogin(StaffLoginRequest request);

    StaffForgotPasswordResponse staffForgotPassword(StaffForgotPasswordRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void revokeAllUserTokens(String username);
}
