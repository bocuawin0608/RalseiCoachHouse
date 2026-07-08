package com.ralsei.service;

import com.ralsei.dto.request.auth.CustomerLoginRequest;
import com.ralsei.dto.request.auth.CustomerRegisterRequest;
import com.ralsei.dto.request.auth.RefreshTokenRequest;
import com.ralsei.dto.request.auth.StaffForgotPasswordRequest;
import com.ralsei.dto.request.auth.StaffLoginRequest;
import com.ralsei.dto.response.auth.AuthResponse;
import com.ralsei.dto.response.auth.StaffForgotPasswordResponse;

public interface AuthService {

    // Customer: form login / Google / Facebook với firebase
    AuthResponse customerLogin(CustomerLoginRequest request);

    // Customer: đăng ký lần đầu qua phone xác thực OTP với firebase
    AuthResponse customerRegister(CustomerRegisterRequest request);

    // Staff: đăng nhập bằng username + password nội bộ
    AuthResponse staffLogin(StaffLoginRequest request);

    /**
     * Resets a local staff password after matching username and staff email.
     *
     * @param request staff username and profile email from the login page
     * @return generic response that does not reveal whether the account exists
     */
    StaffForgotPasswordResponse staffForgotPassword(StaffForgotPasswordRequest request);
    
    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    // Vô hiệu hóa tất cả token khi đổi pass
    void revokeAllUserTokens(String username);
}
