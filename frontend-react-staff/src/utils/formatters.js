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
 * Lấy thời gian hiện tại theo múi giờ local (UTC+7)
 * VD: 2026-07-02T16:15:23.456Z -> 2026-07-02T16:15
 */
export const getMinDateTime = () => {
    const now = new Date();
    // Bù trừ timezone để hàm toISOString() trả về đúng giờ Việt Nam
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 16); 
};

/**
 * Validate và format biển số xe theo quy chuẩn Việt Nam (Ví dụ: 30B-537.11 hoặc 29A-1234)
 * @param {string} input 
 * @returns {{valid: boolean, data: string}} Trả về trạng thái validate và chuỗi đã format chuẩn
 */
export const validateAndFormatLicensePlate = (input) => {
    if (!input) return { valid: false, data: input };

    const cleanPlate = input.toUpperCase().replace(/[^A-Z0-9]/g, '');

    const match = cleanPlate.match(/^(\d{2})([A-Z]{1,2})(\d{4,5})$/);
    if (!match) {
        return { valid: false, data: input };
    }

    const tinh = match[1];     // Ví dụ: "30"
    const seri = match[2];     // Ví dụ: "B"
    let soDuoi = match[3];     // Ví dụ: "53711" hoặc "1234"

    if (soDuoi.length === 5) {
        soDuoi = soDuoi.slice(0, 3) + '.' + soDuoi.slice(3);
    }

    return { valid: true, data: `${tinh}${seri}-${soDuoi}` };
};