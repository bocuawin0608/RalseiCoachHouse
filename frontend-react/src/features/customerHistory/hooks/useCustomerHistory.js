import { useEffect, useState } from 'react';
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

    return state;
};
