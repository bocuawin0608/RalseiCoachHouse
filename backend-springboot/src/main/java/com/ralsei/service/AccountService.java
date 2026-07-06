package com.ralsei.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.dto.request.account.AccountFilterRequest;
import com.ralsei.dto.request.account.AssignRolesRequest;
import com.ralsei.dto.request.account.CreateAccountRequest;
import com.ralsei.dto.request.account.ResetPasswordRequest;
import com.ralsei.dto.request.account.UpdateAccountRequest;
import com.ralsei.dto.response.account.AccountDetailResponse;
import com.ralsei.dto.response.account.AccountListResponse;
import com.ralsei.dto.response.account.RoleResponse;

/**
 * Service interface for account management operations.
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
