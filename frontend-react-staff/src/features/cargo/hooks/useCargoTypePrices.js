import { useState, useCallback, useEffect } from 'react';
import { cargoTypePriceApi } from '../api/cargoTypePriceApi';

export const useCargoTypePrices = () => {
    const [prices, setPrices] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: 10,
        totalPages: 0,
        totalElements: 0
    });
    
    const [filters, setFilters] = useState({
        search: ''
    });

    const fetchPrices = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await cargoTypePriceApi.getAllCargoTypePrices(
                filters.search,
                pageInfo.page,
                pageInfo.size
            );
            setPrices(data.content || []);
            setPageInfo(prev => ({
                ...prev,
                totalPages: data.page?.totalPages ?? data.totalPages ?? 0,
                totalElements: data.page?.totalElements ?? data.totalElements ?? 0
            }));
        } catch (err) {
            console.error('Failed to fetch cargo type prices', err);
            setError('Không thể tải danh sách giá cước. Vui lòng thử lại sau.');
            setPrices([]);
        } finally {
            setLoading(false);
        }
    }, [filters, pageInfo.page, pageInfo.size]);

    useEffect(() => {
        fetchPrices();
    }, [fetchPrices]);

    const handleFilterChange = (key, value) => {
        setFilters(prev => ({ ...prev, [key]: value }));
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    const handleReset = () => {
        setFilters({ search: '' });
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return {
        prices,
        loading,
        pageInfo,
        setPageInfo,
        filters,
        handleFilterChange,
        handleReset,
        error,
        refetch: fetchPrices
    };
};
