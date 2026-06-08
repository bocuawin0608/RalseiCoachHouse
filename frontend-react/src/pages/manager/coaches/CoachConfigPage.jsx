// src/pages/manager/coaches/CoachConfigPage.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
    useCoachTypes, 
    CoachTypeTable, 
    CoachTypeFilter, 
    CoachTypeUpdateInfoModal, 
    CoachTypeViewDetailModal,
    CoachTypeUpdatePriceModal
} from '../../../features/coaches';
import Pagination from '../../../components/common/Pagination';
import { Alert, Button, Card, Container } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';

export default function CoachConfigPage() {
    const navigate = useNavigate();
    
    const { 
        coachTypes, loading, pageInfo, setPageInfo, refetch,
        filters, handleFilterChange, handleReset, error
    } = useCoachTypes();
    
    const [modalState, setModalState] = useState({ type: null, data: null });
    const closeModal = () => setModalState({ type: null, data: null });

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="m-0 fw-bold text-dark">Quản lý các loại xe</h2>
                <Button 
                    variant="primary" 
                    className="fw-medium shadow-sm"
                    onClick={() => navigate('/manager/coach-types/create')}
                >
                    + Thêm loại xe mới
                </Button>
            </div>

            <CoachTypeFilter 
                filters={filters} 
                onFilterChange={handleFilterChange}
                onReset={handleReset}
            />

            {error && (
                <Alert variant="danger" className="shadow-sm border-0 d-flex align-items-center gap-2">
                    <BsExclamationTriangleFill />
                    <span>{error}</span>
                </Alert>
            )}

            <Card className="shadow-sm border-0">
                <Card.Body className="p-0"> 
                    <CoachTypeTable 
                        data={coachTypes} 
                        loading={loading}
                        onViewDetail={(row) => setModalState({ type: 'VIEW_DETAIL', data: row})}
                        onEditInfo={(row) => setModalState({ type: 'EDIT_INFO', data: row })}
                        onEditPrice = {(row) => setModalState({ type: 'EDIT_PRICE', data: row })}
                        onEditSeatMap={(row) => navigate(`/manager/coach-types/${row.coachTypeId}/seat-map`)} 
                    />
                    
                    <div className="d-flex justify-content-center py-4 bg-white border-top">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </div>
                </Card.Body>
            </Card>

            <CoachTypeUpdateInfoModal 
                isOpen={modalState.type === 'EDIT_INFO'} 
                data={modalState.data} 
                onClose={closeModal} 
                onSuccess={refetch} 
            />

            <CoachTypeViewDetailModal 
                isOpen={modalState.type === 'VIEW_DETAIL'} 
                data={modalState.data} 
                onClose={closeModal} 
            />

            <CoachTypeUpdatePriceModal
                isOpen={modalState.type === 'EDIT_PRICE'} 
                data={modalState.data} 
                onClose={closeModal} 
                onSuccess={refetch} 
            />

        </Container>
    );
}