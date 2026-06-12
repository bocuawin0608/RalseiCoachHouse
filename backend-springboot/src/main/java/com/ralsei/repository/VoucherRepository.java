package com.ralsei.repository;

import com.ralsei.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByVoucherCode(String voucherCode);

    boolean existsByVoucherCode(String voucherCode);

    boolean existsByVoucherCodeAndVoucherIdNot(String voucherCode, int voucherId);

    @Query("SELECT v FROM Voucher v WHERE " +
            "(:search IS NULL OR v.voucherCode LIKE %:search%) " +
            "AND (:discountType IS NULL OR v.discountType = :discountType) " +
            "AND (:fromDate IS NULL OR v.startEffectiveDate >= :fromDate) " +
            "AND (:toDate IS NULL OR v.endEffectiveDate <= :toDate)")
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
}
