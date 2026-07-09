package com.ralsei.service.impl;

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
import com.ralsei.dto.projection.AccountProjection;
import com.ralsei.dto.request.auth.CustomerLoginRequest;
import com.ralsei.dto.request.auth.CustomerRegisterRequest;
import com.ralsei.dto.request.auth.RefreshTokenRequest;
import com.ralsei.dto.request.auth.StaffForgotPasswordRequest;
import com.ralsei.dto.request.auth.StaffLoginRequest;
import com.ralsei.dto.response.auth.AuthResponse;
import com.ralsei.dto.response.auth.StaffForgotPasswordResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.model.Account;
import com.ralsei.model.AccountRole;
import com.ralsei.model.Customer;
import com.ralsei.model.RefreshToken;
import com.ralsei.model.Role;
import com.ralsei.model.Staff;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.AccountRoleRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.RefreshTokenRepository;
import com.ralsei.repository.RoleRepository;
import com.ralsei.repository.StaffRepository;
import com.ralsei.service.AuthService;
import com.ralsei.service.FirebaseTokenVerifier;
import com.ralsei.service.JwtService;
import com.ralsei.util.EmailUtility;

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
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final EmailUtility emailUtility;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationDurationMs;

    /**
     * LOGIN cho customer:
     * với google/facebook (ko cần register)
     * với phone auth (phải register mới login đc)
     */
    @Override
    @Transactional
    public AuthResponse customerLogin(CustomerLoginRequest request) {
        FirebaseToken firebaseToken = verifyFirebaseToken(request.idToken());
        String firebaseUid = firebaseToken.getUid();
        String authProvider = detectAuthProvider(firebaseToken);

        AccountProjection account = accountRepository.findByUsernameWithRoles(request.username()).orElse(null);

        // Social login lần đầu -> tự động tạo tài khoản mới (aka register)
        if (account == null) {
            if ("firebase".equals(authProvider)) {
                throw new BusinessRuleException(
                        "Tài khoản chưa tồn tại. Vui lòng đăng ký hoặc đăng nhập bằng hình thức khác!");
            }
            // Google hoặc Facebook
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

    /**
     * REGISTER cho customer: chỉ Phone Auth firebase cần
     */
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

        // hỗ trợ case đăng ký phát thì đăng nhập đc luôn
        accountRepository.findById(account.getAccountId()).ifPresent(acc -> {
            acc.setFirebaseUid(firebaseToken.getUid());
            acc.setAuthProvider(authProvider);
            acc.setLastLogin(LocalDateTime.now());
            accountRepository.save(acc);
        });

        return buildResponse(account);
    }

    /**
     * STAFF: chỉ áp dụng local login (ko dùng firebase)
     */
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

    /**
     * Resets a local staff account password when username and staff email match.
     * Unknown or mismatched accounts return the same accepted response to avoid
     * leaking staff account existence from the public login page.
     */
    @Override
    @Transactional
    public StaffForgotPasswordResponse staffForgotPassword(StaffForgotPasswordRequest request) {
        AccountProjection projection = accountRepository.findByUsernameWithRoles(request.username().trim())
                .orElse(null);
        if (!canResetStaffPassword(projection, request.email())) {
            return forgotPasswordAcceptedResponse();
        }

        Account account = accountRepository.findById(projection.getAccountId())
                .orElse(null);
        if (account == null || !"local".equalsIgnoreCase(account.getAuthProvider())) {
            return forgotPasswordAcceptedResponse();
        }

        Staff staff = staffRepository.findByAccountId(account.getAccountId()).orElse(null);
        if (staff == null || !staff.isActive() || !emailMatches(staff.getEmail(), request.email())) {
            return forgotPasswordAcceptedResponse();
        }

        String temporaryPassword = generateTemporaryPassword();
        try {
            emailUtility.sendHtml(
                    staff.getEmail(),
                    "Mật khẩu tạm thời tài khoản nhân viên",
                    buildStaffForgotPasswordEmail(staff.getStaffName(), account.getUsername(), temporaryPassword),
                    Map.of()
            );
        } catch (RuntimeException exception) {
            log.warn("Could not deliver staff forgot-password email for accountId={}", account.getAccountId(), exception);
            return forgotPasswordAcceptedResponse();
        }
        account.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        accountRepository.save(account);
        refreshTokenRepository.revokeAllByAccount(account);

        return forgotPasswordAcceptedResponse();
    }

    /**
     * Validates the projection-level staff reset rules before loading entities.
     */
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

    /**
     * Compares emails after trimming without changing the stored profile value.
     */
    private boolean emailMatches(String storedEmail, String requestedEmail) {
        return storedEmail != null
                && requestedEmail != null
                && storedEmail.trim().equalsIgnoreCase(requestedEmail.trim());
    }

    /**
     * Builds a short temporary password that still satisfies the staff password
     * rule requiring letters and digits.
     */
    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder("S7");
        for (int index = 0; index < 10; index++) {
            password.append(TEMP_PASSWORD_CHARS[PASSWORD_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length)]);
        }
        return password.toString();
    }

    /**
     * Renders a minimal staff reset email without exposing reset tokens in URLs.
     */
    private String buildStaffForgotPasswordEmail(String staffName, String username, String temporaryPassword) {
        String safeName = escapeHtml(staffName == null || staffName.isBlank() ? "nhân viên" : staffName);
        String safeUsername = escapeHtml(username);
        String safePassword = escapeHtml(temporaryPassword);
        return """
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#0f172a;">
                  <p>Xin chào <strong>%s</strong>,</p>
                  <p>Hệ thống đã cấp mật khẩu tạm thời cho tài khoản nhân viên <strong>%s</strong>.</p>
                  <p style="font-size:18px;font-weight:700;background:#f1f5f9;padding:12px;border-radius:6px;">%s</p>
                  <p>Vui lòng đăng nhập và đổi mật khẩu ngay trong trang hồ sơ.</p>
                  <p style="color:#64748b;font-size:13px;">Nếu bạn không yêu cầu thao tác này, hãy báo quản lý hoặc quản trị hệ thống ngay.</p>
                </div>
                """.formatted(safeName, safeUsername, safePassword);
    }

    /**
     * Escapes the small amount of profile text interpolated into reset emails.
     */
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Returns the generic forgot-password response shared by all outcomes.
     */
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

    /**
     * Tạo Account + Customer theo từng loại Auth
     */
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

        String customerName = determineCustomerName(providedCustomerName, token);
        String phone = determinePhone(providedUsername, token, authProvider);
        String email = determineEmail(providedEmail, token);

        Customer customer = Customer.builder()
                .accountId(savedAccount.getAccountId())
                .customerName(customerName)
                .phone(phone)
                .email(email)
                .isActive(true)
                .build();

        customerRepository.save(customer);
    }

    private String determineUsername(String provided, FirebaseToken token, String authProvider) {
        if (provided != null && !provided.isBlank()) {
            return provided; // Phone Auth
        }

        if (token.getEmail() != null) {
            return token.getEmail(); // Google & Facebook ưu tiên email
        }

        if ("facebook".equals(authProvider)) {
            return "fb_" + token.getUid().substring(0, 8); // tự gen username nếu Facebook ko có info email đi kèm
        }

        return "user_" + token.getUid().substring(0, 8); // fallback
    }

    private String determineCustomerName(String provided, FirebaseToken token) {
        if (provided != null && !provided.isBlank()) {
            return provided;
        }
        return Optional.ofNullable(token.getName())
                .or(() -> Optional.ofNullable((String) token.getClaims().get("name")))
                .orElse("User");
    }

    private String determinePhone(String providedUsername, FirebaseToken token, String authProvider) {
        String phoneFromClaims = (String) token.getClaims().get("phone_number");
        if (phoneFromClaims != null) {
            return phoneFromClaims;
        }

        if ("firebase".equals(authProvider) && providedUsername != null
                && providedUsername.matches("^0[0-9]{9,10}$")) {
            return providedUsername;
        }

        return null;
    }

    private String determineEmail(String provided, FirebaseToken token) {
        if (provided != null && !provided.isBlank()) {
            return provided;
        }
        return token.getEmail();
    }

