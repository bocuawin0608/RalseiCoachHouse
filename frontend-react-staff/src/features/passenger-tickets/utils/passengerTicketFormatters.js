export const TICKET_STATUS_LABELS = {
    PENDING: { text: 'Đang xử lý', bg: 'primary' },
    CONFIRMED: { text: 'Đã xác nhận', bg: 'success' },
    CHANGED: { text: 'Có thay đổi', bg: 'warning' },
    CANCELLED: { text: 'Đã hủy', bg: 'danger' },
};

const ALLOWED_STATUSES = Object.keys(TICKET_STATUS_LABELS);

/** Default search statuses: active tickets only. */
export const DEFAULT_SEARCH_STATUSES = ['CONFIRMED', 'CHANGED'];

export function formatTicketStatus(status) {
    return TICKET_STATUS_LABELS[status]?.text || status || '—';
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

export function calculateStaffRefundPreview(paymentAmount, refundTierLabel) {
    const amount = Number(paymentAmount);
    if (!amount || Number.isNaN(amount)) return null;
    if (refundTierLabel === '100%') return amount;
    if (refundTierLabel === '50%') return Math.round(amount * 0.5);
    return null;
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

export const EMPTY_FILTERS = {
    phone: '',
    ticketCode: '',
    statuses: [...DEFAULT_SEARCH_STATUSES],
    routeId: '',
    departureDate: '',
};

function normalizeStatusesFromParams(searchParams) {
    const fromMulti = searchParams.getAll('statuses').filter(Boolean);
    if (fromMulti.length > 0) {
        return fromMulti.filter((status) => ALLOWED_STATUSES.includes(status));
    }

    // Backward compat: single `status` query param
    const legacy = searchParams.get('status');
    if (legacy && ALLOWED_STATUSES.includes(legacy)) {
        return [legacy];
    }

    return [...DEFAULT_SEARCH_STATUSES];
}

export function parseFiltersFromSearchParams(searchParams) {
    return {
        phone: searchParams.get('phone') || '',
        ticketCode: searchParams.get('ticketCode') || '',
        statuses: normalizeStatusesFromParams(searchParams),
        routeId: searchParams.get('routeId') || '',
        departureDate: searchParams.get('departureDate') || '',
    };
}

/** Builds URL query params for the search list page. */
export function buildListQueryParams(filters, { tripId, page = 0, size = 20 } = {}) {
    const params = new URLSearchParams();

    const phone = filters.phone?.trim();
    const ticketCode = filters.ticketCode?.trim();
    if (phone) params.set('phone', phone);
    if (ticketCode) params.set('ticketCode', ticketCode);
    (filters.statuses || []).forEach((status) => {
        if (ALLOWED_STATUSES.includes(status)) {
            params.append('statuses', status);
        }
    });
    if (filters.routeId) params.set('routeId', String(filters.routeId));
    if (filters.departureDate) params.set('departureDate', filters.departureDate);
    if (tripId) params.set('tripId', String(tripId));
    if (page > 0) params.set('page', String(page));
    if (size !== 20) params.set('size', String(size));

    return params;
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
    if (filters.statuses?.length) {
        params.statuses = filters.statuses.filter((status) => ALLOWED_STATUSES.includes(status));
    }
    const routeId = Number(filters.routeId);
    if (filters.routeId && Number.isInteger(routeId) && routeId > 0) {
        params.routeId = routeId;
    }
    if (filters.departureDate) params.departureDate = filters.departureDate;
    const tripId = Number(hiddenTripId);
    if (hiddenTripId && Number.isInteger(tripId) && tripId > 0) {
        params.tripId = tripId;
    }

    return params;
}

const TICKET_CODE_PATTERN = /^[A-Za-z0-9_-]{3,64}$/;

function normalizePhoneForSearch(rawPhone) {
    if (!rawPhone) return '';
    const trimmed = rawPhone.trim();
    if (trimmed.startsWith('+84')) {
        return `0${trimmed.slice(3)}`;
    }
    if (trimmed.startsWith('84') && trimmed.length === 11) {
        return `0${trimmed.slice(2)}`;
    }
    return trimmed;
}

/**
 * Validates passenger ticket search filters before calling the API.
 * Returns a user-facing error message, or null when valid.
 */
export function validatePassengerTicketSearchFilters(filters, hiddenTripId) {
    const phone = filters.phone?.trim();
    if (phone) {
        const normalizedPhone = normalizePhoneForSearch(phone);
        if (!/^[0-9]{3,11}$/.test(normalizedPhone)) {
            return 'Số điện thoại phải gồm từ 3 đến 11 chữ số.';
        }
    }

    const ticketCode = filters.ticketCode?.trim();
    if (ticketCode && !TICKET_CODE_PATTERN.test(ticketCode)) {
        return 'Mã vé phải gồm từ 3 đến 64 ký tự chữ, số, gạch dưới hoặc gạch ngang.';
    }

    const statuses = filters.statuses || [];
    if (statuses.some((status) => !ALLOWED_STATUSES.includes(status))) {
        return 'Trạng thái vé không hợp lệ.';
    }

    const routeId = Number(filters.routeId);
    if (filters.routeId && (!Number.isInteger(routeId) || routeId <= 0)) {
        return 'Mã tuyến không hợp lệ.';
    }

    const tripId = Number(hiddenTripId);
    if (hiddenTripId && (!Number.isInteger(tripId) || tripId <= 0)) {
        return 'Mã chuyến không hợp lệ.';
    }

    return null;
}
