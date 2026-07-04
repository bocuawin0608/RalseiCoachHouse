import { useState, useEffect, useCallback } from 'react';
import { useDebounce } from '../../../hooks/useDebounce';
import staffApi from '../api/staffApi';

const useStaff = () => {
    const [staffList, setStaffList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({ search: '', isActive: '', staffPosition: '', ticketAgencyId: '' });
    const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0 });

    const debouncedSearch = useDebounce(filters.search, 300);

    const fetch = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const params = {
                page: pageInfo.page,
                size: pageInfo.size,
                search: debouncedSearch || undefined,
                isActive: filters.isActive !== '' ? filters.isActive : undefined,
                staffPosition: filters.staffPosition || undefined,
                ticketAgencyId: filters.ticketAgencyId || undefined,
            };
            const data = await staffApi.filter(params);
            setStaffList(data.content || []);
            setPageInfo(prev => ({
                ...prev,
                totalElements: data.totalElements || 0,
                totalPages: data.totalPages || 0,
            }));
        } catch (err) {
            setError(err.response?.data?.message || 'Có lỗi xảy ra khi tải danh sách nhân viên.');
            setStaffList([]);
        } finally {
            setLoading(false);
        }
    }, [debouncedSearch, filters.isActive, filters.staffPosition, filters.ticketAgencyId, pageInfo.page, pageInfo.size]);

    useEffect(() => { fetch(); }, [fetch]);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    const handleReset = () => {
        setFilters({ search: '', isActive: '', staffPosition: '', ticketAgencyId: '' });
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return {
        staffList, loading, error, filters, pageInfo, setPageInfo,
        handleFilterChange, handleReset, refetch: fetch,
    };
};

export default useStaff;
