import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Badge, Button, Spinner } from 'react-bootstrap';
import { BsArrowLeft, BsBusFront, BsClock, BsGeoAltFill, BsPeople, BsTelephone, BsPersonVcard } from 'react-icons/bs';
import { cargoTicketApi } from '../../../features/cargoTickets/api/cargoTicketApi';
import CargoQueuePanel from '../../../features/cargoTickets/components/CargoQueuePanel';
import Pagination from '../../../components/common/Pagination';
import '../../../features/cargoTickets/styles/CargoOperations.css';

/** Lists upcoming coaches before staff attach a new cargo order to a trip. */
export default function CargoSendPage() {
    const navigate = useNavigate();
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showPending, setShowPending] = useState(false);
    const [agency, setAgency] = useState(null);
    const [now, setNow] = useState(() => new Date());
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: 6,
        totalElements: 0,
        totalPages: 0
    });

    const loadTrips = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            const response = await cargoTicketApi.getUpcomingTrips({
                page: pageInfo.page,
                size: pageInfo.size
            });
            const tripPage = response.trips ?? {};
            setAgency({
                ticketAgencyId: response.ticketAgencyId,
                ticketAgencyName: response.ticketAgencyName,
                stopPointName: response.stopPointName,
                city: response.city
            });
            setTrips(tripPage.content ?? []);
            setPageInfo(previous => ({
                ...previous,
                page: tripPage.pageNumber ?? previous.page,
                totalElements: tripPage.totalElements ?? 0,
                totalPages: tripPage.totalPages ?? 0
            }));
        } catch (requestError) {
            setError(requestError.response?.data?.message || 'Không thể tải danh sách chuyến xe sắp chạy.');
        } finally {
            setLoading(false);
        }
    }, [pageInfo.page, pageInfo.size]);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadTrips();
    }, [loadTrips]);

    useEffect(() => {
        const timerId = window.setInterval(() => setNow(new Date()), 1000);
        return () => window.clearInterval(timerId);
    }, []);

    return (
        <main className="cargo-operations-page">
            <div className="cargo-toolbar">
                <Button variant="link" className="cargo-back" onClick={() => navigate('/staff/cargo-tickets')}><BsArrowLeft /> Đơn hàng</Button>
                <Button className="cargo-primary-button" onClick={() => setShowPending(value => !value)}>
                    {showPending ? 'Xem chuyến xe' : 'Xem đơn đang chờ'}
                </Button>
            </div>
            <header className="cargo-page-heading compact">
                <p className="cargo-eyebrow">Gửi hàng</p>
                <h1>{showPending ? 'Đơn hàng đang chờ' : 'Chuyến xe có thể nhận hàng'}</h1>
                <p>{showPending ? 'Chỉ các đơn ở trạng thái chờ mới được sửa hoặc hủy.' : 'Chọn chuyến để xem trách nhiệm, lộ trình và sức chứa trước khi lập đơn.'}</p>
                <div className="cargo-context-row">
                    {agency && <div className="cargo-agency-context"><BsGeoAltFill /><span><small>Văn phòng hiện tại</small><strong>{agency.ticketAgencyName}</strong><em>{agency.stopPointName} · {agency.city}</em></span></div>}
                    <div className="cargo-current-time" aria-live="off">
                        <BsClock />
                        <span><small>Thời gian hiện tại</small><strong>{formatCurrentTime(now)}</strong><em>{formatCurrentDate(now)}</em></span>
                    </div>
                </div>
            </header>

            {showPending ? <CargoQueuePanel status="RECEIVED" editable /> : (
                <>
                    {error && <Alert variant="danger">{error}</Alert>}
                    {loading ? <div className="cargo-loading"><Spinner size="sm" /> Đang tải chuyến xe...</div> : (
                        <section className="cargo-trip-grid">
                            {trips.length === 0 && <div className="cargo-empty">Hôm nay không còn chuyến nào ghé văn phòng của bạn và còn chỗ.</div>}
                            {trips.map(trip => <TripCard key={trip.tripId} trip={trip} onSelect={() => navigate('/staff/cargo-tickets/create', { state: { trip } })} />)}
                        </section>
                    )}
                    {!loading && <div className="cargo-pagination"><Pagination pageInfo={pageInfo} onPageChange={setPageInfo} /></div>}
                </>
            )}
        </main>
    );
}

/** Presents the operational facts required before staff select a coach. */
function TripCard({ trip, onSelect }) {
    const used = Number(trip.usedCargoVolume || 0);
    const capacity = Number(trip.cargoCapacity || 2);
    const percent = Math.min(100, capacity ? (used / capacity) * 100 : 100);
    return (
        <article className={`cargo-trip-card ${trip.full ? 'is-full' : ''}`}>
            <div className="cargo-trip-top">
                <div><h2>{trip.routeName}</h2></div>
                <Badge bg={trip.full ? 'danger' : 'success'}>{trip.full ? 'Đã đầy' : 'Còn chỗ'}</Badge>
            </div>
            <div className="cargo-coach-line"><BsBusFront /><strong>{trip.licensePlate}</strong><span>{trip.coachTypeName}</span><time>{formatDateTime(trip.pickupTime)}</time></div>
            <p className="cargo-stops"><strong>Nhận tại:</strong> {trip.pickupStopName} ({trip.pickupCity})</p>
            <p className="cargo-stops">{trip.stopSummary || 'Chưa cập nhật điểm dừng'}</p>
            <div className="cargo-responsibility-grid">
                <StaffBlock label="Tài xế" name={trip.driverName} phone={trip.driverPhone} cccd={trip.driverCccd} />
                <StaffBlock label="Phụ xe" name={trip.attendantName} phone={trip.attendantPhone} cccd={trip.attendantCccd} />
            </div>
            <div className="cargo-capacity"><div><span>Khoang hàng</span><strong>{used.toFixed(2)} / {capacity.toFixed(2)} m³</strong></div><div className="cargo-capacity-track"><span style={{ width: `${percent}%` }} /></div></div>
            <Button className="cargo-primary-button w-100" disabled={trip.full} onClick={onSelect}>{trip.full ? 'Chuyến đã đầy' : 'Chọn chuyến và thêm hàng'}</Button>
        </article>
    );
}

/** Displays contact and identity data for one assigned staff member. */
function StaffBlock({ label, name, phone, cccd }) {
    return <div className="cargo-staff-block"><span><BsPeople /> {label}</span><strong>{name || 'Chưa phân công'}</strong><small><BsTelephone /> {phone || '—'}</small><small><BsPersonVcard /> {cccd || '—'}</small></div>;
}

/** Formats backend timestamps consistently for Vietnamese ticket staff. */
function formatDateTime(value) {
    return value ? new Date(value).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' }) : 'Chưa có giờ chạy';
}

function formatCurrentTime(date) {
    return date.toLocaleTimeString('vi-VN', {
        hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
    });
}

function formatCurrentDate(date) {
    return date.toLocaleDateString('vi-VN', {
        weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric'
    });
}
