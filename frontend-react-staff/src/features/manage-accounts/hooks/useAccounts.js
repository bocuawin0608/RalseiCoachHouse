import { useState, useEffect, useCallback } from 'react';
import { useDebounce } from '../../../hooks/useDebounce';
import accountApi from '../api/accountApi';

const useAccounts = () => {
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({
        search: '',
        role: '',
        isActive: '',
        authProvider: '',
    });
    const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0 });

    const debouncedSearch = useDebounce(filters.search, 300);

    const fetchAccounts = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const params = {
                page: pageInfo.page,
                size: pageInfo.size,
                search: debouncedSearch || undefined,
                role: filters.role || undefined,
                isActive: filters.isActive !== '' ? filters.isActive : undefined,
                authProvider: filters.authProvider || undefined,
            };
            const data = await accountApi.filterAccounts(params);
            setAccounts(data.content || []);
            const p = data.page || data;
            setPageInfo(prev => ({
                ...prev,
                totalElements: p.totalElements || 0,
                totalPages: p.totalPages || 0,
            }));
        } catch (err) {
            setError(err.response?.data?.message || 'Có lỗi xảy ra khi tải danh sách tài khoản.');
            setAccounts([]);
        } finally {
            setLoading(false);
        }
    }, [debouncedSearch, filters.role, filters.isActive, filters.authProvider, pageInfo.page, pageInfo.size]);

    useEffect(() => {
        fetchAccounts();
        const onFocus = () => fetchAccounts();
        window.addEventListener('focus', onFocus);
        return () => window.removeEventListener('focus', onFocus);
    }, [fetchAccounts]);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    const handleReset = () => {
        setFilters({ search: '', role: '', isActive: '', authProvider: '' });
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return {
        accounts, loading, error, filters, pageInfo, setPageInfo,
        handleFilterChange, handleReset, refetch: fetchAccounts,
    };
};

export default useAccounts;