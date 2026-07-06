import { useCallback, useEffect, useState } from 'react';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';

export function usePassengerTicketDetail(ticketCode) {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchDetail = useCallback(async () => {
        if (!ticketCode) {
            setData(null);
            setError(null);
            setLoading(false);
            return null;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await staffPassengerTicketApi.getDetail(ticketCode);
            setData(response);
            return response;
        } catch (requestError) {
            setData(null);
            setError(requestError.response?.data?.message || 'Không thể tải chi tiết vé.');
            return null;
        } finally {
            setLoading(false);
        }
    }, [ticketCode]);

    const applyDetail = useCallback((nextDetail) => {
        setData(nextDetail);
        setError(null);
    }, []);

    useEffect(() => {
        fetchDetail();
    }, [fetchDetail]);

    return { data, loading, error, refetch: fetchDetail, applyDetail };
}
