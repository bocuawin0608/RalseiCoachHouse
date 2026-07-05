import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Container, Modal } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import {
    useTrips,
    TripTable,
    TripFilter,
    TripUpdateInfoModal,
    TripCrewModal
} from '../../../features/trip';
import Pagination from '../../../components/common/Pagination';
import { tripApi } from '../../../features/trip/api/tripApi';
import './TripPage.css';
import { useRouteDropdown } from '../../../hooks/useRouteDropdown';

export default function TripPage() {
    const navigate = useNavigate();
    const { routes } = useRouteDropdown(true);

    const {
        trips, loading, pageInfo, setPageInfo, refetch,
        filters, handleFilterChange, handleReset, error
    } = useTrips();

    /** Modal state: type discriminates which modal is open, data holds the selected row */
    const [modalState, setModalState] = useState({ type: null, data: null });
    const [deleteError, setDeleteError] = useState('');
    const [isDeleting, setIsDeleting] = useState(false);

    /** Close any open modal and clear selected row */
    const closeModal = () => setModalState({ type: null, data: null });

    /** Soft-delete (cancel) trip and refresh the list */
    const handleDelete = async () => {
        const row = modalState.data;
        setIsDeleting(true);
        setDeleteError('');
        try {
            await tripApi.deleteTrip(row.tripId);
            closeModal();
            await refetch();
        } catch (err) {
            setDeleteError(err.response?.data?.message || err.response?.data || 'Hủy chuyến thất bại.');
        } finally { setIsDeleting(false); }
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
                routes={routes}
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
                        onViewCrew={(row) => setModalState({ type: 'CREW', data: row })}
                        onEditInfo={(row) => setModalState({ type: 'EDIT_INFO', data: row })}
                        onDelete={(row) => setModalState({ type: 'DELETE', data: row })}
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
                routes={routes}
                onClose={closeModal}
                onSuccess={refetch}
            />

            <TripCrewModal
                isOpen={modalState.type === 'CREW'}
                trip={modalState.data}
                onClose={closeModal}
            />

            <Modal show={modalState.type === 'DELETE'} onHide={closeModal} centered backdrop="static">
                <Modal.Header closeButton><Modal.Title>Hủy chuyến xe</Modal.Title></Modal.Header>
                <Modal.Body>
                    <p>Bạn có chắc muốn hủy chuyến <strong>#{modalState.data?.tripId}</strong>?</p>
                    <p className="trip-delete-detail">{modalState.data?.routeName} · {modalState.data?.departureDate} · {String(modalState.data?.departureTime || '').substring(0, 5)}</p>
                    <p className="trip-delete-warning">Chuyến sẽ chuyển sang trạng thái Đã hủy và được giữ lại trong lịch sử.</p>
                    {deleteError && <Alert variant="danger">{deleteError}</Alert>}
                </Modal.Body>
                <Modal.Footer><Button variant="outline-secondary" onClick={closeModal} disabled={isDeleting}>Giữ chuyến</Button><Button variant="danger" onClick={handleDelete} disabled={isDeleting}>{isDeleting ? 'Đang hủy…' : 'Xác nhận hủy chuyến'}</Button></Modal.Footer>
            </Modal>

        </Container>
    );
}
