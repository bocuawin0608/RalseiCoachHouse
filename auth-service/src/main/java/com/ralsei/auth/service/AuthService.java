package com.ralsei.auth.service;

import com.ralsei.auth.dto.request.auth.CustomerLoginRequest;
import com.ralsei.auth.dto.request.auth.CustomerRegisterRequest;
import com.ralsei.auth.dto.request.auth.RefreshTokenRequest;
import com.ralsei.auth.dto.request.auth.StaffForgotPasswordRequest;
import com.ralsei.auth.dto.request.auth.StaffLoginRequest;
import com.ralsei.auth.dto.response.auth.AuthResponse;
import com.ralsei.auth.dto.response.auth.StaffForgotPasswordResponse;

public interface AuthService {

    AuthResponse customerLogin(CustomerLoginRequest request);

    AuthResponse customerRegister(CustomerRegisterRequest request);

    AuthResponse staffLogin(StaffLoginRequest request);

    StaffForgotPasswordResponse staffForgotPassword(StaffForgotPasswordRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void revokeAllUserTokens(String username);
}
