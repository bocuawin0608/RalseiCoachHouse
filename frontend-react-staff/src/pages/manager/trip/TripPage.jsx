import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Container } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import {
    useTrips,
    TripTable,
    TripFilter,
    TripUpdateInfoModal
} from '../../../features/trip';
import Pagination from '../../../components/common/Pagination';
import { tripApi } from '../../../features/trip/api/tripApi';
import './TripPage.css';

export default function TripPage() {
    const navigate = useNavigate();

    const {
        trips, loading, pageInfo, setPageInfo, refetch,
        filters, handleFilterChange, handleReset, error
    } = useTrips();

    /** Modal state: type discriminates which modal is open, data holds the selected row */
    const [modalState, setModalState] = useState({ type: null, data: null });

    /** Close any open modal and clear selected row */
    const closeModal = () => setModalState({ type: null, data: null });

    /** Soft-delete (cancel) trip and refresh the list */
    const handleDelete = async (row) => {
        if (!window.confirm(`Bạn có chắc muốn hủy chuyến #${row.tripId}?`)) return;
        try {
            await tripApi.deleteTrip(row.tripId);
            refetch();
        } catch (err) {
            alert(err.response?.data?.message || 'Hủy chuyến thất bại.');
        }
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>

            {/* Page header */}
            <div className="trip-page-header">
                <h2 className="trip-page-title">Quản lý chuyến xe</h2>
                <Button
                    className="fw-medium shadow-sm custom-btn-general"
                    onClick={() => navigate('/management/trips/create')}
                >
                    + Thêm chuyến xe mới
                </Button>
            </div>

            {/* Filter bar */}
            <TripFilter
                filters={filters}
                onFilterChange={handleFilterChange}
                onReset={handleReset}
            />

            {/* Error banner */}
            {error && (
                <Alert variant="danger" className="shadow-sm border-0 d-flex align-items-center gap-2">
                    <BsExclamationTriangleFill />
                    <span>{error}</span>
                </Alert>
            )}

            {/* Data table + pagination */}
            <Card className="trip-page-card">
                <Card.Body className="trip-page-card-body">
                    <TripTable
                        data={trips}
                        loading={loading}
                        onEditInfo={(row) => setModalState({ type: 'EDIT_INFO', data: row })}
                        onDelete={handleDelete}
                    />
                    <div className="trip-page-pagination">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </div>
                </Card.Body>
            </Card>

            {/* Edit modal */}
            <TripUpdateInfoModal
                isOpen={modalState.type === 'EDIT_INFO'}
                data={modalState.data}
                onClose={closeModal}
                onSuccess={refetch}
            />

        </Container>
    );
}
