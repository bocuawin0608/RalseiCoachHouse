package com.ralsei.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.ralsei.dto.projection.AccountProjection;
import com.ralsei.dto.request.auth.CustomerLoginRequest;
import com.ralsei.dto.request.auth.CustomerRegisterRequest;
import com.ralsei.dto.request.auth.StaffLoginRequest;
import com.ralsei.dto.response.auth.AuthResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.model.Account;
import com.ralsei.model.AccountRole;
import com.ralsei.model.Customer;
import com.ralsei.model.Role;
import com.ralsei.repository.AccountRepository;
import com.ralsei.repository.AccountRoleRepository;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.RoleRepository;
import com.ralsei.service.AuthService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository      accountRepository;
    private final AccountRoleRepository  accountRoleRepository;
    private final CustomerRepository     customerRepository;
    private final RoleRepository         roleRepository;
    private final Optional<FirebaseAuth> firebaseAuth;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ─────────────────────────────────────────────────────────────────────────
    // CUSTOMER: login (form / Google / Facebook — tất cả qua Firebase)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse customerLogin(CustomerLoginRequest request) {
        // 1. Xác thực token với Firebase Admin SDK
        FirebaseToken firebaseToken = verifyFirebaseToken(request.idToken());
        String firebaseUid   = firebaseToken.getUid();
        String authProvider  = detectAuthProvider(firebaseToken);

        // 2. Tìm account theo username
        AccountProjection account = accountRepository
            .findByUsernameWithRoles(request.username())
            .orElse(null);

        // 3. Social login lần đầu → tự động tạo account + customer profile
        //    (Phone/form login KHÔNG tự tạo ở đây — họ phải đăng ký trước)
        if (account == null) {
            if ("firebase".equals(authProvider)) {
                // Phone/form login mà không tìm thấy → chưa đăng ký
                throw new BusinessRuleException("Tài khoản chưa tồn tại. Vui lòng đăng ký!");
            }
            // Google / Facebook → auto-tạo account lần đầu
            buildAndSaveCustomerAccount(request.username(), firebaseUid, authProvider,
                null, null);
            account = accountRepository.findByUsernameWithRoles(request.username())
                .orElseThrow(() -> new BusinessRuleException("Lỗi tạo tài khoản!"));
        }

        // 4. Guard: Staff không được dùng endpoint này
        if ("local".equals(account.getAuthProvider())) {
            throw new BusinessRuleException("Tài khoản nội bộ không đăng nhập qua đây!");
        }

        // 5. Kiểm tra active
        if (Boolean.FALSE.equals(account.getIsActive())) {
            throw new BusinessRuleException("Tài khoản đã bị khóa!");
        }

        // 6. Cập nhật lastLogin và sync firebaseUid (phòng trường hợp đổi)
        accountRepository.findById(account.getAccountId()).ifPresent(acc -> {
            acc.setFirebaseUid(firebaseUid);
            acc.setAuthProvider(authProvider); // cập nhật nếu lần đầu dùng social
            acc.setLastLogin(LocalDateTime.now());
            accountRepository.save(acc);
        });

        return buildResponse(account);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CUSTOMER: đăng ký (chỉ cho phone/password form, không dùng cho social)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse customerRegister(CustomerRegisterRequest request) {
        // 1. Xác thực token Firebase
        FirebaseToken firebaseToken = verifyFirebaseToken(request.idToken());
        String firebaseUid  = firebaseToken.getUid();
        String authProvider = detectAuthProvider(firebaseToken);

        // 2. Kiểm tra username chưa tồn tại
        if (accountRepository.existsByUsername(request.username())) {
            throw new BusinessRuleException("Số điện thoại đã được đăng ký!");
        }

        // 3. Tạo account + customer profile
        buildAndSaveCustomerAccount(
            request.username(),
            firebaseUid,
            authProvider,
            request.customerName(),
            request.email()
        );

        // 4. Trả về response luôn (không cần login lại)
        AccountProjection account = accountRepository
            .findByUsernameWithRoles(request.username())
            .orElseThrow(() -> new BusinessRuleException("Lỗi tạo tài khoản!"));

        return buildResponse(account);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAFF: đăng nhập bằng local credentials
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse staffLogin(StaffLoginRequest request) {
        // 1. Tìm account
        AccountProjection account = accountRepository
            .findByUsernameWithRoles(request.username())
            .orElseThrow(() -> new BusinessRuleException("Sai tên đăng nhập hoặc mật khẩu!"));

        // 2. Guard: chỉ local account mới vào đây
        if (!"local".equals(account.getAuthProvider())) {
            throw new BusinessRuleException("Tài khoản này đăng nhập qua mạng xã hội!");
        }

        // 3. Verify password (chỉ dùng bcrypt, không so sánh plaintext)
        if (account.getPasswordHash() == null ||
            !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new BusinessRuleException("Sai tên đăng nhập hoặc mật khẩu!");
        }

        // 4. Kiểm tra active
        if (Boolean.FALSE.equals(account.getIsActive())) {
            throw new BusinessRuleException("Tài khoản đã bị khóa!");
        }

        // 5. Cập nhật lastLogin
        accountRepository.findById(account.getAccountId()).ifPresent(acc -> {
            acc.setLastLogin(LocalDateTime.now());
            accountRepository.save(acc);
        });

        return buildResponse(account);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private FirebaseToken verifyFirebaseToken(String idToken) {
        if (firebaseAuth.isEmpty()) {
            throw new BusinessRuleException("Firebase chưa được cấu hình!");
        }
        try {
            return firebaseAuth.get().verifyIdToken(idToken);
        } catch (Exception e) {
            log.error("Firebase token verification failed", e);
            throw new BusinessRuleException("Xác thực Firebase thất bại!");
        }
    }

    @SuppressWarnings("unchecked")
    private String detectAuthProvider(FirebaseToken token) {
        // Firebase token có claim "firebase.sign_in_provider"
        Map<String, Object> firebaseClaims =
            (Map<String, Object>) token.getClaims().get("firebase");
        if (firebaseClaims == null) return "firebase";

        String signInProvider = (String) firebaseClaims
            .getOrDefault("sign_in_provider", "firebase");

        return switch (signInProvider) {
            case "google.com"   -> "google";
            case "facebook.com" -> "facebook";
            default             -> "firebase"; // password, phone, etc.
        };
    }

    private void buildAndSaveCustomerAccount(
        String username, String firebaseUid, String authProvider,
        String customerName, String email
    ) {
        // Tạo Account
        Account account = Account.builder()
            .username(username)
            .passwordHash(null)         // Firebase user không có local password
            .firebaseUid(firebaseUid)
            .authProvider(authProvider)
            .isActive(true)
            .build();
        Account saved = accountRepository.save(account);

        // Gán role Customer
        Role customerRole = roleRepository.findByRoleName("Customer")
            .orElseThrow(() -> new BusinessRuleException("Chưa có role Customer trong hệ thống!"));
        accountRoleRepository.save(AccountRole.builder()
            .accountId(saved.getAccountId())
            .roleId(customerRole.getRoleId())
            .build());

        // Tạo Customer profile
        // phone: nếu username là số điện thoại thì dùng luôn, social login để null
        String phone = (username != null && username.matches("^0[0-9]{9,10}$"))
            ? username : "unknown";  // hoặc throw nếu bắt buộc

        Customer customer = Customer.builder()
            .accountId(saved.getAccountId())
            .customerName(customerName != null ? customerName : username)
            .phone(phone)
            .email(email)
            .isActive(true)
            .build();
        customerRepository.save(customer);
    }

    private AuthResponse buildResponse(AccountProjection account) {
        List<String> roles = (account.getRoleNames() == null || account.getRoleNames().isBlank())
            ? List.of("Customer")
            : Arrays.asList(account.getRoleNames().split(","));

        return AuthResponse.builder()
            .success(true)
            .message("Thành công!")
            .username(account.getUsername())
            .roles(roles)
            .accessToken(UUID.randomUUID().toString()) //TODO: sẽ thay bằng JWT ở đây!!!
            .build();
    }
}

