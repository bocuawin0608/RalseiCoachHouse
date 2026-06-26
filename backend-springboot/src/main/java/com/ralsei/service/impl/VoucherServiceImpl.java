package com.ralsei.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.request.voucher.CreateVoucherRequest;
import com.ralsei.dto.request.voucher.UpdateVoucherRequest;
import com.ralsei.dto.request.voucher.VoucherFilterRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.passengerbooking.VoucherDTO;
import com.ralsei.dto.response.voucher.VoucherMetricsResponse;
import com.ralsei.dto.response.voucher.VoucherResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Voucher;
import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.repository.VoucherRepository;
import com.ralsei.service.VoucherService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository voucherRepository;
    private final PassengerTicketRepository passengerTicketRepository;

    @Override
    @Transactional
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        validateDiscountValue(request.getDiscountType(), request.getDiscountValue());
        validateDateRange(request.getStartEffectiveDate(), request.getEndEffectiveDate());

        if (voucherRepository.existsByVoucherCode(request.getVoucherCode())) {
            throw new BusinessRuleException("Voucher code '" + request.getVoucherCode() + "' already exists");
        }

        Voucher voucher = Voucher.builder()
                .voucherCode(request.getVoucherCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountValue(request.getMaxDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .startEffectiveDate(request.getStartEffectiveDate())
                .endEffectiveDate(request.getEndEffectiveDate())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .build();

        voucher = voucherRepository.save(voucher);
        return mapToResponse(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(int id, UpdateVoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));

        validateDiscountValue(request.getDiscountType(), request.getDiscountValue());
        validateDateRange(request.getStartEffectiveDate(), request.getEndEffectiveDate());

        if (request.getUsageLimit() < voucher.getUsedCount()) {
            throw new BusinessRuleException("Usage limit cannot be less than current used count (" + voucher.getUsedCount() + ")");
        }

        boolean hasReferences = passengerTicketRepository.existsByVoucherId(id);
        if (hasReferences) {
            if (!voucher.getVoucherCode().equals(request.getVoucherCode())) {
                throw new BusinessRuleException("Cannot change voucher code because it has references");
            }
            if (!voucher.getDiscountType().equals(request.getDiscountType())) {
                throw new BusinessRuleException("Cannot change discount type because it has references");
            }
            if (voucher.getDiscountValue().compareTo(request.getDiscountValue()) != 0) {
                throw new BusinessRuleException("Cannot change discount value because it has references");
            }
        }

        voucher.setVoucherCode(request.getVoucherCode());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountValue(request.getMaxDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setStartEffectiveDate(request.getStartEffectiveDate());
        voucher.setEndEffectiveDate(request.getEndEffectiveDate());
        voucher.setUsageLimit(request.getUsageLimit());

        voucher = voucherRepository.save(voucher);
        return mapToResponse(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(int id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));
        return mapToResponse(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VoucherResponse> getAllVouchers(VoucherFilterRequest filterRequest) {
        PageRequest pageRequest = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                Sort.by("voucherId").descending()
        );

        Page<Voucher> voucherPage = voucherRepository.searchVouchers(
                filterRequest.getSearch(),
                filterRequest.getDiscountType(),
                filterRequest.getFromDate(),
                filterRequest.getToDate(),
                pageRequest
        );

        return new PagedResponse<>(
                voucherPage.getContent().stream().map(this::mapToResponse).toList(),
                voucherPage.getNumber(),
                voucherPage.getSize(),
                voucherPage.getTotalElements(),
                voucherPage.getTotalPages(),
                voucherPage.isLast()
        );
    }

    @Override
    @Transactional
    public void deleteVoucher(int id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id: " + id));

        boolean hasReferences = passengerTicketRepository.existsByVoucherId(id);
        if (hasReferences) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(voucher.getStartEffectiveDate())) {
                voucher.setEndEffectiveDate(voucher.getStartEffectiveDate());
            } else {
                voucher.setEndEffectiveDate(now);
            }
            voucherRepository.save(voucher);
        } else {
            voucherRepository.delete(voucher);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherMetricsResponse getVoucherMetrics() {
        LocalDateTime now = LocalDateTime.now();
        long totalVouchers = voucherRepository.count();
        long activeVouchers = voucherRepository.countActiveVouchers(now);
        long expiredVouchers = voucherRepository.countExpiredVouchers(now);
        long exhaustedVouchers = voucherRepository.count() - activeVouchers - expiredVouchers;
        long totalUsageCount = voucherRepository.sumTotalUsageCount();

        return VoucherMetricsResponse.builder()
                .totalVouchers(totalVouchers)
                .activeVouchers(activeVouchers)
                .expiredVouchers(expiredVouchers)
                .exhaustedVouchers(Math.max(exhaustedVouchers, 0))
                .totalUsageCount(totalUsageCount)
                .build();
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .voucherId(voucher.getVoucherId())
                .voucherCode(voucher.getVoucherCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountValue(voucher.getMaxDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .startEffectiveDate(voucher.getStartEffectiveDate())
                .endEffectiveDate(voucher.getEndEffectiveDate())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .createdBy(voucher.getCreatedBy())
                .updatedBy(voucher.getUpdatedBy())
                .status(computeStatus(voucher))
                .build();
    }

    private String computeStatus(Voucher voucher) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(voucher.getEndEffectiveDate())) {
            return "EXPIRED";
        }
        if (voucher.getUsedCount() >= voucher.getUsageLimit() && voucher.getUsageLimit() > 0) {
            return "EXHAUSTED";
        }
        if (now.isBefore(voucher.getStartEffectiveDate())) {
            return "UPCOMING";
        }
        return "ACTIVE";
    }

    private void validateDiscountValue(String discountType, BigDecimal discountValue) {
        if ("PERCENT".equals(discountType) && discountValue.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessRuleException("Percentage discount value cannot exceed 100");
        }
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessRuleException("Start effective date must be before end effective date");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<VoucherDTO> getEligibleVouchers() {
        List<Voucher> vouchers = voucherRepository.getEligibleVouchers();
        return vouchers.stream().map(voucher -> new VoucherDTO(
            voucher.getVoucherId(),
            voucher.getVoucherCode(),
            voucher.getDiscountType(),
            voucher.getDiscountValue(),
            voucher.getMaxDiscountValue(),
            voucher.getMinOrderValue(),
            voucher.getEndEffectiveDate()
        )).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Voucher getEligibleVoucher(Integer voucherId, BigDecimal currentOrderValue) {
        return voucherRepository.getEligibleVoucher(voucherId, currentOrderValue);
    }
}
