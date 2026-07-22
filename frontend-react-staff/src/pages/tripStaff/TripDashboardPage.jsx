import { useCallback, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Alert, Button, Form, Modal, Spinner } from 'react-bootstrap';
import { BsExclamationOctagonFill, BsPlayFill, BsStopFill, BsQrCodeScan } from 'react-icons/bs';
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
    const [modal, setModal] = useState({ show: false, variant: 'success', message: '', result: null });
    const [tripActionLoading, setTripActionLoading] = useState(false);
    const [incidentStep, setIncidentStep] = useState(0);
    const [incidentSubmitting, setIncidentSubmitting] = useState(false);

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

    const handleIncidentConfirmation = async () => {
        if (incidentStep < 3) {
            setIncidentStep(step => step + 1);
            return;
        }
        setIncidentSubmitting(true);
        try {
            await tripStaffApi.reportIncident(tripId);
            setIncidentStep(0);
            await refetch();
        } catch (err) {
            setIncidentStep(0);
            setModal({ show: true, variant: 'error', message: err.response?.data?.message || 'Không thể báo sự cố', result: null });
        } finally {
            setIncidentSubmitting(false);
        }
    };

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
    const hasIncident = summary.coachStatus === 'HAVE_INCIDENT';

    return (
        <div className={`trip-staff-page${hasIncident ? ' is-incident' : ''}`}>
            {hasIncident && (
                <div className="trip-staff-emergency" role="alert">
                    <BsExclamationOctagonFill />
                    <strong>XE {summary.licensePlate} ĐÃ GẶP SỰ CỐ KHÔNG THỂ KHẮC PHỤC</strong>
                    <span>Chuyến đi đã bị khóa hoàn thành. Quản lý đã được cảnh báo.</span>
                </div>
            )}
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
                        <Button size="sm" variant="success" onClick={handleEndTrip} disabled={tripActionLoading || hasIncident} title={hasIncident ? 'Xe gặp sự cố nên không thể hoàn thành chuyến' : ''}>
                            <BsStopFill className="me-1" />Kết thúc chuyến
                        </Button>
                    )}
                    {summary.tripStatus === 'IN_PROGRESS' && (
                        <Button size="sm" className="trip-incident-button" onClick={() => setIncidentStep(1)} disabled={tripActionLoading || incidentSubmitting || hasIncident}>
                            <BsExclamationOctagonFill className="me-1" />{hasIncident ? 'Đã báo sự cố' : 'Xảy ra sự cố'}
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

            <Modal
                show={incidentStep > 0}
                onHide={() => !incidentSubmitting && setIncidentStep(0)}
                centered
                backdrop="static"
                keyboard={false}
                className="trip-incident-modal"
            >
                <Modal.Header>
                    <Modal.Title><BsExclamationOctagonFill /> Cảnh báo sự cố · {incidentStep}/3</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <strong>{incidentStep === 1 && 'Xe thực sự gặp sự cố không thể tiếp tục hành trình?'}</strong>
                    <strong>{incidentStep === 2 && 'Xác nhận lần hai: sự cố này KHÔNG THỂ khắc phục tại chỗ?'}</strong>
                    <strong>{incidentStep === 3 && `CẢNH BÁO CUỐI: Xe ${summary.licensePlate} sẽ bị khóa và chuyến không thể hoàn thành.`}</strong>
                    <p>Hành động này sẽ báo khẩn cấp đến quản lý và không thể hoàn tác từ màn hình chuyến đi.</p>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="light" onClick={() => setIncidentStep(0)} disabled={incidentSubmitting}>Quay lại</Button>
                    <Button className="trip-incident-confirm" onClick={handleIncidentConfirmation} disabled={incidentSubmitting}>
                        {incidentSubmitting ? 'Đang gửi cảnh báo…' : incidentStep < 3 ? 'Tôi xác nhận — tiếp tục' : 'BẠN KHÔNG THỂ QUAY LẠI BƯỚC NÀY'}
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}
