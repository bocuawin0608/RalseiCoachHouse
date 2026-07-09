import { useState, useEffect, useCallback } from 'react';
import { useDebounce } from '../../../hooks/useDebounce';
import roleApi from '../api/roleApi';

const useRoles = () => {
    const [roles, setRoles] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({ search: '', isActive: '' });
    const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0 });

    const debouncedSearch = useDebounce(filters.search, 300);

    const fetchRoles = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const params = {
                page: pageInfo.page,
                size: pageInfo.size,
                search: debouncedSearch || undefined,
                isActive: filters.isActive !== '' ? filters.isActive : undefined,
            };
            const data = await roleApi.filterRoles(params);
            setRoles(data.content || []);
            const p = data.page || data;
            setPageInfo(prev => ({
                ...prev,
                totalElements: p.totalElements || 0,
                totalPages: p.totalPages || 0,
            }));
        } catch (err) {
            setError(err.response?.data?.message || 'Có lỗi xảy ra khi tải danh sách vai trò.');
            setRoles([]);
        } finally {
            setLoading(false);
        }
    }, [debouncedSearch, filters.isActive, pageInfo.page, pageInfo.size]);

    useEffect(() => {
        fetchRoles();
    }, [fetchRoles]);

    useEffect(() => {
        const onFocus = () => fetchRoles();
        window.addEventListener('focus', onFocus);
        return () => window.removeEventListener('focus', onFocus);
    }, [fetchRoles]);

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
        roles, loading, error, filters, pageInfo, setPageInfo,
        handleFilterChange, handleReset, refetch: fetchRoles,
    };
};

export default useRoles;
