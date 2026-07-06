package com.ralsei.service;

import java.util.List;

import com.ralsei.dto.response.customer.CargoOrderLookupResponse;

/** Authenticated, read-only use case for customer-owned cargo history. */
public interface CargoOrderLookupService {
    /** Returns all orders owned by the trusted account identifier. */
    List<CargoOrderLookupResponse> findByAccountId(Integer accountId);
}
