import { useState, useEffect } from 'react';
import { coachStopApi } from '../api/coachStopApi';

export const useCoachStops = () => {
    const [coachStops, setCoachStops] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const [filters, setFilters] = useState({
        search: '',
        isActive: ''
    });

    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: 10,
        totalPages: 0,
        totalElements: 0,
        isLast: true
    });

    const [debouncedSearch, setDebouncedSearch] = useState('');

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedSearch(filters.search);
            setPageInfo(prev => ({ ...prev, page: 0 }));
        }, 500);

        return () => clearTimeout(handler);
    }, [filters.search]);

    const fetchCoachStops = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await coachStopApi.getAllCoachStops(
                debouncedSearch || undefined,
                filters.isActive !== '' ? filters.isActive : undefined,
                pageInfo.page,
                pageInfo.size
            );

            setCoachStops(data.content || []);
            setPageInfo(prev => ({
                ...prev,
                totalPages: data.totalPages ?? 0,
                totalElements: data.totalElements ?? 0,
                isLast: data.last ?? true
            }));
        } catch (err) {
            setError(err.response?.data?.message || 'Có lỗi xảy ra khi tải danh sách điểm dừng');
            console.error('Failed to fetch coach stops', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCoachStops();
    }, [debouncedSearch, filters.isActive, pageInfo.page]);

    const handleFilterChange = (name, value) => {
        setFilters(prev => ({ ...prev, [name]: value }));
        if (name !== 'search') {
            setPageInfo(prev => ({ ...prev, page: 0 }));
        }
    };

    const handleReset = () => {
        setFilters({ search: '', isActive: '' });
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return {
        coachStops,
        loading,
        error,
        filters,
        pageInfo,
        setPageInfo,
        handleFilterChange,
        handleReset,
        refetch: fetchCoachStops
    };
};
