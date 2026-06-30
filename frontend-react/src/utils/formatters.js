/**
 * Format số tiền sang định dạng tiền tệ Việt Nam (VND)
 * VD: 100000 -> "100.000 ₫"
 */
export const formatCurrency = (value) => {
    if (value === null || value === undefined || isNaN(value) || value === '') {
        return '---';
    }
    return new Intl.NumberFormat('vi-VN', { 
        style: 'currency', 
        currency: 'VND' 
    }).format(value);
};

/**
 * Format ngày giờ sang định dạng DD/MM/YYYY HH:mm
 * VD: "2026-06-06T14:30:00Z" -> "06/06/2026 14:30"
 */
export const formatDateTime = (dateInput) => {
    if (!dateInput) return '---';
    
    const date = new Date(dateInput);
    if (isNaN(date.getTime())) return '---';

    const d = date.getDate().toString().padStart(2, '0');
    const m = (date.getMonth() + 1).toString().padStart(2, '0');
    const y = date.getFullYear();
    const h = date.getHours().toString().padStart(2, '0');
    const min = date.getMinutes().toString().padStart(2, '0');

    return `${d}/${m}/${y} ${h}:${min}`;
};

/**
 * Normalizes route name for display (e.g. "A-B" -> "A - B").
 */
export const formatRouteDisplay = (routeName = '') => {
    if (!routeName) return '---';
    return routeName.split('-').map((part) => part.trim()).filter(Boolean).join(' - ');
};

/**
 * Formats ISO/local datetime to Vietnamese weekday + dd/MM/yyyy.
 */
export const formatJourneyDate = (value) => {
    if (!value) return '---';
    const normalized = value.includes('T') ? value : value.replace(' ', 'T');
    const parsedDate = new Date(normalized);
    if (Number.isNaN(parsedDate.getTime())) return '---';
    const formattedDate = parsedDate.toLocaleDateString('vi-VN', {
        weekday: 'long',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    });
    return formattedDate.charAt(0).toUpperCase() + formattedDate.slice(1);
};

/**
 * HH:mm from backend timestamp.
 */
export const formatTime = (value) => {
    if (!value) return '--:--';
    if (value.includes('T')) return value.split('T')[1].substring(0, 5);
    if (value.includes(' ')) return value.split(' ')[1].substring(0, 5);
    return value.substring(0, 5);
};

/**
 * Departure label for trip summary: "12:30, 30/06/2026"
 */
export const formatTripDepartureLabel = (departureTime) => {
    if (!departureTime) return '---';
    const normalized = departureTime.includes('T')
        ? departureTime
        : departureTime.replace(' ', 'T');
    const parsedDate = new Date(normalized);
    if (Number.isNaN(parsedDate.getTime())) return '---';
    const day = parsedDate.getDate().toString().padStart(2, '0');
    const month = (parsedDate.getMonth() + 1).toString().padStart(2, '0');
    const year = parsedDate.getFullYear();
    return `${formatTime(departureTime)}, ${day}/${month}/${year}`;
};
