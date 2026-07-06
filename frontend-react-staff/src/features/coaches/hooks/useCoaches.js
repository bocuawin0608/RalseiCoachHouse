import { useState, useEffect, useCallback } from 'react';
import { coachApi } from '../api/coachApi';
import { useDebounce } from '../../../hooks/useDebounce';

const INITIAL_FILTER = { licensePlate: '', statuses: ['ACTIVE'], coachTypeId: '', routeName: '' };

export const useCoaches = (initialCoachTypeId = '') => {
    const [coaches, setCoaches] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0
    });
    const [filters, setFilters] = useState({
        ...INITIAL_FILTER,
        coachTypeId: initialCoachTypeId,
    });
    const debouncedFilters = useDebounce(filters, 250);

    useEffect(() => {
        if (initialCoachTypeId) {
            setFilters((prev) => ({ ...prev, coachTypeId: initialCoachTypeId }));
        }
    }, [initialCoachTypeId]);

    const fetchCoaches = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await coachApi.filterCoaches({
                ...debouncedFilters,
                page: pageInfo.page,
                size: pageInfo.size
            });

            setCoaches(response.content);
            setPageInfo(prev => ({
                ...prev,
                totalElements: response.page.totalElements,
                totalPages: response.page.totalPages
            }));
        } catch (err) {
            setError(err.response?.data?.message || 'Lỗi khi tải danh sách xe');
            setCoaches([]);
        } finally {
            setLoading(false);
        }
    }, [debouncedFilters, pageInfo.page, pageInfo.size]);

    useEffect(() => {
        fetchCoaches();
    }, [fetchCoaches]);

    const handleFilterChange = useCallback((e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
        setPageInfo(prev => ({ ...prev, page: 0 })); 
    }, []);

    const handleCheckboxChange = useCallback((e) => {
        const { value, checked } = e.target;

        setFilters(prev => {
            const currentStatuses = prev.statuses || [];
            const nextStatuses = checked 
                ? [...currentStatuses, value] 
                : currentStatuses.filter(status => status !== value);
                
            return { ...prev, statuses: nextStatuses };
        });
        
        setPageInfo(prev => ({ ...prev, page: 0 }));
    }, []);

    const handleReset = useCallback(() => {
        setFilters({
            ...INITIAL_FILTER,
            coachTypeId: initialCoachTypeId
        });
        setPageInfo(prev => ({ ...prev, page: 0 }));
    }, [initialCoachTypeId]);

    return {
        coaches, loading, error, pageInfo, setPageInfo, filters,
        handleFilterChange,
        handleCheckboxChange,
        handleReset,
        refetch: fetchCoaches
    };
};
