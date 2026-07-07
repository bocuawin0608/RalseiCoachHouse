import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    useCoachStops,
    CoachStopTable,
    CoachStopFilter,
    CoachStopUpdateInfoModal
} from '../../../features/coachStops';
import Pagination from '../../../components/common/Pagination';
import { Alert, Button, Card, Container } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';

export default function CoachStopPage() {
    const navigate = useNavigate();

    const {
        coachStops, loading, pageInfo, setPageInfo, refetch,
        filters, handleFilterChange, handleReset, error
    } = useCoachStops();

    const [modalState, setModalState] = useState({ type: null, data: null });
    const closeModal = () => setModalState({ type: null, data: null });

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>

            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="m-0 fw-bold text-dark">Quản lý văn phòng</h2>
                <Button
                    className="fw-medium shadow-sm custom-btn-general"
                    onClick={() => navigate('/management/coach-stops/create')}
                >
                    + Thêm văn phòng mới
                </Button>
            </div>

            <CoachStopFilter
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
                    <CoachStopTable
                        data={coachStops}
                        loading={loading}
                        onEditInfo={(row) => setModalState({ type: 'EDIT_INFO', data: row })}
                    />

                    <div className="d-flex justify-content-center py-4 bg-white border-top">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </div>
                </Card.Body>
            </Card>

            <CoachStopUpdateInfoModal
                isOpen={modalState.type === 'EDIT_INFO'}
                data={modalState.data}
                onClose={closeModal}
                onSuccess={refetch}
            />



        </Container>
    );
}
