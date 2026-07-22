import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Modal } from 'react-bootstrap';
import { BsBoxSeam, BsBusFront, BsCalendar3, BsClock, BsExclamationTriangleFill, BsPeople, BsPlusLg } from 'react-icons/bs';
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
        trips, incidents, loading, pageInfo, setPageInfo, refetch,
        filters, handleFilterChange, handleReset, error
    } = useTrips();

    /** Modal state: type discriminates which modal is open, data holds the selected row */
    const [modalState, setModalState] = useState({ type: null, data: null });
    const [deleteError, setDeleteError] = useState('');
    const [isDeleting, setIsDeleting] = useState(false);
    const [now, setNow] = useState(() => new Date());

    useEffect(() => {
        const timerId = window.setInterval(() => setNow(new Date()), 1000);
        return () => window.clearInterval(timerId);
    }, []);

    const overview = useMemo(() => {
        const activeStatuses = new Set(['BOARDING', 'DEPARTED', 'IN_PROGRESS', 'INPROGRESS']);
        return trips.reduce((result, trip) => {
            const status = String(trip.tripStatus || '').toUpperCase();
            const cargoPercent = (Number(trip.usedCargoVolume) || 0) / (Number(trip.cargoCapacity) || 2.5);
            result.active += activeStatuses.has(status) ? 1 : 0;
            result.missingCrew += (!trip.driverName || !trip.attendantName) ? 1 : 0;
            result.cargoAlert += cargoPercent >= .9 ? 1 : 0;
            return result;
        }, { active: 0, missingCrew: 0, cargoAlert: 0 });
    }, [trips]);

    const selectedDate = filters.date
        ? new Date(`${filters.date}T00:00:00`).toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' })
        : '';

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
        <main className="trip-page-shell">

            <div className="trip-page-toolbar">
                <span />
                <Button
                    className="trip-page-create custom-btn-general"
                    onClick={() => navigate('/management/trips/create')}
                >
                    <BsPlusLg /> Tạo chuyến mới
                </Button>
            </div>

            <header className="trip-page-heading">
                <p className="trip-page-eyebrow">Vận hành chuyến xe</p>
                <h1>Quản lý chuyến xe</h1>
                <p>Theo dõi lịch chạy, phương tiện, tổ lái và mức tải trước khi xe rời bến.</p>

                <div className="trip-context-row">
                    <div className="trip-date-context">
                        <BsCalendar3 />
                        <span>
                            <small>Ngày vận hành</small>
                            <strong>{selectedDate}</strong>
                            <em>{pageInfo.totalElements} chuyến theo bộ lọc</em>
                        </span>
                    </div>
                    <div className="trip-current-time" aria-live="off">
                        <BsClock />
                        <span>
                            <small>Thời gian hiện tại</small>
                            <strong>{now.toLocaleTimeString('vi-VN', { hour12: false })}</strong>
                            <em>{now.toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' })}</em>
                        </span>
                    </div>
                </div>
            </header>

            <section className="trip-overview" aria-label="Tổng quan trang hiện tại">
                <div><i className="is-green"><BsBusFront /></i><span><strong>{overview.active}</strong><small>Đang vận hành</small></span></div>
                <div><i className={overview.missingCrew ? 'is-amber' : 'is-green'}><BsPeople /></i><span><strong>{overview.missingCrew}</strong><small>Thiếu nhân sự</small></span></div>
                <div><i className={overview.cargoAlert ? 'is-red' : 'is-green'}><BsBoxSeam /></i><span><strong>{overview.cargoAlert}</strong><small>Khoang hàng ≥ 90%</small></span></div>
                <small className="trip-overview-scope">Chỉ số trên trang hiện tại</small>
            </section>

            {incidents.map(trip => (
                <div className="trip-manager-emergency" role="alert" key={trip.tripId}>
                    <BsExclamationTriangleFill />
                    <strong>KHẨN CẤP: XE CÓ BIỂN SỐ {trip.licensePlate} GẶP SỰ CỐ KHÔNG THỂ KHẮC PHỤC!!!</strong>
                    <span>Chuyến #{trip.tripId} · {String(trip.routeName || '').replace(/\s*-\s*/, ' → ')}</span>
                </div>
            ))}

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

        </main>
    );
}
