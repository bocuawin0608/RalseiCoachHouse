import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { refundApi } from '../api/refundApi';
import {
    buildRefundListQueryParams,
    buildRefundSearchApiParams,
    EMPTY_REFUND_FILTERS,
    parsePageInfo,
    parseRefundFiltersFromSearchParams,
    parseRefundTab,
    validateRefundSearchFilters,
} from '../utils/refundFormatters';

const DEFAULT_TAB = 'passenger';

export function useRefundList() {
    const [searchParams, setSearchParams] = useSearchParams();
    const activeTab = parseRefundTab(searchParams);

    const [filters, setFilters] = useState(() => parseRefundFiltersFromSearchParams(searchParams));
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [pageInfo, setPageInfo] = useState(() => {
        const parsed = parsePageInfo(searchParams);
        return {
            ...parsed,
            totalElements: 0,
            totalPages: 0,
        };
    });

    const fetchRefunds = useCallback(async (nextFilters, nextPageInfo, tab) => {
        if (tab !== DEFAULT_TAB) {
            setError(null);
            setData([]);
            setPageInfo((prev) => ({
                ...prev,
                totalElements: 0,
                totalPages: 0,
            }));
            return;
        }

        const dateRangeError = validateRefundSearchFilters(nextFilters);
        if (dateRangeError) {
            setError(dateRangeError);
            setData([]);
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await refundApi.searchPassenger(
                buildRefundSearchApiParams(nextFilters, nextPageInfo)
            );

            setData(response.content || []);
            setPageInfo({
                page: response.pageNumber ?? nextPageInfo.page,
                size: response.pageSize ?? nextPageInfo.size,
                totalElements: response.totalElements ?? 0,
                totalPages: response.totalPages ?? 0,
            });
        } catch (requestError) {
            setData([]);
            setError(requestError.response?.data?.message || 'Không thể tải danh sách hoàn tiền.');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        const nextTab = parseRefundTab(searchParams);
        const nextFilters = parseRefundFiltersFromSearchParams(searchParams);
        const nextPageInfo = parsePageInfo(searchParams);

        setFilters(nextFilters);
        fetchRefunds(nextFilters, nextPageInfo, nextTab);
    }, [searchParams, fetchRefunds]);

    useEffect(() => {
        if (!searchParams.has('status')) {
            const params = buildRefundListQueryParams(EMPTY_REFUND_FILTERS, {
                tab: parseRefundTab(searchParams),
                ...parsePageInfo(searchParams),
            });
            setSearchParams(params, { replace: true });
        }
    }, [searchParams, setSearchParams]);

    const handleFilterChange = (event) => {
        const { name, value } = event.target;
        setFilters((current) => ({ ...current, [name]: value }));
        setError(null);
    };

    const handleReset = () => {
        setFilters(EMPTY_REFUND_FILTERS);
        setError(null);

        const resetPageInfo = { page: 0, size: pageInfo.size };
        const params = buildRefundListQueryParams(EMPTY_REFUND_FILTERS, {
            tab: activeTab,
            ...resetPageInfo,
        });

        const nextQuery = params.toString();
        const currentQuery = searchParams.toString();

        setSearchParams(params, { replace: true });

        // When filters were edited locally but never synced to URL, query string
        // may not change and the searchParams effect will not rerun.
        if (nextQuery === currentQuery) {
            fetchRefunds(EMPTY_REFUND_FILTERS, resetPageInfo, activeTab);
        }
    };

    const handleSearch = () => {
        const validationError = validateRefundSearchFilters(filters);
        if (validationError) {
            setError(validationError);
            return;
        }

        const params = buildRefundListQueryParams(filters, {
            tab: activeTab,
            page: 0,
            size: pageInfo.size,
        });
        setSearchParams(params, { replace: true });
    };

    const handleTabChange = (nextTab) => {
        const params = buildRefundListQueryParams(filters, {
            tab: nextTab,
            page: 0,
            size: pageInfo.size,
        });
        setSearchParams(params, { replace: true });
    };

    const handlePageChange = (updater) => {
        setPageInfo((prev) => {
            const next = typeof updater === 'function' ? updater(prev) : updater;
            const params = buildRefundListQueryParams(filters, {
                tab: activeTab,
                page: next.page,
                size: next.size,
            });
            setSearchParams(params, { replace: true });
            return next;
        });
    };

    const refreshList = () => {
        fetchRefunds(filters, pageInfo, activeTab);
    };

    return {
        activeTab,
        filters,
        data,
        loading,
        error,
        pageInfo,
        setPageInfo: handlePageChange,
        handleFilterChange,
        handleReset,
        handleSearch,
        handleTabChange,
        refreshList,
    };
}
