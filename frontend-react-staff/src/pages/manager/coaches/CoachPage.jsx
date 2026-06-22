import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
    useCoaches, 
    CoachTable, 
    CoachFilter, 
    CoachUpdateInfoModal, 
    CoachViewDetailModal, 
    CoachCreateModal
} from '../../../features/coaches';
import Pagination from '../../../components/common/Pagination';
import { Alert, Button, Card, Container } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import { useCoachTypeDropdown } from '../../../hooks/useCoachTypeDropdown';

const statusLabels = {
    ACTIVE: { text: 'Đang hoạt động', bg: 'success' },
    MAINTENANCE: { text: 'Đang bảo trì', bg: 'warning' },
    RETIRED: { text: 'Ngừng hoạt động', bg: 'danger' }
};

export default function CoachPage() {
    const navigate = useNavigate();
    
    const { 
        coaches, loading, pageInfo, setPageInfo, refetch,
        filters, handleFilterChange, handleReset, handleCheckboxChange, error
    } = useCoaches();

    const {coachTypes, coachTypesLoading, errorFromDropdown} = useCoachTypeDropdown(true);
    
    const [modalState, setModalState] = useState({ type: null, data: null });
    const closeModal = () => setModalState({ type: null, data: null });

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="m-0 fw-bold text-dark">Quản lý danh sách xe</h2>
                <Button 
                    className="fw-medium shadow-sm custom-btn-general"
                    onClick={() => setModalState({ type: 'CREATE', data: null })}
                >
                    + Thêm xe mới
                </Button>
            </div>

            <CoachFilter 
                filters={filters} 
                onFilterChange={handleFilterChange}
                onCheckboxChange={handleCheckboxChange}
                onReset={handleReset}
                coachTypesDropdown={coachTypes}
                coachTypesDropdownLoading={coachTypesLoading}
                statusLabels={statusLabels}
            />

            {(error || errorFromDropdown) && (
                <Alert variant="danger" className="shadow-sm border-0 d-flex align-items-center gap-2">
                    <BsExclamationTriangleFill />
                    <span>{error || errorFromDropdown}</span>
                </Alert>
            )}

            <Card className="shadow-sm border-0">
                <Card.Body className="p-0"> 
                    <CoachTable 
                        data={coaches} 
                        loading={loading}
                        statusLabels={statusLabels}
                        onViewDetail={(row) => setModalState({ type: 'VIEW_DETAIL', data: row})}
                        onEditInfo={(row) => setModalState({ type: 'EDIT_INFO', data: row })}
                        onEditSeatMap={(row) => navigate(`/management/coaches/${row.coachId}/seat-map`)} 
                    />
                    
                    <div className="d-flex justify-content-center py-4 bg-white border-top">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </div>
                </Card.Body>
            </Card>

            <CoachCreateModal 
                isOpen={modalState.type === 'CREATE'}
                onClose={closeModal}
                onSuccess={refetch}
            />

            <CoachUpdateInfoModal 
                isOpen={modalState.type === 'EDIT_INFO'} 
                data={modalState.data} 
                onClose={closeModal} 
                onSuccess={refetch} 
                statusLabels={statusLabels}
            />

            <CoachViewDetailModal 
                isOpen={modalState.type === 'VIEW_DETAIL'} 
                data={modalState.data} 
                onClose={closeModal} 
                statusLabels={statusLabels}
            />

        </Container>
    );
}