import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useCoaches, CoachTable, CoachFilter, CoachCreateModal } from '../../../features/coaches';
import Pagination from '../../../components/common/Pagination';
import { Alert, Button, Card, Container } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import { useCoachTypeDropdown } from '../../../hooks/useCoachTypeDropdown';

const statusLabels = {
    ACTIVE: { text: 'Đang hoạt động', bg: 'success' },
    HAVE_INCIDENT: { text: 'Gặp sự cố', bg: 'danger' },
    MAINTENANCE: { text: 'Đang bảo trì', bg: 'warning' },
    RETIRED: { text: 'Ngừng hoạt động', bg: 'secondary' }
};

export default function CoachPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const initialCoachTypeId = searchParams.get('coachTypeId') || '';

    const {
        coaches, loading, pageInfo, setPageInfo, refetch,
        filters, handleFilterChange, handleReset, handleCheckboxChange, error
    } = useCoaches(initialCoachTypeId);

    const { coachTypes, coachTypesLoading, errorFromDropdown } = useCoachTypeDropdown(true);

    const [createOpen, setCreateOpen] = useState(false);

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="m-0 fw-bold text-dark">Quản lý danh sách xe</h2>
                <Button
                    className="fw-medium shadow-sm custom-btn-general"
                    onClick={() => setCreateOpen(true)}
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
                        onRowClick={(row) => navigate(`/management/coaches/${row.coachId}`)}
                    />

                    <div className="d-flex justify-content-center py-4 bg-white border-top">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </div>
                </Card.Body>
            </Card>

            <CoachCreateModal
                isOpen={createOpen}
                onClose={() => setCreateOpen(false)}
                onSuccess={refetch}
            />
        </Container>
    );
}
