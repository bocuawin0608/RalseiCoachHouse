import { useCallback, useEffect, useMemo, useState } from 'react';
import { staffTripInfoApi } from '../api/staffTripInfoApi';
import { useDebounce } from '../../../hooks/useDebounce';

/** Returns today's date as yyyy-MM-dd for date inputs and API filters. */
const getTodayInputValue = () => new Date().toLocaleDateString('en-CA');

/** Creates fresh default filters so "today" is recalculated on reset. */
const createDefaultFilters = () => ({
    date: getTodayInputValue(),
    city: '',
    timeFrom: '',
    timeTo: '',
    coachTypeKeyword: '',
    driverName: '',
    priceRanges: [],
    statuses: [],
});

/**
 * Owns data loading, pagination, and filter state for the ticket-staff trip
 * lookup screen. Keeping this logic out of the page keeps rendering code calm.
 */
export function useStaffTripInfo() {
    const [filters, setFilters] = useState(createDefaultFilters);
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0,
    });

    const debouncedFilters = useDebounce(filters, 250);

    /** Build API params while dropping empty form controls. */
    const queryParams = useMemo(() => ({
        date: debouncedFilters.date || getTodayInputValue(),
        city: debouncedFilters.city || undefined,
        timeFrom: debouncedFilters.timeFrom || undefined,
        timeTo: debouncedFilters.timeTo || undefined,
        coachTypeKeyword: debouncedFilters.coachTypeKeyword || undefined,
        driverName: debouncedFilters.driverName || undefined,
        priceRanges: debouncedFilters.priceRanges,
        statuses: debouncedFilters.statuses,
        page: pageInfo.page,
        size: pageInfo.size,
    }), [debouncedFilters, pageInfo.page, pageInfo.size]);

    /** Loads upcoming trips using the current filters and page. */
    const fetchTrips = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            const response = await staffTripInfoApi.searchTrips(queryParams);
            setTrips(response.content || []);
            setPageInfo((prev) => ({
                ...prev,
                totalElements: response.totalElements || 0,
                totalPages: response.totalPages || 0,
            }));
        } catch (err) {
            setTrips([]);
            setError(err.response?.data?.message || 'Không thể tải danh sách chuyến xe.');
        } finally {
            setLoading(false);
        }
    }, [queryParams]);

    useEffect(() => {
        fetchTrips();
    }, [fetchTrips]);

    /** Handles text/select/time filters and resets pagination. */
    const handleFilterChange = (event) => {
        const { name, value } = event.target;
        setFilters((prev) => ({ ...prev, [name]: value }));
        setPageInfo((prev) => ({ ...prev, page: 0 }));
    };

    /**
     * Updates option filters.
     * Price remains multi-select; status is radio-backed and therefore stores
     * either one value or an empty array for "Tất cả".
     */
    const handleCheckboxChange = (groupName, value) => {
        if (groupName === 'statuses') {
            setFilters((prev) => ({ ...prev, statuses: value ? [value] : [] }));
            setPageInfo((prev) => ({ ...prev, page: 0 }));
            return;
        }

        setFilters((prev) => {
            const currentValues = prev[groupName] || [];
            const nextValues = currentValues.includes(value)
                ? currentValues.filter((item) => item !== value)
                : [...currentValues, value];
            return { ...prev, [groupName]: nextValues };
        });
        setPageInfo((prev) => ({ ...prev, page: 0 }));
    };

    /** Clears every filter back to the default upcoming-trip view. */
    const handleReset = () => {
        setFilters(createDefaultFilters());
        setPageInfo((prev) => ({ ...prev, page: 0 }));
        setError('');
    };

    return {
        filters,
        trips,
        loading,
        error,
        pageInfo,
        setPageInfo,
        handleFilterChange,
        handleCheckboxChange,
        handleReset,
        refetch: fetchTrips,
        today: getTodayInputValue(),
    };
}
