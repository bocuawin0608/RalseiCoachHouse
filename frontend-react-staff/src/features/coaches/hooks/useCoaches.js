import { useState, useEffect, useCallback } from 'react';
import { coachApi } from '../api/coachApi';
import { useDebounce } from '../../../hooks/useDebounce';

const INITIAL_FILTER = { licensePlate: '', statuses: [], coachTypeId: '', routeName: '' };

export const useCoaches = () => {
    const [coaches, setCoaches] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [pageInfo, setPageInfo] = useState({ 
        page: 0,
        size: 10, 
        totalElements: 0, 
        totalPages: 0 
    });
    const [filters, setFilters] = useState(INITIAL_FILTER);
    const debouncedFilters = useDebounce(filters, 250);

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
        const load = () => {
            fetchCoaches();
        }
        load();
    }, [fetchCoaches]);

    const handleCheckboxChange = (e) => {
        const {value, checked } = e.target;

        if(checked) setFilters({...filters, statuses: [...filters.statuses, value]});
        else setFilters({...filters, statuses: filters.statuses.filter(status => status != value)})
    }

    return { 
        coaches, loading, error, pageInfo, setPageInfo, filters, 
        handleFilterChange: (e) => setFilters(prev => ({...prev, [e.target.name]: e.target.value})),
        handleCheckboxChange,
        handleReset: () => setFilters(INITIAL_FILTER),
        refetch: fetchCoaches 
    };
};