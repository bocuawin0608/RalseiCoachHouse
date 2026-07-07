import { Badge, Button, Card, Col, Row, Table } from 'react-bootstrap';
import {
    formatCurrency,
    formatDateTime,
    formatTicketStatus,
} from '../utils/passengerTicketFormatters';

const QR_ELIGIBLE_STATUSES = new Set(['CONFIRMED', 'CHECKED_IN']);

export default function PassengerTicketDetailPanel({
    ticket,
    onShowQr,
    onEditPassenger,
    onCancelFull,
    activeQrDetailId = null,
    qrLoading = false,
}) {
    if (!ticket) return null;

    const canChangePassengerInfo = ticket.allowedActions?.includes('CHANGE_PASSENGER_INFO');
    const canCancelFull = ticket.allowedActions?.includes('CANCEL_FULL');

    const policyHint = ticket.hoursUntilDeparture != null && ticket.hoursUntilDeparture >= 0
        ? `Còn ${ticket.hoursUntilDeparture} giờ trước giờ xe khởi hành • Được phép hoàn tiền: ${ticket.refundTierLabel}`
        : 'Chuyến đã khởi hành hoặc sắp khởi hành • Không được phép hoàn tiền';

    return (
        <div>
            <Card className="shadow-sm border-0 mb-4">
                <Card.Body className="p-4">
                    <div className="d-flex flex-wrap justify-content-between gap-3 mb-3">
                        <div>
                            <Badge bg="primary" className="me-2 mb-2">{formatTicketStatus(ticket.status)}</Badge>
                            <div className="text-muted small">{policyHint}</div>
                        </div>
                        <div className="text-end">
                            <span 
                                title={!canCancelFull ? "Không thể hủy vé này" : "Bấm để hủy toàn bộ vé"} 
                                className="d-inline-block"
                            >
                                <Button
                                    variant="outline-danger"
                                    size="sm"
                                    className="mb-2"
                                    onClick={() => onCancelFull?.()}
                                    disabled={!canCancelFull}
                                    style={{
                                        cursor: !canCancelFull ? 'not-allowed' : 'pointer', 
                                        pointerEvents: 'auto' 
                                    }}
                                >
                                    Hủy vé
                                </Button>
                            </span>
                            <div className="fw-bold fs-5">{formatCurrency(ticket.totalPrice)}</div>
                            <div className="text-muted small">Tổng tiền vé</div>
                        </div>
                    </div>

                    <Row className="g-3">
                        <Col md={6} lg={4}>
                            <div className="text-muted small">Tuyến</div>
                            <div className="fw-semibold">{ticket.routeName || '—'}</div>
                        </Col>
                        <Col md={6} lg={4}>
                            <div className="text-muted small">Giờ khởi hành</div>
                            <div className="fw-semibold">{formatDateTime(ticket.departureTime)}</div>
                        </Col>
                        <Col md={6} lg={4}>
                            <div className="text-muted small">Biển số xe</div>
                            <div className="fw-semibold">{ticket.licensePlate || '—'}</div>
                        </Col>
                        <Col md={6} lg={4}>
                            <div className="text-muted small">Loại xe</div>
                            <div>{ticket.coachTypeName || '—'}</div>
                        </Col>
                        <Col md={6} lg={4}>
                            <div className="text-muted small">Điểm đón / trả</div>
                            <div>{ticket.pickupStopName} → {ticket.dropoffStopName}</div>
                        </Col>
                        <Col md={6} lg={4}>
                            <div className="text-muted small">Thanh toán</div>
                            <div>
                                {ticket.paymentMethod || '—'} • {ticket.paymentStatus || '—'}
                                {' '}({formatCurrency(ticket.paymentAmount)})
                            </div>
                        </Col>
                        <Col md={6} lg={4}>
                            <div className="text-muted small">Voucher</div>
                            <div>{ticket.voucherCodeSnapshot || 'Không có'}</div>
                        </Col>
                        <Col md={6} lg={4}>
                            <div className="text-muted small">Nhân viên bán</div>
                            <div>{ticket.soldByStaffName || 'Online'}</div>
                        </Col>
                        <Col md={6} lg={4}>
                            <div className="text-muted small">Ngày đặt</div>
                            <div>{formatDateTime(ticket.bookedAt)}</div>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            <Card className="shadow-sm border-0 mb-4">
                <Card.Header className="bg-white fw-semibold">Danh sách ghế / hành khách</Card.Header>
                <Card.Body className="p-0">
                    <Table responsive className="mb-0 align-middle">
                        <thead className="table-light">
                            <tr>
                                <th>Ghế</th>
                                <th>Hành khách</th>
                                <th>SĐT</th>
                                <th>Email</th>
                                <th>Trẻ em</th>
                                <th>Giá ghế</th>
                                <th>Trạng thái</th>
                                <th>QR</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            {ticket.seats.map((seat) => {
                                const canShowQr = QR_ELIGIBLE_STATUSES.has(seat.status);
                                const canEditPassenger = canChangePassengerInfo && seat.status === 'CONFIRMED';
                                const isActive = activeQrDetailId === seat.ticketDetailId;

                                return (
                                    <tr key={seat.ticketDetailId}>
                                        <td className="fw-semibold">{seat.seatCode}</td>
                                        <td>{seat.fullName}</td>
                                        <td>{seat.phone}</td>
                                        <td>{seat.email || '—'}</td>
                                        <td>
                                            {seat.childFullname
                                                ? `${seat.childFullname} (${seat.childBirthYear})`
                                                : '—'}
                                        </td>
                                        <td>{formatCurrency(seat.price)}</td>
                                        <td>{formatTicketStatus(seat.status)}</td>
                                        <td>
                                            {canShowQr ? (
                                                <Button
                                                    variant={isActive ? 'secondary' : 'outline-primary'}
                                                    size="sm"
                                                    onClick={() => onShowQr?.(seat)}
                                                    disabled={qrLoading && isActive}
                                                >
                                                    {qrLoading && isActive ? 'Đang tải...' : 'Xem QR'}
                                                </Button>
                                            ) : '—'}
                                        </td>
                                        <td>
                                            {canEditPassenger ? (
                                                <Button
                                                    variant="outline-secondary"
                                                    size="sm"
                                                    onClick={() => onEditPassenger?.(seat)}
                                                >
                                                    Sửa thông tin
                                                </Button>
                                            ) : '—'}
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </Table>
                </Card.Body>
            </Card>

            {ticket.refunds?.length > 0 && (
                <Card className="shadow-sm border-0">
                    <Card.Header className="bg-white fw-semibold">Lịch sử hoàn tiền</Card.Header>
                    <Card.Body className="p-0">
                        <Table responsive className="mb-0">
                            <thead className="table-light">
                                <tr>
                                    <th>Số tiền</th>
                                    <th>Trạng thái</th>
                                    <th>Lý do</th>
                                    <th>Thời gian hoàn</th>
                                </tr>
                            </thead>
                            <tbody>
                                {ticket.refunds.map((refund) => (
                                    <tr key={refund.refundId}>
                                        <td>{formatCurrency(refund.amount)}</td>
                                        <td>{refund.status}</td>
                                        <td>{refund.reason || '—'}</td>
                                        <td>{formatDateTime(refund.refundTime)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    </Card.Body>
                </Card>
            )}
        </div>
    );
}
