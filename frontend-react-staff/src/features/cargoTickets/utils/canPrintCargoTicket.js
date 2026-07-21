/**
 * Pickup-agency staff may print a sticker for non-cancelled tickets
 * whose pickup stop matches the staff agency stop. Payment is not required.
 */
export function canPrintCargoTicket(ticket, agencyStopId) {
    if (!ticket || agencyStopId == null || agencyStopId === '') return false;
    if (ticket.status === 'CANCELLED') return false;
    return Number(ticket.pickupStopId) === Number(agencyStopId);
}
