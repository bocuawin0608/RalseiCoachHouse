import { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Badge, Button, Form, Spinner, Table } from 'react-bootstrap';
import { BsArrowLeft, BsBusFront, BsCheck2Square } from 'react-icons/bs';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { cargoTicketApi } from '../../../features/cargoTickets/api/cargoTicketApi';
import { formatCurrency } from '../../../utils/formatters';
import '../../../features/cargoTickets/styles/CargoOperations.css';

const DEFAULT_CAPACITY = 2.5;

/** Batch-assigns waiting unassigned cargo orders onto one scheduled coach. */
export default function CargoTripAssignPage() {
    const navigate = useNavigate();
    const { tripId } = useParams();
    const { state } = useLocation();
    const tripFromState = state?.trip;

    const [board, setBoard] = useState(null);
    const [selectedIds, setSelectedIds] = useState(() => new Set());
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const goToTrips = () => navigate('/staff/cargo-tickets/send', { state: { showTrips: true } });

    const loadBoard = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            const response = await cargoTicketApi.getAssignableCargo(Number(tripId));
            setBoard(response);
            setSelectedIds(new Set());
        } catch (requestError) {
            setBoard(null);
            setError(requestError.response?.data?.message || 'Không thể tải danh sách đơn có thể gán.');
        } finally {
            setLoading(false);
        }
    }, [tripId]);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadBoard();
    }, [loadBoard]);

    const usedVolume = Number(board?.usedCargoVolume ?? tripFromState?.usedCargoVolume ?? 0);
    const capacity = Number(board?.cargoCapacity ?? tripFromState?.cargoCapacity ?? DEFAULT_CAPACITY);
    const tickets = board?.tickets ?? [];

    const selectedVolume = useMemo(() => {
        return tickets
            .filter((ticket) => selectedIds.has(ticket.cargoTicketId))
            .reduce((sum, ticket) => sum + Number(ticket.occupiedVolume || 0), 0);
    }, [tickets, selectedIds]);

    const remainingAfterSelection = capacity - usedVolume - selectedVolume;
    const wouldExceed = remainingAfterSelection < -1e-9;
    const nextOccupiedPercent = Math.min(100, capacity
        ? ((usedVolume + selectedVolume) / capacity) * 100
        : 100);

    const toggleTicket = (ticket) => {
        const volume = Number(ticket.occupiedVolume || 0);
        setSelectedIds((previous) => {
            const next = new Set(previous);
            if (next.has(ticket.cargoTicketId)) {
                next.delete(ticket.cargoTicketId);
                return next;
            }
            const currentSelectedVolume = tickets
                .filter((row) => next.has(row.cargoTicketId))
                .reduce((sum, row) => sum + Number(row.occupiedVolume || 0), 0);
            if (usedVolume + currentSelectedVolume + volume > capacity + 1e-9) {
                setError(`Không đủ chỗ: còn ${(capacity - usedVolume - currentSelectedVolume).toFixed(2)} m³, đơn cần ${volume.toFixed(2)} m³.`);
                return previous;
            }
            setError('');
            next.add(ticket.cargoTicketId);
            return next;
        });
    };

    const handleAssign = async () => {
        if (selectedIds.size === 0 || wouldExceed) return;
        setSubmitting(true);
        setError('');
        setSuccess('');
        try {
            const response = await cargoTicketApi.assignCargoToTrip(Number(tripId), {
                cargoTicketIds: [...selectedIds]
            });
            setSuccess(`Đã gán ${response.assignedCount} đơn vào chuyến #${response.tripId}.`);
            await loadBoard();
        } catch (requestError) {
            setError(requestError.response?.data?.message || 'Không thể gán đơn vào chuyến.');
        } finally {
            setSubmitting(false);
        }
    };

    const trip = tripFromState || {};
    const plate = trip.licensePlate || 'Chưa có biển số';
    const routeName = trip.routeName || `Chuyến #${tripId}`;

    return (
        <main className="cargo-operations-page">
            <div className="cargo-toolbar">
                <Button variant="link" className="cargo-back" onClick={goToTrips}>
                    <BsArrowLeft /> Quay lại chuyến xe
                </Button>
            </div>

            <header className="cargo-page-heading compact">
                <p className="cargo-eyebrow">Gán hàng vào chuyến</p>
                <h1>{routeName}</h1>
                <p>
                    Chỉ hiện đơn đang chờ, đã đủ điều kiện gán chuyến: lộ trình khớp,
                    có hàng, và nếu người gửi trả phí thì phải đã thanh toán xong
                    (đơn chuyển khoản đang chờ CK không hiện ở đây).
                </p>
            </header>

            <section className="cargo-assign-trip-summary">
                <div className="cargo-coach-line">
                    <BsBusFront />
                    <strong>{plate}</strong>
                    {trip.coachTypeName && <span>{trip.coachTypeName}</span>}
                    {trip.pickupTime && <time>{formatDateTime(trip.pickupTime)}</time>}
                </div>
                <div className="cargo-capacity">
                    <div>
                        <span>Khoang hàng sau khi chọn</span>
                        <strong>
                            {(usedVolume + selectedVolume).toFixed(2)} / {capacity.toFixed(2)} m³
                        </strong>
                    </div>
                    <div className="cargo-capacity-track">
                        <span style={{ width: `${nextOccupiedPercent}%` }} />
                    </div>
                    <small>
                        Đã trên xe: {usedVolume.toFixed(2)} m³ · Đang chọn: {selectedVolume.toFixed(2)} m³ · Còn lại:{' '}
                        {Math.max(0, remainingAfterSelection).toFixed(2)} m³
                    </small>
                </div>
            </section>

            <Alert variant="light" className="cargo-assign-hint border">
                Chỉ hiện đơn khớp chiều xe (điểm đón → điểm trả theo thứ tự trên tuyến),
                đã có hàng, và nếu người gửi trả phí thì phải đã thanh toán xong.
                Đơn chiều ngược hoặc chờ chuyển khoản của người gửi không hiện ở đây.
            </Alert>

            {error && <Alert variant="danger">{error}</Alert>}
            {success && <Alert variant="success">{success}</Alert>}

            {loading ? (
                <div className="cargo-loading"><Spinner size="sm" /> Đang tải đơn có thể gán...</div>
            ) : (
                <section className="cargo-queue-card">
                    <Table responsive hover className="cargo-queue-table align-middle mb-0">
                        <thead>
                            <tr>
                                <th style={{ width: 48 }} />
                                <th>Mã đơn</th>
                                <th>Người gửi / nhận</th>
                                <th>Điểm trả</th>
                                <th>Thanh toán</th>
                                <th>Thể tích</th>
                                <th>Cước</th>
                            </tr>
                        </thead>
                        <tbody>
                            {tickets.length === 0 && (
                                <tr>
                                    <td colSpan="7" className="cargo-empty">
                                        Không có đơn đủ điều kiện để gán (chưa CK của người gửi, sai lộ trình, hoặc chưa có hàng).
                                    </td>
                                </tr>
                            )}
                            {tickets.map((ticket) => {
                                const checked = selectedIds.has(ticket.cargoTicketId);
                                const volume = Number(ticket.occupiedVolume || 0);
                                const currentSelectedVolume = tickets
                                    .filter((row) => selectedIds.has(row.cargoTicketId) && row.cargoTicketId !== ticket.cargoTicketId)
                                    .reduce((sum, row) => sum + Number(row.occupiedVolume || 0), 0);
                                const cannotSelect = !checked
                                    && usedVolume + currentSelectedVolume + volume > capacity + 1e-9;
                                return (
                                    <tr key={ticket.cargoTicketId} className={checked ? 'cargo-assign-row-selected' : ''}>
                                        <td>
                                            <Form.Check
                                                type="checkbox"
                                                checked={checked}
                                                disabled={cannotSelect || submitting}
                                                onChange={() => toggleTicket(ticket)}
                                                aria-label={`Chọn đơn ${ticket.ticketCode}`}
                                            />
                                        </td>
                                        <td>
                                            <strong>{ticket.ticketCode}</strong>
                                            <div><Badge bg="primary">Đang chờ</Badge></div>
                                        </td>
                                        <td>
                                            <small>Gửi: {ticket.senderName} · {ticket.senderPhone}</small>
                                            <small>Nhận: {ticket.receiverName} · {ticket.receiverPhone}</small>
                                        </td>
                                        <td>
                                            <strong>{ticket.dropoffStopName || `Stop #${ticket.dropoffStopId}`}</strong>
                                            <small>{ticket.pickupStopName} → {ticket.dropoffStopName}</small>
                                        </td>
                                        <td>
                                            <small>{feePayerLabel(ticket.feePayer)}</small>
                                            <Badge bg={paymentBadge(ticket.paymentStatus)}>
                                                {paymentLabel(ticket.paymentStatus, ticket.paymentMethod)}
                                            </Badge>
                                        </td>
                                        <td><strong>{volume.toFixed(2)} m³</strong></td>
                                        <td><strong>{formatCurrency(ticket.totalPrice)}</strong></td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </Table>
                </section>
            )}

            <div className="cargo-assign-actions">
                <Button variant="outline-secondary" onClick={goToTrips} disabled={submitting}>
                    Hủy
                </Button>
                <Button
                    className="cargo-primary-button"
                    disabled={selectedIds.size === 0 || wouldExceed || submitting || loading}
                    onClick={handleAssign}
                >
                    <BsCheck2Square />
                    {submitting ? 'Đang gán...' : `Gán vào chuyến (${selectedIds.size})`}
                </Button>
            </div>
        </main>
    );
}

function formatDateTime(value) {
    return value
        ? new Date(value).toLocaleString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit',
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        })
        : 'Chưa có giờ chạy';
}

function feePayerLabel(feePayer) {
    if (feePayer === 'RECEIVER') return 'Người nhận trả';
    if (feePayer === 'SENDER') return 'Người gửi trả';
    return feePayer || '—';
}

function paymentLabel(status, method) {
    const methodLabel = method === 'BANK_TRANSFER' ? 'CK' : method === 'CASH' ? 'TM' : '';
    if (status === 'COMPLETED') return methodLabel ? `Đã thu (${methodLabel})` : 'Đã thu';
    if (status === 'PENDING') return methodLabel ? `Chờ (${methodLabel})` : 'Chờ thanh toán';
    if (status === 'FAILED') return 'Thất bại';
    return status || 'Chưa có TT';
}

function paymentBadge(status) {
    if (status === 'COMPLETED') return 'success';
    if (status === 'PENDING') return 'warning';
    if (status === 'FAILED') return 'danger';
    return 'secondary';
}
