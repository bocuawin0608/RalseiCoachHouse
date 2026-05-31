import { useState, useEffect, useCallback } from 'react';
import './SeatLayoutPage.module.css';
import SeatLayoutFilter from '../../components/seat-layout/SeatLayoutFilter';
import SeatLayoutTable from '../../components/seat-layout/SeatLayoutTable';
import { seatLayoutService } from '../../services/seatLayoutService';
import Pagination from '../../components/common/Pagination';
import { useNavigate } from 'react-router-dom';
import Button from '../../components/common/Button';
import SeatLayoutCreateModal from '../../components/seat-layout/SeatLayoutCreateModal';

export default function SeatLayoutPage() {
    const navigate = useNavigate();

    const [layouts, setLayouts] = useState([]);
    const [filters, setFilters] = useState({ seatLayoutName: '' });
    const [pageInfo, setPageInfo] = useState({ 
        page: 0,
        size: 10, 
        totalElements: 0, 
        totalPages: 0 
    });

    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

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
            console.error("Lỗi khi tải dữ liệu Sơ đồ ghế: ", error);
        }
    }, []);
    
    useEffect(() => {
        const load = async () => {
            await fetchLayouts(filters, pageInfo.page, pageInfo.size);
        };
        load();
    }, [pageInfo.page, pageInfo.size, filters, fetchLayouts]);

    const handleFilterChange = (e) => {
        setFilters({ ...filters, [e.target.name]: e.target.value });
    };

    const handleReset = () => {
        setFilters({ seatLayoutName: '' });
        setPageInfo(prev => ({ ...prev, page: 0 }));
    };

    return (
        <div style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-around', alignItems: 'center', marginBottom: '20px' }}>
                <h2>Quản lý Sơ đồ ghế</h2>
                <Button 
                    variant="primary" 
                    size="medium" 
                    onClick={() => setIsCreateModalOpen(true)}
                >
                    + Thêm sơ đồ mới
                </Button>
            </div>

            <SeatLayoutFilter 
                filters={filters} 
                onFilterChange={handleFilterChange}
                onReset={handleReset}
            />

            <SeatLayoutTable layouts={layouts} navigate={navigate}/>            
            <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />

            <SeatLayoutCreateModal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} onSuccess={handleReset} />
        </div>
    );
}