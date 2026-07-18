import { useCallback, useEffect, useRef, useState } from 'react';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';

function createHoldToken() {
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
        return crypto.randomUUID().replace(/-/g, '');
    }
    return `${Date.now()}${Math.random().toString(36).slice(2)}`;
}

export function useSeatHoldSession() {
    const holdTokenRef = useRef('');
    const heldSeatsRef = useRef(new Map());
    const mountedRef = useRef(true);
    const pendingOperationsRef = useRef(0);
    const [busy, setBusy] = useState(false);

    const startOperation = useCallback(() => {
        pendingOperationsRef.current += 1;
        setBusy(true);
    }, []);

    const finishOperation = useCallback(() => {
        pendingOperationsRef.current = Math.max(0, pendingOperationsRef.current - 1);
        if (mountedRef.current && pendingOperationsRef.current === 0) setBusy(false);
    }, []);

    const beginSession = useCallback(() => {
        holdTokenRef.current = createHoldToken();
        heldSeatsRef.current.clear();
    }, []);

    const releaseSnapshot = useCallback(async (token, entries) => {
        await Promise.allSettled(entries.map(([tripId, seatIds]) =>
            staffPassengerTicketApi.releaseSeats(tripId, seatIds, token)
        ));
    }, []);

    const rotateSession = useCallback(async () => {
        const previousToken = holdTokenRef.current;
        const previousHolds = Array.from(heldSeatsRef.current.entries())
            .map(([tripId, seatIds]) => [tripId, Array.from(seatIds)]);

        holdTokenRef.current = createHoldToken();
        heldSeatsRef.current.clear();

        if (!previousToken || !previousHolds.length) return;

        startOperation();
        try {
            await releaseSnapshot(previousToken, previousHolds);
        } finally {
            finishOperation();
        }
    }, [finishOperation, releaseSnapshot, startOperation]);

    const lockSeat = useCallback(async (tripId, tripSeatId) => {
        if (!tripId || !tripSeatId || !holdTokenRef.current) return;
        startOperation();
        try {
            await staffPassengerTicketApi.lockSeats(
                tripId,
                [tripSeatId],
                holdTokenRef.current,
                'ITINERARY'
            );
            const heldForTrip = heldSeatsRef.current.get(tripId) || new Set();
            heldForTrip.add(tripSeatId);
            heldSeatsRef.current.set(tripId, heldForTrip);
        } finally {
            finishOperation();
        }
    }, [finishOperation, startOperation]);

    const releaseSeat = useCallback(async (tripId, tripSeatId) => {
        if (!tripId || !tripSeatId || !holdTokenRef.current) return;
        startOperation();
        try {
            await staffPassengerTicketApi.releaseSeats(tripId, [tripSeatId], holdTokenRef.current);
            const heldForTrip = heldSeatsRef.current.get(tripId);
            heldForTrip?.delete(tripSeatId);
            if (heldForTrip && heldForTrip.size === 0) heldSeatsRef.current.delete(tripId);
        } finally {
            finishOperation();
        }
    }, [finishOperation, startOperation]);

    const forgetSession = useCallback(() => {
        heldSeatsRef.current.clear();
        holdTokenRef.current = '';
    }, []);

    useEffect(() => {
        mountedRef.current = true;
        const heldSeats = heldSeatsRef.current;
        return () => {
            mountedRef.current = false;
            const token = holdTokenRef.current;
            const entries = Array.from(heldSeats.entries())
                .map(([tripId, seatIds]) => [tripId, Array.from(seatIds)]);
            heldSeats.clear();
            if (token && entries.length) void releaseSnapshot(token, entries);
        };
    }, [releaseSnapshot]);

    return {
        busy,
        holdTokenRef,
        beginSession,
        rotateSession,
        lockSeat,
        releaseSeat,
        forgetSession,
    };
}
