import { useCallback, useEffect, useState } from "react";
import { routeApi } from "../api/routeApi";
import { useDebounce } from "../../../hooks/useDebounce";

export function useRoutes() {
    
    const [routes, setRoutes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({ search: '' });
    const [pageInfo, setPageInfo] = useState({ 
        page: 0,
        size: 10, 
        totalElements: 0, 
        totalPages: 0 
    });
    const debouncedFilters = useDebounce(filters, 250);
    
    const fetchRoutes = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await routeApi.filterRoutes({ 
                ...debouncedFilters, 
                page: pageInfo.page, 
                size: pageInfo.size 
            });
            
            // Backend returning Page of RouteResponse
            // RouteController returns PagedResponse: content, page (totalElements, totalPages)
            // Wait, does it return PagedResponse? 
            // In RouteManager.jsx, it uses `data.content` and `data.totalPages` directly. 
            // Oh, let me check RouteManager.jsx again
            // In RouteManager.jsx: setRoutes(data.content || []); setTotalPages(data.totalPages || 0);
            
            setRoutes(response.content || []);
            setPageInfo(prev => ({
                ...prev,
                totalElements: response.totalElements ?? response.page?.totalElements ?? 0,
                totalPages: response.totalPages ?? response.page?.totalPages ?? 0
            }));
            
            // Note: If backend uses a 'page' object inside the response like CoachTypes do, 
            // we should adjust it based on the exact API response. Assuming `data.totalPages` works from RouteManager.
        } catch (error) {
            setError(error.response?.data?.message || "Có lỗi xảy ra khi lấy dữ liệu.");
            setRoutes([]);
        } finally {
            setLoading(false);
        }
    }, [debouncedFilters, pageInfo.page, pageInfo.size]);

    useEffect(() => {
        const load = () => {
            fetchRoutes();
        }
        load();
    }, [fetchRoutes]);

    const handleFilterChange = (e) => {
        setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
        setPageInfo(prev => ({ ...prev, page: 0 })); 
    };

    const handleReset = () => {
        setFilters({ search: '' });
        setError(null);
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return {
        routes, loading, error,
        filters, pageInfo, setPageInfo, 
        handleFilterChange, handleReset,
        refetch: fetchRoutes 
    };
}
