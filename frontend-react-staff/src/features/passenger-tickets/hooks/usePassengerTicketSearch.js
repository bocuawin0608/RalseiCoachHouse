import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';
import {
    buildListQueryParams,
    buildSearchParams,
    EMPTY_FILTERS,
    hasSearchTrigger,
    parseFiltersFromSearchParams,
    validatePassengerTicketSearchFilters,
} from '../utils/passengerTicketFormatters';

export function usePassengerTicketSearch() {
    const [searchParams, setSearchParams] = useSearchParams();
    const hiddenTripId = searchParams.get('tripId');

    const [filters, setFilters] = useState(() => parseFiltersFromSearchParams(searchParams));
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [hasSearched, setHasSearched] = useState(false);
    const [pageInfo, setPageInfo] = useState({
        page: Number(searchParams.get('page') || 0),
        size: Number(searchParams.get('size') || 20),
        totalElements: 0,
        totalPages: 0,
    });

    const fetchSearch = useCallback(async (nextFilters, nextPage, nextSize, tripId) => {
        if (!hasSearchTrigger(nextFilters, tripId)) {
            setError(null);
            setData([]);
            setHasSearched(false);
            setPageInfo((prev) => ({ ...prev, page: 0, totalElements: 0, totalPages: 0 }));
            return;
        }

        const validationError = validatePassengerTicketSearchFilters(nextFilters, tripId);
        if (validationError) {
            setError(validationError);
            setData([]);
            setHasSearched(true);
            setPageInfo((prev) => ({ ...prev, page: 0, totalElements: 0, totalPages: 0 }));
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await staffPassengerTicketApi.search(
                buildSearchParams(nextFilters, tripId, { page: nextPage, size: nextSize })
            );

            setData(response.content || []);
            setHasSearched(true);
            setPageInfo({
                page: response.pageNumber ?? nextPage,
                size: response.pageSize ?? nextSize,
                totalElements: response.totalElements ?? 0,
                totalPages: response.totalPages ?? 0,
            });
        } catch (requestError) {
            setData([]);
            setHasSearched(true);
            setError(requestError.response?.data?.message || 'Không thể tra cứu vé.');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        const urlFilters = parseFiltersFromSearchParams(searchParams);
        const tripId = searchParams.get('tripId');
        const page = Number(searchParams.get('page') || 0);
        const size = Number(searchParams.get('size') || 20);

        setFilters(urlFilters);

        if (hasSearchTrigger(urlFilters, tripId)) {
            fetchSearch(urlFilters, page, size, tripId);
        } else {
            setError(null);
            setData([]);
            setHasSearched(false);
            setPageInfo((prev) => ({
                ...prev,
                page: 0,
                totalElements: 0,
                totalPages: 0,
            }));
        }
    }, [searchParams, fetchSearch]);

    const handleFilterChange = (event) => {
        const { name, value } = event.target;
        setFilters((current) => ({ ...current, [name]: value }));
    };

    const handleStatusCheckboxChange = (event) => {
        const { value, checked } = event.target;
        setFilters((current) => {
            const currentStatuses = current.statuses || [];
            const nextStatuses = checked
                ? [...currentStatuses, value]
                : currentStatuses.filter((status) => status !== value);
            return { ...current, statuses: nextStatuses };
        });
    };

    const handleReset = () => {
        setFilters({ ...EMPTY_FILTERS, statuses: [...EMPTY_FILTERS.statuses] });
        setSearchParams({}, { replace: true });
    };

    const handleSearch = () => {
        const validationError = validatePassengerTicketSearchFilters(filters, hiddenTripId);
        if (validationError) {
            setError(validationError);
            return;
        }

        const params = buildListQueryParams(filters, {
            tripId: hiddenTripId,
            page: 0,
            size: pageInfo.size,
        });
        setSearchParams(params, { replace: true });
    };

    const handlePageChange = (updater) => {
        setPageInfo((prev) => {
            const next = typeof updater === 'function' ? updater(prev) : updater;
            const params = buildListQueryParams(filters, {
                tripId: hiddenTripId,
                page: next.page,
                size: next.size,
            });
            setSearchParams(params, { replace: true });
            return next;
        });
    };

    return {
        filters,
        data,
        loading,
        error,
        hasSearched,
        hiddenTripId,
        pageInfo,
        setPageInfo: handlePageChange,
        handleFilterChange,
        handleStatusCheckboxChange,
        handleReset,
        handleSearch,
    };
}
