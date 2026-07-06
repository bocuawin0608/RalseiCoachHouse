import { useCallback, useEffect, useState } from 'react';
import { customerHistoryApi } from '../api/customerHistoryApi';

/**
 * Loads customer booking history while preventing updates after component unmount.
 */
export const useCustomerHistory = () => {
    const [state, setState] = useState({ data: [], loading: true, error: '' });

    useEffect(() => {
        let isMounted = true;

        customerHistoryApi.getHistory()
            .then((data) => {
                if (isMounted) setState({ data, loading: false, error: '' });
            })
            .catch(() => {
                if (isMounted) {
                    setState({ data: [], loading: false, error: 'Không thể tải lịch sử đặt vé.' });
                }
            });

        return () => { isMounted = false; };
    }, []);

    /** Applies a committed server-side change to one booking without refetching the list. */
    const updateBooking = useCallback((ticketCode, updates) => {
        setState((current) => ({
            ...current,
            data: current.data.map((booking) => booking.ticketCode === ticketCode
                ? { ...booking, ...updates }
                : booking),
        }));
    }, []);

    return { ...state, updateBooking };
};
