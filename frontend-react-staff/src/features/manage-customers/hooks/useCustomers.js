import { useState, useEffect, useCallback } from 'react';
import { useDebounce } from '../../../hooks/useDebounce';
import customerApi from '../api/customerApi';

const useCustomers = () => {
    const [customers, setCustomers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({ search: '', isActive: '' });
    const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0 });

    const debouncedSearch = useDebounce(filters.search, 300);

    const fetchCustomers = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const params = {
                page: pageInfo.page,
                size: pageInfo.size,
                search: debouncedSearch || undefined,
                isActive: filters.isActive !== '' ? filters.isActive : undefined,
            };
            const data = await customerApi.filterCustomers(params);
            setCustomers(data.content || []);
            const pg = data.page || data;
            setPageInfo(prev => ({
                ...prev,
                totalElements: pg.totalElements || 0,
                totalPages: pg.totalPages || 0,
            }));
        } catch (err) {
            setError(err.response?.data?.message || 'Có lỗi xảy ra khi tải danh sách khách hàng.');
            setCustomers([]);
        } finally {
            setLoading(false);
        }
    }, [debouncedSearch, filters.isActive, pageInfo.page, pageInfo.size]);

    useEffect(() => {
        fetchCustomers();
        const onFocus = () => fetchCustomers();
        window.addEventListener('focus', onFocus);
        return () => window.removeEventListener('focus', onFocus);
    }, [fetchCustomers]);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    const handleReset = () => {
        setFilters({ search: '', isActive: '' });
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return {
        customers, loading, error, filters, pageInfo, setPageInfo,
        handleFilterChange, handleReset, refetch: fetchCustomers,
    };
};

export default useCustomers;
