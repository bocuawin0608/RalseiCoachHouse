import { useCallback, useEffect, useState } from 'react';
import { tripApi } from '../api/tripApi';
import { useDebounce } from '../../../hooks/useDebounce';

export function useTrips() {
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [incidents, setIncidents] = useState([]);
    const today = new Date().toLocaleDateString('en-CA');
    const [filters, setFilters] = useState({ date: today, routeId: '', period: '', status: '' });
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: 9,
        totalElements: 0,
        totalPages: 0
    });

    /** Debounce filters to avoid excessive API calls on rapid input */
    const debouncedFilters = useDebounce(filters, 250);

    /** Fetch trip summaries from the API with current filters and pagination */
    const fetchTrips = useCallback(async (silent = false) => {
        if (!silent) setLoading(true);
        setError(null);
        try {
            const [response, incidentResponse] = await Promise.all([
                tripApi.filterTrips({
                    date: debouncedFilters.date || undefined,
                    routeId: debouncedFilters.routeId || undefined,
                    period: debouncedFilters.period || undefined,
                    status: debouncedFilters.status || undefined,
                    page: pageInfo.page,
                    size: pageInfo.size
                }),
                tripApi.getIncidents(debouncedFilters.date || undefined)
            ]);

            setTrips(response.content || []);
            setIncidents(incidentResponse || []);
            setPageInfo(prev => ({
                ...prev,
                totalElements: response.page?.totalElements ?? response.totalElements ?? 0,
                totalPages: response.page?.totalPages ?? response.totalPages ?? 0
            }));
        } catch (err) {
            setError(err.response?.data?.message || 'Có lỗi xảy ra khi lấy dữ liệu.');
            setTrips([]);
            setIncidents([]);
        } finally {
            if (!silent) setLoading(false);
        }
    }, [debouncedFilters, pageInfo.page, pageInfo.size]);

    /** Re-fetch whenever debounced filters or pagination changes */
    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        fetchTrips();
    }, [fetchTrips]);

    useEffect(() => {
        const refreshIncidents = () => {
            if (document.visibilityState === 'visible') fetchTrips(true);
        };
        const timerId = window.setInterval(refreshIncidents, 5000);
        window.addEventListener('focus', refreshIncidents);
        document.addEventListener('visibilitychange', refreshIncidents);
        return () => {
            window.clearInterval(timerId);
            window.removeEventListener('focus', refreshIncidents);
            document.removeEventListener('visibilitychange', refreshIncidents);
        };
    }, [fetchTrips]);

    /** Handle individual filter field change and reset to page 0 */
    const handleFilterChange = (e) => {
        setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    /** Reset all filters and pagination to initial state */
    const handleReset = () => {
        setFilters({ date: today, routeId: '', period: '', status: '' });
        setError(null);
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return {
        trips, incidents, loading, error,
        filters, pageInfo, setPageInfo,
        handleFilterChange, handleReset,
        refetch: fetchTrips
    };
}
