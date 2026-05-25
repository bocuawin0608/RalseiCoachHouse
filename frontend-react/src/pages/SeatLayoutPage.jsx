import { useState, useEffect, useCallback } from 'react';
import SeatLayoutFilter from '../components/seat-layout/SeatLayoutFilter';
import SeatLayoutTable from '../components/seat-layout/SeatLayoutTable';
import { seatLayoutService } from '../services/seatLayoutService';

export default function SeatLayoutPage() {
    const [layouts, setLayouts] = useState([]);
    const [filters, setFilters] = useState({ seatLayoutName: '' });
    const [pageInfo, setPageInfo] = useState({ 
        page: 0,
        size: 10, 
        totalElements: 0, 
        totalPages: 0 
    });

    const fetchLayouts = useCallback(async (currentFilters, currentPage, currentSize) => {
        try {
            const response = await seatLayoutService.filterSeatLayouts({ 
                ...currentFilters, 
                page: currentPage, 
                size: currentSize 
            });
            
            setLayouts(response.content);
            
            setPageInfo(prev => ({
                ...prev,
                totalElements: response.page.totalElements,
                totalPages: response.page.totalPages
            }));
        } catch (error) {
            console.error("Lỗi khi kéo dữ liệu Sơ đồ ghế:", error);
        }
    }, []);

    useEffect(() => { 
        fetchLayouts(filters, pageInfo.page, pageInfo.size); 
    }, [pageInfo.page, fetchLayouts]); 

    const handleFilterChange = (e) => {
        setFilters({ ...filters, [e.target.name]: e.target.value });
    };

    const handleSearch = () => {
        setPageInfo(prev => ({ ...prev, page: 0 })); 
        fetchLayouts(filters, 0, pageInfo.size); 
    };

    return (
        <div style={{ padding: '20px' }}>
            <h2>Quản lý Sơ đồ xe</h2>
            
            <SeatLayoutFilter 
                filters={filters} 
                onFilterChange={handleFilterChange} 
                onSearch={handleSearch} 
            />

            <SeatLayoutTable layouts={layouts} />
            
            {/* ĐỂ TẠM COMPONENT PHÂN TRANG DẠNG CHƯA CHO VÀO FOLDER COMPONENTS/COMMON Ở ĐÂY!! */}
            <div style={{ marginTop: '20px', display: 'flex', gap: '15px', alignItems: 'center' }}>
                <button 
                    disabled={pageInfo.page === 0} 
                    onClick={() => setPageInfo(prev => ({ ...prev, page: prev.page - 1 }))}
                    style={{ cursor: pageInfo.page === 0 ? 'not-allowed' : 'pointer' }}
                >
                    Trang trước
                </button>
                
                <span>
                    Trang {pageInfo.page + 1} / {pageInfo.totalPages === 0 ? 1 : pageInfo.totalPages} 
                    {" "}(Tổng: {pageInfo.totalElements} bản ghi)
                </span>
                
                <button 
                    disabled={pageInfo.page >= pageInfo.totalPages - 1 || pageInfo.totalPages === 0} 
                    onClick={() => setPageInfo(prev => ({ ...prev, page: prev.page + 1 }))}
                    style={{ cursor: (pageInfo.page >= pageInfo.totalPages - 1) ? 'not-allowed' : 'pointer' }}
                >
                    Trang sau
                </button>
            </div>
        </div>
    );
}