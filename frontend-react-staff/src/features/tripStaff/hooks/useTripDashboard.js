import { useCallback, useEffect, useState } from 'react';
import { tripStaffApi } from '../api/tripStaffApi';

export function useTripDashboard(tripId) {
    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchDashboard = useCallback(async () => {
        if (!tripId) return;
        setLoading(true);
        setError(null);
        try {
            const data = await tripStaffApi.getDashboard(tripId);
            setDashboard(data);
        } catch (err) {
            setError(err.response?.data?.message || 'Không thể tải dữ liệu chuyến đi');
            setDashboard(null);
        } finally {
            setLoading(false);
        }
    }, [tripId]);

    useEffect(() => {
        fetchDashboard();
    }, [fetchDashboard]);

    return { dashboard, loading, error, refetch: fetchDashboard };
}
