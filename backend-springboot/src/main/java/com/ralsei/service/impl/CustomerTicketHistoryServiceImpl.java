package com.ralsei.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.customer.CustomerTicketHistoryProjection;
import com.ralsei.dto.response.customer.CustomerTicketHistoryResponse;
import com.ralsei.dto.response.customer.CustomerTicketHistoryResponse.CustomerTicketSeatResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Customer;
import com.ralsei.repository.CustomerRepository;
import com.ralsei.repository.PassengerTicketDetailRepository;
import com.ralsei.service.CustomerTicketHistoryService;
import com.ralsei.util.QRCreateUitility;
import com.ralsei.util.PhoneNumberUtility;

import lombok.RequiredArgsConstructor;

/**
 * Reads customer-owned booking rows and converts their flat seat data into API responses.
 */
@Service
@RequiredArgsConstructor
public class CustomerTicketHistoryServiceImpl implements CustomerTicketHistoryService {

    private final CustomerRepository customerRepository;
    private final PassengerTicketDetailRepository ticketDetailRepository;
    private final QRCreateUitility qrCreateUitility;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public List<CustomerTicketHistoryResponse> getHistory(Integer accountId) {
        validateAccountId(accountId);
        ensureLegacyContactPhone(accountId);
        return assembleTickets(ticketDetailRepository.findCustomerTicketHistory(accountId, null));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public CustomerTicketHistoryResponse getDetail(Integer accountId, String ticketCode) {
        if (ticketCode == null || ticketCode.isBlank()) {
            throw new ResourceNotFoundException("Mã vé không hợp lệ.");
        }

        validateAccountId(accountId);
        ensureLegacyContactPhone(accountId);
        return assembleTickets(ticketDetailRepository.findCustomerTicketHistory(accountId, ticketCode))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé trong tài khoản của bạn."));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public byte[] getSeatQrImage(Integer accountId, Integer ticketDetailId) {
        validateAccountId(accountId);
        ensureLegacyContactPhone(accountId);
        String token = ticketDetailRepository.findOwnedQrToken(ticketDetailId, accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã QR của ghế trong tài khoản của bạn."));
        return qrCreateUitility.createPng(token);
    }

    /**
     * Rejects access tokens that do not contain a usable account identifier.
     */
    private void validateAccountId(Integer accountId) {
        if (accountId == null || accountId < 1) {
            throw new ResourceNotFoundException("Không xác định được tài khoản khách hàng.");
        }
    }

    /**
     * Backfills contact data for OAuth customers created before booking phone
     * synchronization. The source booking is already owned by the same account.
     */
    private void ensureLegacyContactPhone(Integer accountId) {
        Customer customer = customerRepository.findByAccountId(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ khách hàng."));

        if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
            return;
        }

        ticketDetailRepository.findLatestOwnedContactPhone(accountId).ifPresent(phone -> {
            customer.setPhone(PhoneNumberUtility.normalizeToLocalFormat(phone));
            customerRepository.save(customer);
        });
    }

    /**
     * Groups one-row-per-seat projections into one response per master ticket.
     */
    private List<CustomerTicketHistoryResponse> assembleTickets(List<CustomerTicketHistoryProjection> rows) {
        Map<Integer, List<CustomerTicketHistoryProjection>> rowsByTicket = new LinkedHashMap<>();
        rows.forEach(row -> rowsByTicket
            .computeIfAbsent(row.getPassengerTicketId(), ignored -> new ArrayList<>())
            .add(row));

        return rowsByTicket.values().stream().map(ticketRows -> {
            CustomerTicketHistoryProjection first = ticketRows.get(0);
            List<CustomerTicketSeatResponse> seats = ticketRows.stream()
                .map(row -> new CustomerTicketSeatResponse(
                    row.getTicketDetailId(),
                    row.getSeatCode(),
                    row.getSeatPrice()
                ))
                .toList();

            return new CustomerTicketHistoryResponse(
                first.getPassengerTicketId(),
                first.getTicketCode(),
                first.getTicketStatus(),
                first.getTotalPrice(),
                first.getPickupStopName(),
                first.getDropoffStopName(),
                first.getBookedAt(),
                first.getDepartureTime(),
                first.getRouteName(),
                first.getCoachTypeName(),
                first.getPaymentMethod(),
                first.getPaymentStatus(),
                first.getFullName(),
                first.getPhone(),
                first.getEmail(),
                seats
            );
        }).toList();
    }
}
