package com.ralsei.config;

import com.ralsei.dto.projection.AccountProjection;
import com.ralsei.repository.AccountRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
/**
 * Configures app for the application.
 */
public class AppConfig {

    private final AccountRepository accountRepository;

    /**
     * Cấu hình UserDetailsService để Spring Security có thể lấy thông tin người dùng từ DB
     * @return UserDetailsService
     */
@Bean
/**
 * Executes the user details service operation.
 *
 * @return the operation result
 */
public UserDetailsService userDetailsService() {
    return username -> {
        // 1. Lấy dữ liệu tối ưu từ Projection (Chỉ 1 câu lệnh SQL duy nhất)
        AccountProjection projection = accountRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 2. Kiểm tra trạng thái hoạt động của tài khoản
        if (projection.getIsActive() != null && !projection.getIsActive()) {
            throw new UsernameNotFoundException("Account is inactive: " + username);
        }

        // 3. Bóc tách chuỗi quyền (Ví dụ DB trả về dạng "CUSTOMER,STAFF" hoặc "CUSTOMER")
        List<SimpleGrantedAuthority> authorities = List.of();
        if (projection.getRoleNames() != null && !projection.getRoleNames().isBlank()) {
            authorities = Arrays.stream(projection.getRoleNames().split(","))
                    .map(String::trim)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // 4. Trả về đối tượng User chuẩn của Spring Security, đắp dữ liệu từ Projection sang
        return new User(
                projection.getUsername(),
                projection.getPasswordHash(), // Password đã mã hóa BCrypt dưới DB
                authorities
        );
    };
}

    @Bean
    /**
     * Executes the password encoder operation.
     *
     * @return the operation result
     */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    /**
     * Executes the authentication manager operation.
     *
     * @param config the value supplied for this operation
     *
     * @return the operation result
     *
     * @throws Exception if the operation fails
     */
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public org.springframework.web.client.RestTemplate restTemplate() {
        return new org.springframework.web.client.RestTemplate();
    }
}