package com.ralsei.auth.controller.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.auth.model.Account;
import com.ralsei.auth.model.AccountRole;
import com.ralsei.auth.model.Role;
import com.ralsei.auth.repository.AccountRepository;
import com.ralsei.auth.repository.AccountRoleRepository;
import com.ralsei.auth.repository.RoleRepository;

@RestController
@RequestMapping("/api/internal/accounts")
public class InternalAccountController {

    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final RoleRepository roleRepository;

    public InternalAccountController(AccountRepository accountRepository,
                                     AccountRoleRepository accountRoleRepository,
                                     RoleRepository roleRepository) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable Integer accountId) {
        return accountRepository.findById(accountId)
            .map(acc -> {
                List<AccountRole> accountRoles = accountRoleRepository.findByAccountId(accountId);
                List<Integer> roleIds = accountRoles.stream().map(AccountRole::getRoleId).collect(Collectors.toList());
                List<String> roleNames = roleIds.stream()
                    .map(rId -> roleRepository.findById(rId).map(Role::getRoleName).orElse(""))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());

                Map<String, Object> map = new HashMap<>();
                map.put("accountId", acc.getAccountId());
                map.put("username", acc.getUsername());
                map.put("authProvider", acc.getAuthProvider());
                map.put("isActive", acc.isEnabled());
                map.put("firebaseUid", acc.getFirebaseUid());
                map.put("lastLogin", acc.getLastLogin());
                map.put("roleIds", roleIds);
                map.put("roles", roleNames);
                return ResponseEntity.ok((Object) map);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{accountId}/roles")
    public ResponseEntity<List<Role>> getRolesByAccountId(@PathVariable Integer accountId) {
        List<AccountRole> accountRoles = accountRoleRepository.findByAccountId(accountId);
        List<Integer> roleIds = accountRoles.stream().map(AccountRole::getRoleId).collect(Collectors.toList());
        List<Role> roles = roleRepository.findAllById(roleIds);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Account>> getAccountsBatch(@RequestBody List<Integer> accountIds) {
        List<Account> accounts = accountRepository.findAllById(accountIds);
        return ResponseEntity.ok(accounts);
    }
}
