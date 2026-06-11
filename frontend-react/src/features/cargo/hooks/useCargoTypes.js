import { useCallback, useEffect, useState } from "react";
import { cargoTypeApi } from "../api/cargoTypeApi";
import { useDebounce } from "../../../hooks/useDebounce";

export function useCargoTypes() {
    const [cargoTypes, setCargoTypes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({ search: '', isActive: '' });
    const [pageInfo, setPageInfo] = useState({ 
        page: 0,
        size: 10, 
        totalElements: 0, 
        totalPages: 0 
    });
    const debouncedFilters = useDebounce(filters, 250);
    
    const fetchCargoTypes = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await cargoTypeApi.filterCargoTypes({ 
                search: debouncedFilters.search, 
                isActive: debouncedFilters.isActive,
                page: pageInfo.page, 
                size: pageInfo.size 
            });
            
            setCargoTypes(response.content);
            setPageInfo(prev => ({
                ...prev,
                totalElements: response.page?.totalElements ?? response.totalElements ?? 0, 
                totalPages: response.page?.totalPages ?? response.totalPages ?? 0
            }));
        } catch (error) {
            setError(error.response?.data?.message || "Có lỗi xảy ra khi lấy dữ liệu.");
            setCargoTypes([]);
        } finally {
            setLoading(false);
        }
    }, [debouncedFilters, pageInfo.page, pageInfo.size]);

    useEffect(() => {
        fetchCargoTypes();
    }, [fetchCargoTypes]);

    const handleFilterChange = (e) => {
        setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
        setPageInfo(prev => ({ ...prev, page: 0 })); 
    };

    const handleReset = () => {
        setFilters({ search: '', isActive: '' });
        setError(null);
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return {
        cargoTypes, loading, error,
        filters, pageInfo, setPageInfo, 
        handleFilterChange, handleReset,
        refetch: fetchCargoTypes 
    };
}
