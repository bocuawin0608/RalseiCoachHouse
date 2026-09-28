package com.ralsei.auth.controller.internal;

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
