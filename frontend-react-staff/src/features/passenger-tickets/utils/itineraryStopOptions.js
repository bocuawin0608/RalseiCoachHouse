/**
 * [ITINERARY-CHANGE-UX] Cùng quy tắc với PassengerBookingServiceImpl.getStep2InitData:
 * - Điểm đón: coach_stop thuộc thành phố của điểm đầu tuyến
 * - Điểm trả: coach_stop thuộc thành phố của điểm cuối tuyến
 */

export function sortTripStops(stops) {
    return [...(stops || [])].sort((a, b) => a.stopOrder - b.stopOrder);
}

export function resolveRouteEndpointCities(sortedStops) {
    if (!sortedStops.length) {
        return { pickupCity: '', dropoffCity: '' };
    }

    return {
        pickupCity: sortedStops[0].city?.trim() || '',
        dropoffCity: sortedStops[sortedStops.length - 1].city?.trim() || '',
    };
}

function matchesCity(stop, city) {
    if (!city) return false;
    return (stop.city?.trim() || '').localeCompare(city, undefined, { sensitivity: 'accent' }) === 0;
}

export function buildItineraryStopOptions(stops, pickupStopId, dropoffStopId) {
    const sortedStops = sortTripStops(stops);
    const { pickupCity, dropoffCity } = resolveRouteEndpointCities(sortedStops);

    const pickupPool = sortedStops.filter((stop) => matchesCity(stop, pickupCity));
    const dropoffPool = sortedStops.filter((stop) => matchesCity(stop, dropoffCity));

    const dropoffOrder = dropoffStopId
        ? dropoffPool.find((stop) => stop.stopPointId === Number(dropoffStopId))?.stopOrder
        : null;
    const pickupOrder = pickupStopId
        ? pickupPool.find((stop) => stop.stopPointId === Number(pickupStopId))?.stopOrder
        : null;

    const pickupOptions = pickupPool.filter(
        (stop) => dropoffOrder == null || stop.stopOrder < dropoffOrder
    );
    const dropoffOptions = dropoffPool.filter(
        (stop) => pickupOrder == null || stop.stopOrder > pickupOrder
    );

    return {
        sortedStops,
        pickupCity,
        dropoffCity,
        pickupOptions,
        dropoffOptions,
    };
}
