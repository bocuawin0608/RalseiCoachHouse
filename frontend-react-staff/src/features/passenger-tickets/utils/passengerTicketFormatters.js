const STATUS_LABELS = {
    PENDING: 'Đang xử lý',
    CONFIRMED: 'Đã xác nhận',
    CHANGED: 'Có thay đổi',
    CANCELLED: 'Đã hủy',
};

export function formatTicketStatus(status) {
    return STATUS_LABELS[status] || status || '—';
}

export function formatDateTime(value) {
    if (!value) return '—';
    return new Date(value).toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
}

export function formatCurrency(value) {
    if (value == null || Number.isNaN(Number(value))) return '—';
    return `${Number(value).toLocaleString('vi-VN')} đ`;
}

export function hasVisibleSearchFilter(filters) {
    return Boolean(
        filters.phone?.trim()
        || filters.ticketCode?.trim()
        || filters.departureDate
    );
}

export function hasSearchTrigger(filters, hiddenTripId) {
    return hasVisibleSearchFilter(filters) || Boolean(hiddenTripId);
}

export function buildSearchParams(filters, hiddenTripId, pageInfo) {
    const params = {
        page: pageInfo.page,
        size: pageInfo.size,
    };

    const phone = filters.phone?.trim();
    const ticketCode = filters.ticketCode?.trim();
    if (phone) params.phone = phone;
    if (ticketCode) params.ticketCode = ticketCode;
    if (filters.status) params.status = filters.status;
    if (filters.routeId) params.routeId = Number(filters.routeId);
    if (filters.departureDate) params.departureDate = filters.departureDate;
    if (hiddenTripId) params.tripId = Number(hiddenTripId);

    return params;
}
