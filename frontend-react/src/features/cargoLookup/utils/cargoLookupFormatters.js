/** Formats money consistently for cargo cards and order details. */
export const formatCargoCurrency = (value) => new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
}).format(Number(value || 0));

/** Formats API date-time values in the Vietnamese locale. */
export const formatCargoDateTime = (value) => value
    ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
    : 'Chưa phân chuyến';

/** Converts backend cargo workflow states into customer-facing Vietnamese. */
export const formatCargoStatus = (status) => ({
    RECEIVED: 'Đã tiếp nhận',
    LOADED: 'Đã xếp hàng',
    ARRIVED: 'Đã đến nơi',
    DELIVERED: 'Đã giao',
    CANCELLED: 'Đã hủy',
    REJECTED: 'Từ chối',
    ABANDONED: 'Không nhận hàng',
}[status] || status || 'Chưa cập nhật');

/** Converts the stored fee-payer code into a readable label. */
export const formatFeePayer = (feePayer) => feePayer === 'RECEIVER' ? 'Người nhận' : 'Người gửi';
