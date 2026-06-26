import { useState, useEffect } from 'react';
import { bookingApi } from '../api/bookingApi';

export const usePriceCalculation = (tripId, holdToken, { pickupStopId, dropoffStopId, voucherId }) => {
    const [priceData, setPriceData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!tripId || !holdToken) return;

        let cancelled = false;

        const fetchPrice = async () => {
            setLoading(true);
            setError(null);
            try {
                const res = await bookingApi.calculatePrice(
                    tripId,
                    {
                        pickupStopId: pickupStopId ? Number(pickupStopId) : null,
                        dropoffStopId: dropoffStopId ? Number(dropoffStopId) : null,
                        voucherId: voucherId ? Number(voucherId) : null,
                    },
                    holdToken
                );
                if (!cancelled) setPriceData(res);
            } catch (err) {
                if (!cancelled) {
                    setError(err.response?.data?.message || 'Không thể tính giá. Vui lòng thử lại!');
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        fetchPrice();
        return () => { cancelled = true; };
    }, [tripId, holdToken, pickupStopId, dropoffStopId, voucherId]);

    return { priceData, loading, error };
};
