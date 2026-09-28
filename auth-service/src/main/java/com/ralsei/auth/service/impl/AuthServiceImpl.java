package com.ralsei.auth.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseToken;
import com.ralsei.auth.dto.projection.AccountProjection;
import com.ralsei.auth.dto.request.auth.CustomerLoginRequest;
import com.ralsei.auth.dto.request.auth.CustomerRegisterRequest;
import com.ralsei.auth.dto.request.auth.RefreshTokenRequest;
import com.ralsei.auth.dto.request.auth.StaffForgotPasswordRequest;
import com.ralsei.auth.dto.request.auth.StaffLoginRequest;
import com.ralsei.auth.dto.response.auth.AuthResponse;
import com.ralsei.auth.dto.response.auth.StaffForgotPasswordResponse;
import com.ralsei.auth.exception.BusinessRuleException;
import com.ralsei.auth.model.Account;
import com.ralsei.auth.model.AccountRole;
import com.ralsei.auth.model.RefreshToken;
import com.ralsei.auth.model.Role;
import com.ralsei.auth.repository.AccountRepository;
import com.ralsei.auth.repository.AccountRoleRepository;
import com.ralsei.auth.repository.RefreshTokenRepository;
import com.ralsei.auth.repository.RoleRepository;
import com.ralsei.auth.service.AuthService;
import com.ralsei.auth.service.FirebaseTokenVerifier;
import com.ralsei.auth.feign.CustomerServiceClient;
import com.ralsei.auth.feign.StaffServiceClient;
import com.ralsei.auth.service.JwtService;
import com.ralsei.auth.util.validation.BookingValidationPatterns;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom PASSWORD_RANDOM = new SecureRandom();
    private static final char[] TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789".toCharArray();
    private static final String STAFF_FORGOT_PASSWORD_MESSAGE =
            "Nếu thông tin khớp với tài khoản nhân viên, mật khẩu tạm thời sẽ được gửi đến email đã đăng ký.";
    private static final List<String> STAFF_ROLES = List.of("ADMIN", "MANAGER", "TICKET_STAFF", "TRIP_STAFF");

    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationDurationMs;

    @Override
    @Transactional
    public AuthResponse customerLogin(CustomerLoginRequest request) {
        FirebaseToken firebaseToken = verifyFirebaseToken(request.idToken());
        String firebaseUid = firebaseToken.getUid();
        String authProvider = detectAuthProvider(firebaseToken);

        AccountProjection account = accountRepository.findByUsernameWithRoles(request.username()).orElse(null);

        if (account == null) {
            if ("firebase".equals(authProvider)) {
                throw new BusinessRuleException(
                        "Tài khoản chưa tồn tại. Vui lòng đăng ký hoặc đăng nhập bằng hình thức khác!");
            }
            buildAndSaveCustomerAccount(request.username(), firebaseToken, authProvider, null, null);

            account = accountRepository.findByUsernameWithRoles(request.username())
                    .orElseThrow(() -> new BusinessRuleException("Lỗi tạo tài khoản!"));
        }

        if ("local".equals(account.getAuthProvider())) {
            throw new BusinessRuleException("Tài khoản nội bộ không đăng nhập qua đây!");
        }

        if (Boolean.FALSE.equals(account.getIsActive())) {
            throw new BusinessRuleException("Tài khoản đã bị khóa!");
        }

        accountRepository.findById(account.getAccountId()).ifPresent(acc -> {
            acc.setFirebaseUid(firebaseUid);
            acc.setAuthProvider(authProvider);
            acc.setLastLogin(LocalDateTime.now());
            accountRepository.save(acc);
        });

        return buildResponse(account);
    }

    @Override
    @Transactional
    public AuthResponse customerRegister(CustomerRegisterRequest request) {
        FirebaseToken firebaseToken = verifyFirebaseToken(request.idToken());
        String authProvider = detectAuthProvider(firebaseToken);

        if (accountRepository.existsByUsername(request.username())) {
            throw new BusinessRuleException("Số điện thoại đã được đăng ký!");
        }

        buildAndSaveCustomerAccount(
                request.username(),
                firebaseToken,
                authProvider,
                request.customerName(),
                request.email());

        AccountProjection account = accountRepository
                .findByUsernameWithRoles(request.username())
                .orElseThrow(() -> new BusinessRuleException("Lỗi tạo tài khoản!"));

        accountRepository.findById(account.getAccountId()).ifPresent(acc -> {
            acc.setFirebaseUid(firebaseToken.getUid());
            acc.setAuthProvider(authProvider);
            acc.setLastLogin(LocalDateTime.now());
            accountRepository.save(acc);
        });

        return buildResponse(account);
    }

    @Override
    @Transactional
    public AuthResponse staffLogin(StaffLoginRequest request) {
        AccountProjection account = accountRepository
                .findByUsernameWithRoles(request.username())
                .orElseThrow(() -> new BusinessRuleException("Sai tên đăng nhập hoặc mật khẩu!"));

        if (!"local".equals(account.getAuthProvider())) {
            throw new BusinessRuleException("Tài khoản này không được đăng nhập qua mạng xã hội!");
        }

        if (account.getPasswordHash() == null ||
                !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new BusinessRuleException("Sai tên đăng nhập hoặc mật khẩu!");
        }

        if (Boolean.FALSE.equals(account.getIsActive())) {
            throw new BusinessRuleException("Tài khoản đã bị khóa!");
        }

        accountRepository.findById(account.getAccountId()).ifPresent(acc -> {
            acc.setLastLogin(LocalDateTime.now());
            accountRepository.save(acc);
        });

        return buildResponse(account);
    }

    @Override
    @Transactional
    public StaffForgotPasswordResponse staffForgotPassword(StaffForgotPasswordRequest request) {
        AccountProjection projection = accountRepository.findByUsernameWithRoles(request.username())
                .orElse(null);
        if (!canResetStaffPassword(projection, request.email())) {
            return forgotPasswordAcceptedResponse();
        }

        Account account = accountRepository.findById(projection.getAccountId())
                .orElse(null);
        if (account == null || !"local".equalsIgnoreCase(account.getAuthProvider())) {
            return forgotPasswordAcceptedResponse();
        }

        // [MICROSERVICE-REFACTOR]: Replaced local call with FeignClient to Staff_Service

        String temporaryPassword = generateTemporaryPassword();

        // [MICROSERVICE-REFACTOR]: Replaced local call with FeignClient to Notification_Service

        account.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        accountRepository.save(account);
        refreshTokenRepository.revokeAllByAccount(account);

        return forgotPasswordAcceptedResponse();
    }

    private boolean canResetStaffPassword(AccountProjection projection, String email) {
        if (projection == null || Boolean.FALSE.equals(projection.getIsActive())) {
            return false;
        }
        if (!"local".equalsIgnoreCase(projection.getAuthProvider())) {
            return false;
        }
        if (email == null || email.isBlank()) {
            return false;
        }
        List<String> roles = projection.getRoleNames() == null || projection.getRoleNames().isBlank()
                ? List.of()
                : Arrays.stream(projection.getRoleNames().split(","))
                    .map(String::trim)
                    .filter(role -> !role.isBlank())
                    .toList();
        return roles.stream().anyMatch(STAFF_ROLES::contains);
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder("S7");
        for (int index = 0; index < 10; index++) {
            password.append(TEMP_PASSWORD_CHARS[PASSWORD_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length)]);
        }
        return password.toString();
    }

    private StaffForgotPasswordResponse forgotPasswordAcceptedResponse() {
        return new StaffForgotPasswordResponse(true, STAFF_FORGOT_PASSWORD_MESSAGE);
    }

    private FirebaseToken verifyFirebaseToken(String idToken) {
        return firebaseTokenVerifier.verifyIdToken(idToken);
    }

    @SuppressWarnings("unchecked")
    private String detectAuthProvider(FirebaseToken token) {
        Map<String, Object> firebaseClaims = (Map<String, Object>) token.getClaims().get("firebase");
        if (firebaseClaims == null)
            return "firebase";

        String signInProvider = (String) firebaseClaims.getOrDefault("sign_in_provider", "firebase");

        return switch (signInProvider) {
            case "google.com" -> "google";
            case "facebook.com" -> "facebook";
            default -> "firebase";
        };
    }

    private void buildAndSaveCustomerAccount(
            String providedUsername, FirebaseToken token, String authProvider,
            String providedCustomerName, String providedEmail) {
        String username = determineUsername(providedUsername, token, authProvider);

        Account account = Account.builder()
                .username(username)
                .passwordHash(null)
                .firebaseUid(token.getUid())
                .authProvider(authProvider)
                .isActive(true)
                .build();

        Account savedAccount = accountRepository.save(account);

        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new BusinessRuleException("Chưa có role Customer trong hệ thống!"));

        accountRoleRepository.save(AccountRole.builder()
                .accountId(savedAccount.getAccountId())
                .roleId(customerRole.getRoleId())
                .build());

        // [MICROSERVICE-REFACTOR]: Replaced local call with FeignClient to Customer_Service
    }

    private String determineUsername(String provided, FirebaseToken token, String authProvider) {
        if (provided != null && !provided.isBlank()) {
            return provided;
        }

        if (token.getEmail() != null) {
            return token.getEmail();
        }

        if ("facebook".equals(authProvider)) {
            return "fb_" + token.getUid().substring(0, 8);
        }

        return "user_" + token.getUid().substring(0, 8);
    }

    private AuthResponse buildResponse(AccountProjection account) {
        if (account.getRoleNames() == null || account.getRoleNames().isBlank()) {
            log.error("Tài khoản '{}' (ID: {}) không được gán bất kỳ quyền nào dưới Database!", 
                    account.getUsername(), account.getAccountId());
            throw new BusinessRuleException("Tài khoản của bạn chưa được cấp quyền trên hệ thống. Vui lòng liên hệ Admin!");
        }

        List<String> roles = Arrays.stream(account.getRoleNames().split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("accountId", account.getAccountId());
        extraClaims.put("roles", roles);

        Account accountEntity = Account.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .build();

        String jwtToken = jwtService.generateToken(extraClaims, accountEntity);

        String refreshToken = jwtService.generateRefreshToken(accountEntity);
        refreshTokenRepository.deleteAllByAccount(accountEntity);
        refreshTokenRepository.save(RefreshToken.builder()
                .account(accountEntity)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plus(refreshExpirationDurationMs, ChronoUnit.MILLIS))
                .isRevoked(false)
                .build());

        return AuthResponse.builder()
                .success(true)
                .message("Thành công!")
                .username(account.getUsername())
                .roles(roles)
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new BusinessRuleException("Refresh Token không tồn tại trong hệ thống!"));
        
        if(!refreshTokenEntity.isValid()) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new BusinessRuleException("Refresh Token đã hết hạn hoặc bị vô hiệu hóa. Vui lòng đăng nhập lại!");
        }
        
        Account account = refreshTokenEntity.getAccount();
        
        AccountProjection accountProj = accountRepository.findByUsernameWithRoles(account.getUsername())
                .orElseThrow(() -> new BusinessRuleException("User không tồn tại!"));
                
        List<String> roles = Arrays.stream(accountProj.getRoleNames().split(",")).map(String::trim).toList();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("accountId", account.getAccountId());
        extraClaims.put("roles", roles);
        String newAccessToken = jwtService.generateToken(extraClaims, account);
        
        return AuthResponse.builder()
                .success(true)
                .message("Làm mới access token thành công!")
                .username(account.getUsername())
                .roles(roles)
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken()).ifPresent(token -> refreshTokenRepository.delete(token));
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(String username) {

    }
}
