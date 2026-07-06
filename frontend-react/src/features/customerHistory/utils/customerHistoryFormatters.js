/**
 * Formats a backend timestamp using the Vietnamese customer locale.
 */
export const formatCustomerDateTime = (value) => {
    if (!value) return 'Chưa cập nhật';
    const parsedValue = new Date(value);
    if (Number.isNaN(parsedValue.getTime())) return 'Chưa cập nhật';

    return new Intl.DateTimeFormat('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    }).format(parsedValue);
};

/**
 * Formats ticket prices as Vietnamese đồng without hardcoded separators.
 */
export const formatCustomerCurrency = (value) => new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
}).format(Number(value || 0));

/**
 * Converts backend ticket statuses to customer-readable Vietnamese labels.
 */
export const formatCustomerTicketStatus = (status) => ({
    CONFIRMED: 'Đã thanh toán',
    PENDING: 'Chờ thanh toán',
    CANCELLED: 'Đã hủy',
    EXPIRED: 'Đã hết hạn',
}[status] || status || 'Chưa cập nhật');