private AuthResponse buildResponse(AccountProjection account) {
    // 1. Kiểm tra xem Projection có trả về chuỗi Roles nào không
    if (account.getRoleNames() == null || account.getRoleNames().isBlank()) {
        log.error("Tài khoản '{}' (ID: {}) không được gán bất kỳ quyền nào dưới Database!", 
                account.getUsername(), account.getAccountId());
        throw new BusinessRuleException("Tài khoản của bạn chưa được cấp quyền trên hệ thống. Vui lòng liên hệ Admin!");
    }

    // 2. Tách chuỗi thành List Roles xịn từ DB
    List<String> roles = Arrays.stream(account.getRoleNames().split(","))
            .map(String::trim)
            .collect(Collectors.toList());

    // 3. Gom dữ liệu đút vào Claims của JWT
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("accountId", account.getAccountId());
    extraClaims.put("roles", roles);

    // 4. Tạo Entity giả lập bọc thông tin cơ bản để JwtService ký sinh token
    Account accountEntity = Account.builder()
            .accountId(account.getAccountId())
            .username(account.getUsername())
            .build();

    // 5. Gọi JwtService sinh mã token thực tế
    String jwtToken = jwtService.generateToken(extraClaims, accountEntity);

    String refreshToken = jwtService.generateRefreshToken(accountEntity);
    refreshTokenRepository.deleteAllByAccount(accountEntity); // chơi trò 1 lúc chỉ đc login 1 thiết bị
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
