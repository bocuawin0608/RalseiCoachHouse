import { formatJourneyDate, formatRouteDisplay, formatTime, formatTripDepartureLabel } from '../../../utils/formatters';

export function buildTripInfoFromSearchCard(trip) {
    return {
        tripId: trip.tripId,
        routeName: trip.routeName,
        coachTypeName: trip.coachTypeName,
        departureTime: trip.departureTime,
        arrivalTime: trip.arrivalTime ?? null,
        duration: trip.duration ?? null,
        seatPrice: Number(trip.seatPrice ?? 0),
    };
}

export function buildTripShellLabels(tripInfo) {
    if (!tripInfo) return { tripTitle: 'N/A', tripDate: 'N/A' };

    return {
        tripTitle: `Lịch trình: ${formatRouteDisplay(tripInfo.routeName)}`,
        tripDate: formatJourneyDate(tripInfo.departureTime),
    };
}

export function computePickupPresentBy(departureTime, minutesFromStart) {
    if (!departureTime || minutesFromStart == null) return null;

    const normalized = departureTime.includes('T')
        ? departureTime
        : departureTime.replace(' ', 'T');

    const date = new Date(normalized);

    if (Number.isNaN(date.getTime())) return null;

    date.setMinutes(date.getMinutes() + Number(minutesFromStart));

    return date;
}

export function formatPickupPresentByLabel(date) {
    if (!date || Number.isNaN(date.getTime())) return '';

    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();

    return `${hours}:${minutes} ${day}/${month}/${year}`;
}

export { formatTime, formatTripDepartureLabel };