import { useCallback, useEffect, useState } from 'react';
import { tripStaffApi } from '../api/tripStaffApi';

export function useAssignedTrips(date) {
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchTrips = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await tripStaffApi.getAssignedTrips(date);
            setTrips(data || []);
        } catch (err) {
            setError(err.response?.data?.message || 'Không thể tải danh sách chuyến đi');
            setTrips([]);
        } finally {
            setLoading(false);
        }
    }, [date]);

    useEffect(() => {
        fetchTrips();
    }, [fetchTrips]);

    return { trips, loading, error, refetch: fetchTrips };
}
