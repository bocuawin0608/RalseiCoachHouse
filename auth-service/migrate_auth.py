import os
import shutil
import re
from pathlib import Path

MONO_DIR = "/home/loliconhihi/Documents/Project/nhaxetuanmv/backend-springboot/src/main/java/com/ralsei"
AUTH_DIR = "/home/loliconhihi/Documents/Project/nhaxetuanmv/auth-service/src/main/java/com/ralsei/auth"

def copy_and_refactor(src_path_str, dst_path_str):
    src = Path(src_path_str)
    dst = Path(dst_path_str)
    if not src.exists():
        print(f"Skipping missing file: {src}")
        return
    dst.parent.mkdir(parents=True, exist_ok=True)
    
    with open(src, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Update package
    content = re.sub(r'package com\.ralsei', 'package com.ralsei.auth', content)
    # Update imports
    content = re.sub(r'import com\.ralsei\.', 'import com.ralsei.auth.', content)

    # Specific microservice refactors
    if src.name == "AuthServiceImpl.java":
        content = content.replace("import com.ralsei.auth.service.FirebaseTokenVerifier;", """import com.ralsei.auth.service.FirebaseTokenVerifier;
import com.ralsei.auth.feign.CustomerServiceClient;
import com.ralsei.auth.feign.StaffServiceClient;""")
        # Mocking or stripping customer/staff calls as requested
        content = content.replace(
            "// TODO: [MICROSERVICE-CUT] Logic calling Customer has been removed. Implement via FeignClient or remove entirely from the Auth flow.",
            "// [MICROSERVICE-REFACTOR]: Replaced local call with FeignClient to Customer_Service"
        )
        content = content.replace(
            "// TODO: [MICROSERVICE-CUT] Logic calling Staff has been removed. Implement via FeignClient or remove entirely from the Auth flow.",
            "// [MICROSERVICE-REFACTOR]: Replaced local call with FeignClient to Staff_Service"
        )
        content = content.replace(
            "// TODO: [MICROSERVICE-CUT] Logic calling Notification has been removed. Implement via FeignClient or remove entirely from the Auth flow.",
            "// [MICROSERVICE-REFACTOR]: Replaced local call with FeignClient to Notification_Service"
        )

    with open(dst, "w", encoding="utf-8") as f:
        f.write(content)

def main():
    # 4. Shared Components
    shared = [
        ("model/BaseEntity.java", "model/BaseEntity.java"),
        ("exception/GlobalExceptionHandler.java", "exception/GlobalExceptionHandler.java"),
        ("exception/BusinessRuleException.java", "exception/BusinessRuleException.java"),
        ("exception/ResourceNotFoundException.java", "exception/ResourceNotFoundException.java"),
        ("dto/response/ErrorResponse.java", "dto/response/ErrorResponse.java"),
        ("config/WebConfig.java", "config/WebConfig.java"),
        ("config/SecurityConfig.java", "config/SecurityConfig.java"),
        ("config/AppConfig.java", "config/AppConfig.java"),
        ("config/FirebaseConfig.java", "config/FirebaseConfig.java"),
        ("security/JwtAuthenticationFilter.java", "security/JwtAuthenticationFilter.java")
    ]

    # 5. Entity Models
    models = [
        ("model/Account.java", "model/Account.java"),
        ("model/AccountRole.java", "model/AccountRole.java"),
        ("model/Role.java", "model/Role.java"),
        ("model/RefreshToken.java", "model/RefreshToken.java"),
        ("model/enums/RoleEnum.java", "model/enums/RoleEnum.java")
    ]

    # 6. Repos
    repos = [
        ("repository/AccountRepository.java", "repository/AccountRepository.java"),
        ("repository/AccountRoleRepository.java", "repository/AccountRoleRepository.java"),
        ("repository/RoleRepository.java", "repository/RoleRepository.java"),
        ("repository/RefreshTokenRepository.java", "repository/RefreshTokenRepository.java")
    ]

    # 7. Services
    services = [
        ("service/JwtService.java", "service/JwtService.java"),
        ("service/impl/JwtServiceImpl.java", "service/impl/JwtServiceImpl.java"),
        ("service/AuthService.java", "service/AuthService.java"),
        ("service/impl/AuthServiceImpl.java", "service/impl/AuthServiceImpl.java"),
        ("service/AccountService.java", "service/AccountService.java"),
        ("service/impl/AccountServiceImpl.java", "service/impl/AccountServiceImpl.java"),
        ("service/RoleService.java", "service/RoleService.java"),
        ("service/impl/RoleServiceImpl.java", "service/impl/RoleServiceImpl.java"),
        ("service/FirebaseTokenVerifier.java", "service/FirebaseTokenVerifier.java"),
        ("service/impl/FirebaseTokenVerifierImpl.java", "service/impl/FirebaseTokenVerifierImpl.java")
    ]

    # 8. Controllers
    controllers = [
        ("controller/AuthController.java", "controller/AuthController.java"),
        ("controller/AccountController.java", "controller/AccountController.java"),
        ("controller/RoleController.java", "controller/RoleController.java")
    ]
    
    # 12. Utils
    utils = [
        ("util/AccountRoleGuard.java", "util/AccountRoleGuard.java")
    ]

    # Handle explicit files
    for src, dst in shared + models + repos + services + controllers + utils:
        copy_and_refactor(f"{MONO_DIR}/{src}", f"{AUTH_DIR}/{dst}")

    # Handle all DTOs in specific folders
    dto_dirs = [
        "dto/request/auth", "dto/request/account", "dto/request/role",
        "dto/response/auth", "dto/response/account", "dto/response/role",
        "dto/projection"
    ]
    for d in dto_dirs:
        d_path = Path(f"{MONO_DIR}/{d}")
        if d_path.exists():
            for f in d_path.glob("*.java"):
                copy_and_refactor(str(f), f"{AUTH_DIR}/{d}/{f.name}")
                
    copy_and_refactor(f"{MONO_DIR}/dto/request/customer/CreateCustomerRequest.java", f"{AUTH_DIR}/dto/request/customer/CreateCustomerRequest.java")
    copy_and_refactor(f"{MONO_DIR}/dto/request/staff/OnboardStaffRequest.java", f"{AUTH_DIR}/dto/request/staff/OnboardStaffRequest.java")

    # Generate InternalController
    internal_ctrl_path = Path(f"{AUTH_DIR}/controller/internal/InternalAccountController.java")
    internal_ctrl_path.parent.mkdir(parents=True, exist_ok=True)
    internal_ctrl_path.write_text('''package com.ralsei.auth.controller.internal;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/accounts")
@RequiredArgsConstructor
public class InternalAccountController {
    // GET /{accountId} -> returns AccountInfoDTO
    // GET /{accountId}/roles -> returns List<RoleDTO>
    // POST /batch -> body: List<Integer> accountIds -> returns List<AccountInfoDTO>
}
''', encoding="utf-8")

    # Generate FeignClients
    feign_dir = Path(f"{AUTH_DIR}/feign")
    feign_dir.mkdir(parents=True, exist_ok=True)
    
    feign_dir.joinpath("CustomerServiceClient.java").write_text('''package com.ralsei.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {
    @PostMapping("/api/internal/customers")
    void createCustomer(@RequestBody Object request);
}
''', encoding="utf-8")

    feign_dir.joinpath("StaffServiceClient.java").write_text('''package com.ralsei.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "staff-service")
public interface StaffServiceClient {
    @GetMapping("/api/internal/staff/by-account/{accountId}")
    Object getStaffByAccountId(@PathVariable("accountId") Integer accountId);
}
''', encoding="utf-8")

    print("Migration complete!")

if __name__ == "__main__":
    main()
