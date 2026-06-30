package com.ralsei.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.account.AccountFilterRequest;
import com.ralsei.dto.request.account.AssignRolesRequest;
import com.ralsei.dto.request.account.CreateAccountRequest;
import com.ralsei.dto.request.account.ResetPasswordRequest;
import com.ralsei.dto.request.account.UpdateAccountRequest;
import com.ralsei.dto.response.account.AccountDetailResponse;
import com.ralsei.dto.response.account.AccountListResponse;
import com.ralsei.dto.response.account.RoleResponse;
import com.ralsei.service.AccountService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AccountController {
    private final AccountService accountService;

    @GetMapping(path = {"", "/"})
    public ResponseEntity<Page<AccountListResponse>> filterAccounts(
        @Valid @ModelAttribute AccountFilterRequest filterRequest,
        Pageable pageable
    ) {
        return ResponseEntity.ok(accountService.filterAccounts(filterRequest, pageable));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(accountService.getAllRoles());
    }

    @GetMapping("/{accountId:\\d+}")
    public ResponseEntity<AccountDetailResponse> getAccountDetail(
        @PathVariable @Min(value = 1, message = "ID tài khoản phải lớn hơn 0.") Integer accountId
    ) {
        return ResponseEntity.ok(accountService.getAccountDetail(accountId));
    }

    @PostMapping(path = {"", "/"})
    public ResponseEntity<Integer> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Integer newId = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newId);
    }

    @PutMapping("/{accountId:\\d+}")
    public ResponseEntity<Void> updateAccount(
        @PathVariable @Min(value = 1, message = "ID tài khoản phải lớn hơn 0.") Integer accountId,
        @Valid @RequestBody UpdateAccountRequest request
    ) {
        accountService.updateAccount(accountId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{accountId:\\d+}/roles")
    public ResponseEntity<Void> assignRoles(
        @PathVariable @Min(value = 1, message = "ID tài khoản phải lớn hơn 0.") Integer accountId,
        @Valid @RequestBody AssignRolesRequest request
    ) {
        accountService.assignRoles(accountId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{accountId:\\d+}/reset-password")
    public ResponseEntity<Void> resetPassword(
        @PathVariable @Min(value = 1, message = "ID tài khoản phải lớn hơn 0.") Integer accountId,
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        accountService.resetPassword(accountId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{accountId:\\d+}/toggle-active")
    public ResponseEntity<Void> toggleActive(
        @PathVariable @Min(value = 1, message = "ID tài khoản phải lớn hơn 0.") Integer accountId
    ) {
        accountService.toggleActive(accountId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{accountId:\\d+}")
    public ResponseEntity<Void> deleteAccount(
        @PathVariable @Min(value = 1, message = "ID tài khoản phải lớn hơn 0.") Integer accountId
    ) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}
