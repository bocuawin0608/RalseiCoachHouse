import { useCallback, useEffect, useMemo, useReducer, useRef } from 'react';
import { useRouteDropdown } from '../../../hooks/useRouteDropdown';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';
import { buildItineraryStopOptions } from '../utils/itineraryStopOptions';
import {
    createItineraryChangePayload,
    ITINERARY_CHANGE_INITIAL_STATE,
    itineraryChangeReducer,
    toDateInputValue,
} from '../utils/itineraryChangeState';
import { buildSeatLayout } from '../components/TripSeatMapGrid';
import { useSeatHoldSession } from './useSeatHoldSession';

const PREVIEW_DELAY_MS = 200;

function getErrorMessage(error, fallback) {
    return error.response?.data?.message || fallback;
}

/** Draft workflow for same-trip stop change or transfer; confirm is owned by ChangeTicketSessionModal. */
export function useItineraryChangeWorkflow({
    isOpen,
    mode,
    ticket,
}) {
    const isTransferMode = mode === 'transfer';
    const keepCurrentTrip = !isTransferMode;
    const [state, dispatch] = useReducer(itineraryChangeReducer, ITINERARY_CHANGE_INITIAL_STATE);
    const requestIdsRef = useRef({ candidates: 0, stops: 0, seats: 0, preview: 0 });
    const {
        busy: locking,
        holdTokenRef,
        beginSession,
        rotateSession,
        lockSeat,
        releaseSeat,
        forgetSession,
    } = useSeatHoldSession();
    const { routes: routeOptions, loading: loadingRoutes } = useRouteDropdown(isOpen && isTransferMode);

    const confirmedSeatCount = useMemo(
        () => ticket?.seats?.filter((seat) => seat.status === 'CONFIRMED').length ?? 0,
        [ticket]
    );
    const layout = useMemo(() => buildSeatLayout(state.seatList), [state.seatList]);
    const stopOptions = useMemo(
        () => buildItineraryStopOptions(state.stops, state.pickupStopId, state.dropoffStopId),
        [state.stops, state.pickupStopId, state.dropoffStopId]
    );
    const canSearchTransferTrips = Boolean(
        isTransferMode && state.departureDate && state.selectedRouteId
    );

    const invalidateRequests = useCallback(() => {
        Object.keys(requestIdsRef.current).forEach((key) => {
            requestIdsRef.current[key] += 1;
        });
    }, []);

    const loadStops = useCallback(async (
        tripId,
        initialPickupStopId = null,
        initialDropoffStopId = null
    ) => {
        if (!tripId) return;
        const requestId = ++requestIdsRef.current.stops;
        dispatch({ type: 'STOPS_LOADING' });
        try {
            const response = await staffPassengerTicketApi.getTripStops(tripId);
            if (requestId !== requestIdsRef.current.stops) return;
            dispatch({
                type: 'STOPS_LOADED',
                stops: response || [],
                pickupStopId: initialPickupStopId ? String(initialPickupStopId) : '',
                dropoffStopId: initialDropoffStopId ? String(initialDropoffStopId) : '',
            });
        } catch (error) {
            if (requestId !== requestIdsRef.current.stops) return;
            dispatch({
                type: 'REQUEST_FAILED',
                loadingKey: 'loadingStops',
                clearKey: 'stops',
                clearValue: [],
                message: getErrorMessage(error, 'Không thể tải điểm dừng.'),
            });
        }
    }, []);

    const loadSeatMap = useCallback(async (tripId) => {
        if (!tripId) return;
        const requestId = ++requestIdsRef.current.seats;
        dispatch({ type: 'SEATS_LOADING' });
        try {
            const response = await staffPassengerTicketApi.getTripSeatMap(tripId);
            if (requestId !== requestIdsRef.current.seats) return;
            dispatch({ type: 'SEATS_LOADED', seats: response || [] });
        } catch (error) {
            if (requestId !== requestIdsRef.current.seats) return;
            dispatch({
                type: 'REQUEST_FAILED',
                loadingKey: 'loadingSeats',
                clearKey: 'seatList',
                clearValue: [],
                message: getErrorMessage(error, 'Không thể tải sơ đồ ghế.'),
            });
        }
    }, []);

    useEffect(() => {
        if (!isOpen || !ticket) return undefined;

        beginSession();
        invalidateRequests();
        dispatch({
            type: 'INITIALIZE',
            departureDate: ticket.departureTime
                ? toDateInputValue(new Date(ticket.departureTime))
                : toDateInputValue(new Date()),
            selectedRouteId: ticket.routeId ? String(ticket.routeId) : '',
        });

        if (!isTransferMode) {
            void loadStops(ticket.tripId, ticket.pickupStopId, ticket.dropoffStopId);
        }

        return () => {
            invalidateRequests();
            void rotateSession();
        };
    }, [
        beginSession,
        invalidateRequests,
        isOpen,
        isTransferMode,
        loadStops,
        rotateSession,
        ticket,
    ]);

    useEffect(() => {
        if (!isOpen || !isTransferMode || !canSearchTransferTrips || !ticket?.ticketCode) return;

        const requestId = ++requestIdsRef.current.candidates;
        dispatch({ type: 'CANDIDATES_LOADING' });
        staffPassengerTicketApi.getTransferCandidates(ticket.ticketCode, {
            departureDate: state.departureDate,
            routeId: Number(state.selectedRouteId),
            excludeCurrentTrip: true,
        }).then((response) => {
            if (requestId !== requestIdsRef.current.candidates) return;
            dispatch({ type: 'CANDIDATES_LOADED', candidates: response || [] });
        }).catch((error) => {
            if (requestId !== requestIdsRef.current.candidates) return;
            dispatch({
                type: 'REQUEST_FAILED',
                loadingKey: 'loadingCandidates',
                clearKey: 'candidates',
                clearValue: [],
                message: getErrorMessage(error, 'Không thể tải danh sách chuyến.'),
            });
        });
    }, [
        canSearchTransferTrips,
        isOpen,
        isTransferMode,
        state.departureDate,
        state.selectedRouteId,
        ticket?.ticketCode,
    ]);

    useEffect(() => {
        if (!isOpen || !isTransferMode || !state.selectedTripId) return;
        void loadStops(state.selectedTripId);
        void loadSeatMap(state.selectedTripId);
    }, [isOpen, isTransferMode, loadSeatMap, loadStops, state.selectedTripId]);

    /** Same-trip: no diff vs current stops (or stops not loaded yet). Transfer: untouched until a trip is chosen. */
    const hasNoChanges = keepCurrentTrip
        ? (
            !state.pickupStopId
            || !state.dropoffStopId
            || (
                Number(state.pickupStopId) === Number(ticket?.pickupStopId)
                && Number(state.dropoffStopId) === Number(ticket?.dropoffStopId)
            )
        )
        : !state.selectedTripId;

    useEffect(() => {
        const seatsReady = keepCurrentTrip
            || state.selectedTripSeatIds.length === confirmedSeatCount;
        if (
            !isOpen
            || !ticket?.ticketCode
            || !state.pickupStopId
            || !state.dropoffStopId
            || !seatsReady
            || hasNoChanges
        ) {
            requestIdsRef.current.preview += 1;
            dispatch({ type: 'PREVIEW_CLEARED' });
            return undefined;
        }

        const requestId = ++requestIdsRef.current.preview;
        const timerId = window.setTimeout(async () => {
            dispatch({ type: 'PREVIEW_LOADING' });
            try {
                const response = await staffPassengerTicketApi.previewItinerary(
                    ticket.ticketCode,
                    createItineraryChangePayload({
                        keepCurrentTrip,
                        selectedTripId: state.selectedTripId,
                        pickupStopId: state.pickupStopId,
                        dropoffStopId: state.dropoffStopId,
                        selectedTripSeatIds: state.selectedTripSeatIds,
                    })
                );
                if (requestId !== requestIdsRef.current.preview) return;
                dispatch({ type: 'PREVIEW_LOADED', preview: response });
            } catch (error) {
                if (requestId !== requestIdsRef.current.preview) return;
                dispatch({
                    type: 'REQUEST_FAILED',
                    loadingKey: 'previewing',
                    clearKey: 'preview',
                    clearValue: null,
                    message: getErrorMessage(error, 'Không thể xem trước giá vé.'),
                });
            }
        }, PREVIEW_DELAY_MS);

        return () => window.clearTimeout(timerId);
    }, [
        confirmedSeatCount,
        hasNoChanges,
        isOpen,
        keepCurrentTrip,
        state.dropoffStopId,
        state.pickupStopId,
        state.selectedTripId,
        state.selectedTripSeatIds,
        ticket?.ticketCode,
    ]);

    const resetHeldSeats = useCallback(() => {
        requestIdsRef.current.preview += 1;
        return rotateSession();
    }, [rotateSession]);

    const handleRouteChange = useCallback((routeId) => {
        void resetHeldSeats();
        requestIdsRef.current.candidates += 1;
        requestIdsRef.current.stops += 1;
        requestIdsRef.current.seats += 1;
        dispatch({ type: 'ROUTE_CHANGED', routeId });
    }, [resetHeldSeats]);

    const handleDepartureDateChange = useCallback((departureDate) => {
        void resetHeldSeats();
        requestIdsRef.current.candidates += 1;
        requestIdsRef.current.stops += 1;
        requestIdsRef.current.seats += 1;
        dispatch({ type: 'DEPARTURE_DATE_CHANGED', departureDate });
    }, [resetHeldSeats]);

    const handleTripChange = useCallback((tripId) => {
        void resetHeldSeats();
        requestIdsRef.current.stops += 1;
        requestIdsRef.current.seats += 1;
        dispatch({ type: 'TRIP_SELECTED', tripId });
    }, [resetHeldSeats]);

    const handleSeatClick = useCallback(async (clickedSeat) => {
        if (keepCurrentTrip || locking || clickedSeat.status !== 'AVAILABLE') return;
        const seatId = clickedSeat.tripSeatId;
        const isSelected = state.selectedTripSeatIds.includes(seatId);

        if (!isSelected && state.selectedTripSeatIds.length >= confirmedSeatCount) {
            dispatch({ type: 'ERROR_SET', message: `Chỉ được chọn tối đa ${confirmedSeatCount} ghế.` });
            return;
        }

        try {
            if (isSelected) {
                await releaseSeat(state.selectedTripId, seatId);
                dispatch({
                    type: 'SEATS_CHANGED',
                    seatIds: state.selectedTripSeatIds.filter((id) => id !== seatId),
                });
            } else {
                await lockSeat(state.selectedTripId, seatId);
                dispatch({
                    type: 'SEATS_CHANGED',
                    seatIds: [...state.selectedTripSeatIds, seatId],
                });
            }
        } catch (error) {
            dispatch({
                type: 'ERROR_SET',
                message: getErrorMessage(
                    error,
                    isSelected ? 'Không thể bỏ giữ ghế. Vui lòng thử lại.' : 'Không thể giữ ghế. Vui lòng thử lại.'
                ),
            });
        }
    }, [
        confirmedSeatCount,
        keepCurrentTrip,
        lockSeat,
        locking,
        releaseSeat,
        state.selectedTripId,
        state.selectedTripSeatIds,
    ]);

    const canSubmit = Boolean(
        state.preview?.eligible
        && state.pickupStopId
        && state.dropoffStopId
        && !hasNoChanges
        && (keepCurrentTrip || (
            state.selectedTripId
            && state.selectedTripSeatIds.length === confirmedSeatCount
        ))
    );

    const getDraft = useCallback(() => {
        if (hasNoChanges || !canSubmit) {
            return {
                ready: false,
                payload: null,
                holdToken: null,
            };
        }

        return {
            ready: true,
            payload: createItineraryChangePayload({
                keepCurrentTrip,
                selectedTripId: state.selectedTripId,
                pickupStopId: state.pickupStopId,
                dropoffStopId: state.dropoffStopId,
                selectedTripSeatIds: state.selectedTripSeatIds,
            }),
            holdToken: keepCurrentTrip ? null : holdTokenRef.current,
        };
    }, [
        canSubmit,
        hasNoChanges,
        holdTokenRef,
        keepCurrentTrip,
        state.dropoffStopId,
        state.pickupStopId,
        state.selectedTripId,
        state.selectedTripSeatIds,
    ]);

    return {
        ...state,
        isTransferMode,
        keepCurrentTrip,
        locking,
        interactionDisabled: locking,
        routeOptions,
        loadingRoutes,
        confirmedSeatCount,
        layout,
        ...stopOptions,
        canSearchTransferTrips,
        hasNoChanges,
        canSubmit,
        getDraft,
        forgetSession,
        handleRouteChange,
        handleDepartureDateChange,
        handleTripChange,
        handlePickupChange: (stopId) => dispatch({ type: 'PICKUP_CHANGED', stopId }),
        handleDropoffChange: (stopId) => dispatch({ type: 'DROPOFF_CHANGED', stopId }),
        handleSeatClick,
    };
}
