export const ITINERARY_CHANGE_INITIAL_STATE = {
    departureDate: '',
    selectedRouteId: '',
    candidates: [],
    selectedTripId: null,
    stops: [],
    pickupStopId: '',
    dropoffStopId: '',
    seatList: [],
    selectedTripSeatIds: [],
    preview: null,
    loadingCandidates: false,
    loadingStops: false,
    loadingSeats: false,
    previewing: false,
    submitting: false,
    error: null,
};

function resetTargetSelection(state) {
    return {
        ...state,
        selectedTripId: null,
        stops: [],
        pickupStopId: '',
        dropoffStopId: '',
        seatList: [],
        selectedTripSeatIds: [],
        preview: null,
        loadingStops: false,
        loadingSeats: false,
        previewing: false,
        error: null,
    };
}

export function itineraryChangeReducer(state, action) {
    switch (action.type) {
        case 'INITIALIZE':
            return {
                ...ITINERARY_CHANGE_INITIAL_STATE,
                departureDate: action.departureDate,
                selectedRouteId: action.selectedRouteId,
            };
        case 'ROUTE_CHANGED':
            return {
                ...resetTargetSelection(state),
                selectedRouteId: action.routeId,
                candidates: [],
                loadingCandidates: false,
            };
        case 'DEPARTURE_DATE_CHANGED':
            return {
                ...resetTargetSelection(state),
                departureDate: action.departureDate,
                candidates: [],
                loadingCandidates: false,
            };
        case 'CANDIDATES_LOADING':
            return { ...state, candidates: [], loadingCandidates: true, error: null };
        case 'CANDIDATES_LOADED':
            return { ...state, candidates: action.candidates, loadingCandidates: false };
        case 'TRIP_SELECTED':
            return {
                ...state,
                selectedTripId: action.tripId,
                stops: [],
                pickupStopId: '',
                dropoffStopId: '',
                seatList: [],
                selectedTripSeatIds: [],
                preview: null,
                loadingStops: false,
                loadingSeats: false,
                previewing: false,
                error: null,
            };
        case 'STOPS_LOADING':
            return { ...state, stops: [], loadingStops: true, error: null };
        case 'STOPS_LOADED':
            return {
                ...state,
                stops: action.stops,
                pickupStopId: action.pickupStopId || '',
                dropoffStopId: action.dropoffStopId || '',
                loadingStops: false,
            };
        case 'SEATS_LOADING':
            return { ...state, seatList: [], loadingSeats: true, error: null };
        case 'SEATS_LOADED':
            return { ...state, seatList: action.seats, loadingSeats: false };
        case 'PICKUP_CHANGED':
            return { ...state, pickupStopId: action.stopId, preview: null, error: null };
        case 'DROPOFF_CHANGED':
            return { ...state, dropoffStopId: action.stopId, preview: null, error: null };
        case 'SEATS_CHANGED':
            return { ...state, selectedTripSeatIds: action.seatIds, preview: null, error: null };
        case 'PREVIEW_LOADING':
            return { ...state, preview: null, previewing: true, error: null };
        case 'PREVIEW_LOADED':
            return { ...state, preview: action.preview, previewing: false };
        case 'PREVIEW_CLEARED':
            return { ...state, preview: null, previewing: false };
        case 'SUBMITTING':
            return { ...state, submitting: true, error: null };
        case 'SUBMIT_FINISHED':
            return { ...state, submitting: false };
        case 'REQUEST_FAILED':
            return {
                ...state,
                [action.loadingKey]: false,
                ...(action.clearKey ? { [action.clearKey]: action.clearValue } : {}),
                error: action.message,
            };
        case 'ERROR_SET':
            return { ...state, error: action.message };
        default:
            return state;
    }
}

export function toDateInputValue(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

export function createItineraryChangePayload({
    keepCurrentTrip,
    selectedTripId,
    pickupStopId,
    dropoffStopId,
    selectedTripSeatIds,
}) {
    return {
        newTripId: keepCurrentTrip ? null : selectedTripId,
        pickupStopId: Number(pickupStopId),
        dropoffStopId: Number(dropoffStopId),
        newTripSeatIds: keepCurrentTrip ? undefined : selectedTripSeatIds,
    };
}
