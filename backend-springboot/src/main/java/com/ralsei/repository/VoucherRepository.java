package com.ralsei.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ralsei.model.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByVoucherCode(String voucherCode);

    boolean existsByVoucherCode(String voucherCode);

    boolean existsByVoucherCodeAndVoucherIdNot(String voucherCode, int voucherId);

    @Query("SELECT v FROM Voucher v WHERE " +
            "(:search IS NULL OR v.voucherCode LIKE %:search%) " +
            "AND (:discountType IS NULL OR v.discountType = :discountType) " +
            "AND (:fromDate IS NULL OR v.startEffectiveDate >= :fromDate) " +
            "AND (:toDate IS NULL OR v.endEffectiveDate <= :toDate) ")
    Page<Voucher> searchVouchers(@Param("search") String search,
                                 @Param("discountType") String discountType,
                                 @Param("fromDate") LocalDateTime fromDate,
                                 @Param("toDate") LocalDateTime toDate,
                                 Pageable pageable);

    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.startEffectiveDate <= :now AND v.endEffectiveDate >= :now AND v.usedCount < v.usageLimit")
    long countActiveVouchers(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.endEffectiveDate < :now")
    long countExpiredVouchers(@Param("now") LocalDateTime now);

    @Query("SELECT COALESCE(SUM(v.usedCount), 0) FROM Voucher v")
    long sumTotalUsageCount();

    @Query(value = """
        SELECT v FROM Voucher v
        WHERE v.endEffectiveDate > CURRENT_TIMESTAMP
        AND v.startEffectiveDate <= CURRENT_TIMESTAMP
        AND v.usageLimit > v.usedCount
    """)
    List<Voucher> getEligibleVouchers();

    @Query(value = """
        SELECT v FROM Voucher v
        WHERE v.endEffectiveDate > CURRENT_TIMESTAMP
        AND v.startEffectiveDate <= CURRENT_TIMESTAMP
        AND v.usageLimit > v.usedCount
        AND v.voucherId = :voucherId
        AND v.minOrderValue <= :currentOrderValue
    """)
    Voucher getEligibleVoucher(@Param("voucherId") Integer voucherId, @Param("currentOrderValue") BigDecimal currentOrderValue);

    @Modifying
    @Query("""
        UPDATE Voucher v SET v.usedCount = v.usedCount + 1 
        WHERE v.voucherId = :id AND v.usedCount < v.usageLimit
        AND CURRENT_TIMESTAMP BETWEEN v.startEffectiveDate AND v.endEffectiveDate
    """)
    int incrementUsedCountIfAvailable(@Param("id") Integer voucherId);

    @Modifying
    @Query("""
        UPDATE Voucher v SET v.usedCount = v.usedCount - 1
        WHERE v.voucherId = :id AND v.usedCount > 0
    """)
    int decrementUsedCountIfApplied(@Param("id") Integer voucherId);
}
