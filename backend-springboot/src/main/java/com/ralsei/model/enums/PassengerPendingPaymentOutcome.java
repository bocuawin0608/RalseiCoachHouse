package com.ralsei.model.enums;

/**
 * Kết quả khi dừng thanh toán đang chờ (pending) của vé hành khách.
 * Khác với hủy vé đã xác nhận (confirmed ticket cancellation).
 */
/**
 * Provides the passenger pending payment outcome component for the application.
 */
public enum PassengerPendingPaymentOutcome {
    EXPIRED,
    USER_CANCELLED
}
