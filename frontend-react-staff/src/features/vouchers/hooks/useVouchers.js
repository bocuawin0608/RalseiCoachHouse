import { useState, useEffect, useCallback } from 'react';
import { voucherApi } from '../api/voucherApi';
import { useDebounce } from '../../../hooks/useDebounce';

export const useVouchers = () => {
    const [vouchers, setVouchers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [pageInfo, setPageInfo] = useState({
        pageNumber: 0,
        pageSize: 10,
        totalElements: 0,
        totalPages: 0,
        isLast: true,
    });
    const [filters, setFilters] = useState({
        search: '',
        discountType: '',
        fromDate: '',
        toDate: '',
        page: 0,
        size: 10,
    });

    const debouncedSearch = useDebounce(filters.search, 500);

    const fetchVouchers = useCallback(async (filterParams) => {
        setLoading(true);
        setError(null);
        try {
            const params = {};
            if (filterParams.search) params.search = filterParams.search;
            if (filterParams.discountType) params.discountType = filterParams.discountType;
            if (filterParams.fromDate) params.fromDate = filterParams.fromDate;
            if (filterParams.toDate) params.toDate = filterParams.toDate;
            params.page = filterParams.page ?? 0;
            params.size = filterParams.size ?? 10;

            const response = await voucherApi.getAll(params);
            setVouchers(response.content || []);
            setPageInfo({
                pageNumber: response.pageNumber ?? 0,
                pageSize: response.pageSize ?? 10,
                totalElements: response.totalElements ?? 0,
                totalPages: response.totalPages ?? 0,
                isLast: response.last ?? true,
            });
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch vouchers');
            setVouchers([]);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        const params = { ...filters, search: debouncedSearch };
        fetchVouchers(params);
    }, [debouncedSearch, filters.discountType, filters.fromDate, filters.toDate, filters.page, filters.size, fetchVouchers]);

    const handleFilterChange = (name, value) => {
        setFilters((prev) => ({ ...prev, [name]: value, page: 0 }));
    };

    const handleReset = () => {
        setFilters({
            search: '',
            discountType: '',
            fromDate: '',
            toDate: '',
            page: 0,
            size: 10,
        });
    };

    const handlePageChange = (newPage) => {
        setFilters((prev) => ({ ...prev, page: newPage }));
    };

    return {
        vouchers,
        loading,
        error,
        pageInfo,
        filters,
        handleFilterChange,
        handleReset,
        handlePageChange,
        fetchVouchers,
    };
};
