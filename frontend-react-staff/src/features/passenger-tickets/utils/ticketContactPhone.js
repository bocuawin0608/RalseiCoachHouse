/** Contact phone for staff OTP: first CONFIRMED seat with a phone (matches BE). */
export function resolveTicketContactPhone(ticket) {
    const seats = ticket?.seats || [];
    const seat = seats
        .filter((item) => item.status === 'CONFIRMED' && item.phone)
        .sort((a, b) => Number(a.ticketDetailId) - Number(b.ticketDetailId))[0];
    return seat?.phone?.trim() || '';
}
