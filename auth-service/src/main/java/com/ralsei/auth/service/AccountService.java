package com.ralsei.auth.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.auth.dto.request.account.AccountFilterRequest;
import com.ralsei.auth.dto.request.account.AssignRolesRequest;
import com.ralsei.auth.dto.request.account.CreateAccountRequest;
import com.ralsei.auth.dto.request.account.ResetPasswordRequest;
import com.ralsei.auth.dto.request.account.UpdateAccountRequest;
import com.ralsei.auth.dto.response.account.AccountDetailResponse;
import com.ralsei.auth.dto.response.account.AccountListResponse;
import com.ralsei.auth.dto.response.account.RoleResponse;

/**
 * Service interface for account management operations.
 */

/**
 * Provides the business service contract for account.
 */
public interface AccountService {
    Page<AccountListResponse> filterAccounts(AccountFilterRequest filterRequest, Pageable pageable);
    AccountDetailResponse getAccountDetail(Integer accountId);
    Integer createAccount(CreateAccountRequest request);
    void updateAccount(Integer accountId, UpdateAccountRequest request);
    void assignRoles(Integer accountId, AssignRolesRequest request);
    void resetPassword(Integer accountId, ResetPasswordRequest request);
    void toggleActive(Integer accountId);
    void deleteAccount(Integer accountId);
    List<RoleResponse> getAllRoles();
}
