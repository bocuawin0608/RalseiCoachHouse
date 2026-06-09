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
                throw new BusinessRuleException("Tài khoản chưa tồn tại. Vui lòng đăng ký hoặc đăng nhập bằng hình thức khác!");
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
            request.email()
        );

        AccountProjection account = accountRepository
            .findByUsernameWithRoles(request.username())
            .orElseThrow(() -> new BusinessRuleException("Lỗi tạo tài khoản!"));

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
        Map<String, Object> firebaseClaims = (Map<String, Object>) token.getClaims().get("firebase");
        if (firebaseClaims == null) return "firebase";

        String signInProvider = (String) firebaseClaims.getOrDefault("sign_in_provider", "firebase");

        return switch (signInProvider) {
            case "google.com"   -> "google";
            case "facebook.com" -> "facebook";
            default             -> "firebase"; 
        };
    }

    /**
     * Tạo Account + Customer theo từng loại Auth
     */
    private void buildAndSaveCustomerAccount(
        String providedUsername, FirebaseToken token, String authProvider, 
        String providedCustomerName, String providedEmail
    ) {
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
            return provided;                    // Phone Auth
        }

        if (token.getEmail() != null) {
            return token.getEmail();            // Google & Facebook ưu tiên email
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
        List<String> roles = (account.getRoleNames() == null || account.getRoleNames().isBlank())
            ? List.of("CUSTOMER")
            : Arrays.asList(account.getRoleNames().split(","));

        return AuthResponse.builder()
            .success(true)
            .message("Thành công!")
            .username(account.getUsername())
            .roles(roles)
            .accessToken(UUID.randomUUID().toString()) // TODO: Thay bằng JWT sau
            .build();
    }
}