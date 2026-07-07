import { useCallback, useEffect, useMemo, useState } from 'react';
import { tripStaffApi } from '../api/tripStaffApi';

export function useAssignedTrips(date) {
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('');

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

    const filteredTrips = useMemo(() => {
        let list = trips;
        const keyword = search.trim().toLowerCase();
        if (keyword) {
            list = list.filter(
                (t) =>
                    t.routeName?.toLowerCase().includes(keyword) ||
                    t.licensePlate?.toLowerCase().includes(keyword)
            );
        }
        if (statusFilter) {
            list = list.filter((t) => t.tripStatus === statusFilter);
        }
        return list;
    }, [trips, search, statusFilter]);

    return { trips: filteredTrips, allTrips: trips, loading, error, refetch: fetchTrips, search, setSearch, statusFilter, setStatusFilter };
}
