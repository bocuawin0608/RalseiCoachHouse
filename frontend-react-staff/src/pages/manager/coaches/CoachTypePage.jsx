import { useNavigate } from 'react-router-dom';
import { useCoachTypes, CoachTypeTable, CoachTypeFilter } from '../../../features/coaches';
import Pagination from '../../../components/common/Pagination';
import { Alert, Button, Card, Container } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';

export default function CoachTypePage() {
    const navigate = useNavigate();

    const {
        coachTypes, loading, pageInfo, setPageInfo,
        filters, handleFilterChange, handleReset, error
    } = useCoachTypes();

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="m-0 fw-bold text-dark">Quản lý các loại xe</h2>
                <Button
                    className="fw-medium shadow-sm custom-btn-general"
                    onClick={() => navigate('/management/coach-types/create')}
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
                <Alert variant="danger" className="shadow-sm border-0 mb-3">
                    <div className="d-flex align-items-center gap-2 fw-semibold">
                        <BsExclamationTriangleFill />
                        <span>{error.message}</span>
                    </div>
                    {error.fieldErrors && (
                        <ul className="mb-0 ps-4 mt-2" style={{ fontSize: '0.85rem' }}>
                            {[...new Set(Object.values(error.fieldErrors))].map((msg, i) => (
                                <li key={i}>{msg}</li>
                            ))}
                        </ul>
                    )}
                </Alert>
            )}

            <Card className="shadow-sm border-0">
                <Card.Body className="p-0">
                    <CoachTypeTable
                        data={coachTypes}
                        loading={loading}
                        onRowClick={(row) => navigate(`/management/coach-types/${row.coachTypeId}`)}
                    />

                    <div className="d-flex justify-content-center py-4 bg-white border-top">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </div>
                </Card.Body>
            </Card>
        </Container>
    );
}
