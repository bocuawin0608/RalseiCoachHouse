import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
    useCoaches, 
    CoachTable, 
    CoachFilter, 
    CoachUpdateInfoModal, 
    CoachViewDetailModal 
} from '../../../features/coaches';
import Pagination from '../../../components/common/Pagination';
import { Alert, Button, Card, Container } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';

export default function CoachPage() {
    const navigate = useNavigate();
    
    const { 
        coaches, loading, pageInfo, setPageInfo, refetch,
        filters, handleFilterChange, handleReset, handleCheckboxChange, error
    } = useCoaches();
    
    const [modalState, setModalState] = useState({ type: null, data: null });
    const closeModal = () => setModalState({ type: null, data: null });

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="m-0 fw-bold text-dark">Quản lý danh sách xe</h2>
                <Button 
                    variant="primary" 
                    className="fw-medium shadow-sm"
                    onClick={() => navigate('/manager/coaches/create')}
                >
                    + Thêm xe mới
                </Button>
            </div>

            <CoachFilter 
                filters={filters} 
                onFilterChange={handleFilterChange}
                onCheckboxChange={handleCheckboxChange}
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
                    <CoachTable 
                        data={coaches} 
                        loading={loading}
                        onViewDetail={(row) => setModalState({ type: 'VIEW_DETAIL', data: row})}
                        onEditInfo={(row) => setModalState({ type: 'EDIT_INFO', data: row })}
                        onEditSeatMap={(row) => navigate(`/manager/coaches/${row.coachId}/seat-map`)} 
                    />
                    
                    <div className="d-flex justify-content-center py-4 bg-white border-top">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </div>
                </Card.Body>
            </Card>

            <CoachUpdateInfoModal 
                isOpen={modalState.type === 'EDIT_INFO'} 
                data={modalState.data} 
                onClose={closeModal} 
                onSuccess={refetch} 
            />

            <CoachViewDetailModal 
                isOpen={modalState.type === 'VIEW_DETAIL'} 
                data={modalState.data} 
                onClose={closeModal} 
            />

        </Container>
    );
}