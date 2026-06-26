import { useState, useEffect, useCallback } from 'react';
import { bookingApi } from '../api/bookingApi';

export const useStep2InitData = (tripId, holdToken) => {
    const [initData, setInitData] = useState({
        pickupStopPoints: [],
        dropoffStopPoints: [],
        vouchers: [],
        totalPrice: null,
        basePrice: null,
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchInitData = useCallback(async () => {
        setLoading(true); 
        setError(null);
        try {
            const res = await bookingApi.getStep2InitData(tripId, holdToken);
            setInitData(res);
        } catch (err) {
            setError(err.response?.data?.message || "Không thể lấy thông tin chặng và ưu đãi. Vui lòng thử lại!");
        } finally {
            setLoading(false);
        }
    }, [tripId, holdToken]);

    useEffect(() => {
        if (tripId && holdToken) fetchInitData();
    }, [fetchInitData, tripId, holdToken]);

    return { initData, loading, error };
};