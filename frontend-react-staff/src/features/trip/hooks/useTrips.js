import { useCallback, useEffect, useState } from 'react';
import { tripApi } from '../api/tripApi';
import { useDebounce } from '../../../hooks/useDebounce';

export function useTrips() {
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({ date: '' });
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0
    });

    /** Debounce filters to avoid excessive API calls on rapid input */
    const debouncedFilters = useDebounce(filters, 250);

    /** Fetch trip summaries from the API with current filters and pagination */
    const fetchTrips = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await tripApi.filterTrips({
                date: debouncedFilters.date || undefined,
                page: pageInfo.page,
                size: pageInfo.size
            });

            setTrips(response.content || []);
            setPageInfo(prev => ({
                ...prev,
                totalElements: response.page?.totalElements ?? response.totalElements ?? 0,
                totalPages: response.page?.totalPages ?? response.totalPages ?? 0
            }));
        } catch (err) {
            setError(err.response?.data?.message || 'Có lỗi xảy ra khi lấy dữ liệu.');
            setTrips([]);
        } finally {
            setLoading(false);
        }
    }, [debouncedFilters, pageInfo.page, pageInfo.size]);

    /** Re-fetch whenever debounced filters or pagination changes */
    useEffect(() => {
        fetchTrips();
    }, [fetchTrips]);

    /** Handle individual filter field change and reset to page 0 */
    const handleFilterChange = (e) => {
        setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    /** Reset all filters and pagination to initial state */
    const handleReset = () => {
        setFilters({ date: '' });
        setError(null);
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return {
        trips, loading, error,
        filters, pageInfo, setPageInfo,
        handleFilterChange, handleReset,
        refetch: fetchTrips
    };
}