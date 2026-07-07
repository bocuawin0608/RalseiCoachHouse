import { useCallback, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Alert, Button, Form, Spinner } from 'react-bootstrap';
import { BsPlayFill, BsStopFill, BsQrCodeScan } from 'react-icons/bs';
import CargoTabPlaceholder from '../../features/tripStaff/components/CargoTabPlaceholder';
import CheckInResultModal from '../../features/tripStaff/components/CheckInResultModal';
import PassengerCard from '../../features/tripStaff/components/PassengerCard';
import TripDashboardTabs from '../../features/tripStaff/components/TripDashboardTabs';
import TripStaffSeatMapModal from '../../features/tripStaff/components/TripStaffSeatMapModal';
import { tripStaffApi } from '../../features/tripStaff/api/tripStaffApi';
import { useTripDashboard } from '../../features/tripStaff/hooks/useTripDashboard';
import { formatDateTime } from '../../utils/formatters';
import '../../features/tripStaff/components/TripStaff.css';

export default function TripDashboardPage() {
    const { tripId } = useParams();
    const navigate = useNavigate();
    const { dashboard, loading, error, refetch } = useTripDashboard(tripId);
    const [activeTab, setActiveTab] = useState('passenger');
    const [search, setSearch] = useState('');
    const [showSeatMap, setShowSeatMap] = useState(false);
    const [checkingId, setCheckingId] = useState(null);
    const [noShowingId, setNoShowingId] = useState(null);
    const [modal, setModal] = useState({ show: false, variant: 'success', message: '', result: null });
    const [tripActionLoading, setTripActionLoading] = useState(false);

    const handleStartTrip = useCallback(async () => {
        setTripActionLoading(true);
        try {
            await tripStaffApi.startTrip(tripId);
            refetch();
        } catch (err) {
            setModal({ show: true, variant: 'error', message: err.response?.data?.message || 'Không thể bắt đầu chuyến', result: null });
        } finally {
            setTripActionLoading(false);
        }
    }, [tripId, refetch]);

    const handleEndTrip = useCallback(async () => {
        setTripActionLoading(true);
        try {
            await tripStaffApi.endTrip(tripId);
            refetch();
        } catch (err) {
            setModal({ show: true, variant: 'error', message: err.response?.data?.message || 'Không thể kết thúc chuyến', result: null });
        } finally {
            setTripActionLoading(false);
        }
    }, [tripId, refetch]);

    const passengers = useMemo(() => {
        const list = dashboard?.passengers || [];
        const keyword = search.trim().toLowerCase();
        if (!keyword) return list;
        return list.filter(
            (p) =>
                p.fullName?.toLowerCase().includes(keyword) ||
                p.phone?.includes(keyword)
        );
    }, [dashboard, search]);

    const handleNoShow = async (ticketDetailId) => {
        setNoShowingId(ticketDetailId);
        try {
            await tripStaffApi.markNoShow(tripId, ticketDetailId);
            setModal({ show: true, variant: 'success', message: 'Đã đánh dấu hành khách vắng mặt. Trạng thái vé chuyển thành CANCELLED.', result: null });
            refetch();
        } catch (err) {
            setModal({
                show: true,
                variant: 'error',
                message: err.response?.data?.message || 'Không thể cập nhật',
                result: null,
            });
        } finally {
            setNoShowingId(null);
        }
    };

    const handleManualCheckIn = async (ticketDetailId) => {
        setCheckingId(ticketDetailId);
        try {
            const result = await tripStaffApi.checkInManual(tripId, ticketDetailId);
            setModal({ show: true, variant: 'success', message: '', result });
            refetch();
        } catch (err) {
            setModal({
                show: true,
                variant: 'error',
                message: err.response?.data?.message || 'Check-in thất bại',
                result: null,
            });
        } finally {
            setCheckingId(null);
        }
    };

    if (loading) {
        return (
            <div className="trip-staff-page text-center py-5">
                <Spinner animation="border" />
            </div>
        );
    }

    if (error || !dashboard) {
        return (
            <div className="trip-staff-page">
                <Alert variant="danger">{error || 'Không tải được dữ liệu'}</Alert>
            </div>
        );
    }

    const summary = dashboard.tripSummary;

    return (
        <div className="trip-staff-page">
            <div className="mb-3">
                <h5 className="fw-bold mb-1">{summary.routeName}</h5>
                <p className="text-muted mb-1" style={{ fontSize: '14px' }}>
                    {formatDateTime(summary.departureTime)} · {summary.licensePlate}
                </p>
                <p className="mb-0 fw-semibold" style={{ color: 'var(--ralsei-primary)' }}>
                    Đã lên xe: {summary.checkedInCount}/{summary.totalPassengers}
                </p>
                <div className="d-flex gap-2 mt-2">
                    {summary.tripStatus === 'SCHEDULED' && (
                        <Button size="sm" variant="success" onClick={handleStartTrip} disabled={tripActionLoading}>
                            <BsPlayFill className="me-1" />Bắt đầu chuyến
                        </Button>
                    )}
                    {summary.tripStatus === 'IN_PROGRESS' && (
                        <Button size="sm" variant="danger" onClick={handleEndTrip} disabled={tripActionLoading}>
                            <BsStopFill className="me-1" />Kết thúc chuyến
                        </Button>
                    )}
                </div>
            </div>

            <TripDashboardTabs activeTab={activeTab} onChange={setActiveTab} />

            {activeTab === 'cargo' ? (
                <CargoTabPlaceholder tripId={tripId} />
            ) : (
                <>
                    <Form.Control
                        type="search"
                        placeholder="Tìm theo SĐT hoặc tên khách..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="mb-3"
                    />

                    <Button
                        variant="outline-secondary"
                        size="sm"
                        className="mb-3"
                        onClick={() => setShowSeatMap(true)}
                    >
                        Sơ đồ ghế
                    </Button>

                    {passengers.length === 0 ? (
                        <p className="text-muted text-center py-3">Không tìm thấy hành khách.</p>
                    ) : (
                        passengers.map((passenger) => (
                            <PassengerCard
                                key={passenger.ticketDetailId}
                                passenger={passenger}
                                onCheckIn={handleManualCheckIn}
                                checkingIn={checkingId === passenger.ticketDetailId}
                                onNoShow={handleNoShow}
                                noShowing={noShowingId === passenger.ticketDetailId}
                                noShow={dashboard?.noShowTicketDetailIds?.includes(passenger.ticketDetailId)}
                            />
                        ))
                    )}

                    <button
                        type="button"
                        className="trip-staff-fab"
                        aria-label="Quét QR"
                        onClick={() => navigate(`/staff/trip/${tripId}/scan`)}
                    >
                        <BsQrCodeScan />
                    </button>
                </>
            )}

            <TripStaffSeatMapModal
                show={showSeatMap}
                onHide={() => setShowSeatMap(false)}
                seats={dashboard.seats}
            />

            <CheckInResultModal
                show={modal.show}
                variant={modal.variant}
                message={modal.message}
                result={modal.result}
                autoCloseMs={null}
                onClose={() => setModal((m) => ({ ...m, show: false }))}
            />
        </div>
    );
}
