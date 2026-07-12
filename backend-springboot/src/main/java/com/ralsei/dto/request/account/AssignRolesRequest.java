package com.ralsei.dto.request.account;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * AssignRolesRequest
 */

public record AssignRolesRequest(
    @NotNull(message = "Vui lòng chọn ít nhất một vai trò.")
    List<Integer> roleIds
) {}
