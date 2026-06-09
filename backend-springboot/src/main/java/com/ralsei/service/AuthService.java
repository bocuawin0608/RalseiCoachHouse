package com.ralsei.service;

import com.ralsei.dto.request.auth.CustomerLoginRequest;
import com.ralsei.dto.request.auth.CustomerRegisterRequest;
import com.ralsei.dto.request.auth.StaffLoginRequest;
import com.ralsei.dto.response.auth.AuthResponse;

public interface AuthService {

    // Customer: form login / Google / Facebook - tất cả đều gửi idToken
    AuthResponse customerLogin(CustomerLoginRequest request);

    // Customer: đăng ký lần đầu qua form (phone + password qua Firebase)
    AuthResponse customerRegister(CustomerRegisterRequest request);

    // Staff: đăng nhập bằng username + password nội bộ
    AuthResponse staffLogin(StaffLoginRequest request);
}