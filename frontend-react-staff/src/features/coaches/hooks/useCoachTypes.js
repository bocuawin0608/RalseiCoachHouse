import { useCallback, useEffect, useState } from "react";
import { coachTypeApi } from "../api/coachTypeApi";
import { useDebounce } from "../../../hooks/useDebounce";

export function useCoachTypes() {
    
    const [coachTypes, setCoachTypes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({ isActive: true });
    const [pageInfo, setPageInfo] = useState({ 
        page: 0,
        size: 10, 
        totalElements: 0, 
        totalPages: 0 
    });
    const debouncedFilters = useDebounce(filters, 250);
    
    const fetchCoachTypes = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await coachTypeApi.filterCoachTypes({
                ...debouncedFilters,
                coachTypeName: debouncedFilters.coachTypeName?.trim() || undefined,
                page: pageInfo.page,
                size: pageInfo.size,
            });
            
            setCoachTypes(response.content);
            setPageInfo(prev => ({
                ...prev,
                totalElements: response.page.totalElements, 
                totalPages: response.page.totalPages
            }));
        } catch (err) {
            const data = err.response?.data;
            setError({
                message: data?.message || "Có lỗi xảy ra khi lấy dữ liệu.",
                fieldErrors: data?.fieldErrors || null,
            });
            setCoachTypes([]);
        } finally {
            setLoading(false);
        }
    }, [debouncedFilters, pageInfo.page, pageInfo.size]);

    
    useEffect(() => {
        const load = () => {
            fetchCoachTypes();
        }
        load();
    }, [fetchCoachTypes]);

    
    const handleFilterChange = (e) => {
        setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
        setPageInfo(prev => ({ ...prev, page: 0 })); 
    };

    const handleReset = () => {
        setFilters({ isActive: true });
        setError(null);
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return {
        coachTypes, loading, error,
        filters, pageInfo, setPageInfo, 
        handleFilterChange, handleReset,
        refetch: fetchCoachTypes 
    };
}