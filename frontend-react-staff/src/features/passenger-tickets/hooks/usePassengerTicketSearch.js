import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';
import { buildSearchParams, hasSearchTrigger } from '../utils/passengerTicketFormatters';

const EMPTY_FILTERS = {
    phone: '',
    ticketCode: '',
    status: '',
    routeId: '',
    departureDate: '',
};

export function usePassengerTicketSearch() {
    const [searchParams] = useSearchParams();
    const hiddenTripId = searchParams.get('tripId');

    const [filters, setFilters] = useState(EMPTY_FILTERS);
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [hasSearched, setHasSearched] = useState(false);
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
    });

    const fetchSearch = useCallback(async (nextFilters, nextPage, nextSize, tripId) => {
        if (!hasSearchTrigger(nextFilters, tripId)) {
            setError(null);
            setData([]);
            setHasSearched(false);
            setPageInfo((prev) => ({ ...prev, totalElements: 0, totalPages: 0 }));
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
        if (hiddenTripId) {
            fetchSearch(filters, 0, pageInfo.size, hiddenTripId);
        }
        // Auto-search only when deep-linking with tripId.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [hiddenTripId]);

    const handleFilterChange = (event) => {
        const { name, value } = event.target;
        setFilters((current) => ({ ...current, [name]: value }));
    };

    const handleReset = () => {
        setFilters(EMPTY_FILTERS);
        setError(null);
        setData([]);
        setHasSearched(false);
        setPageInfo((prev) => ({ ...prev, page: 0, totalElements: 0, totalPages: 0 }));
    };

    const handleSearch = () => {
        fetchSearch(filters, 0, pageInfo.size, hiddenTripId);
    };

    const handlePageChange = (updater) => {
        setPageInfo((prev) => {
            const next = typeof updater === 'function' ? updater(prev) : updater;
            if (hasSearched || hiddenTripId) {
                fetchSearch(filters, next.page, next.size, hiddenTripId);
            }
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
        handleReset,
        handleSearch,
        refetch: () => fetchSearch(filters, pageInfo.page, pageInfo.size, hiddenTripId),
    };
}
