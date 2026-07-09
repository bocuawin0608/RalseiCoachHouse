import { Badge, Button, Card, Col, Row, Table } from 'react-bootstrap';
import {
    formatCurrency,
    formatDateTime,
    formatTicketStatus,
} from '../utils/passengerTicketFormatters';
import PassengerTicketActionsToolbar from './PassengerTicketActionsToolbar';

const QR_ELIGIBLE_STATUSES = new Set(['CONFIRMED', 'CHECKED_IN']);

export default function PassengerTicketDetailPanel({
    ticket,
    onShowQr,
    onEditPassenger,
    onChangeSeat,
    onChangeItinerary,
    onTransferTrip,
    onCancelTicket,
    suppressCancel = false,
    activeQrDetailId = null,
    qrLoading = false,
}) {
    if (!ticket) return null;

    const canChangePassengerInfo = ticket.allowedActions?.includes('CHANGE_PASSENGER_INFO');
    const canChangeSeat = ticket.allowedActions?.includes('CHANGE_SEAT');
    const canChangeItinerary = ticket.allowedActions?.includes('CHANGE_ITINERARY');
    const majorChangeUsed = Boolean(ticket.majorChangeType);
    const canTransferTrip = ticket.allowedActions?.includes('TRANSFER_TRIP') && !majorChangeUsed;
    const canCancelTicket = ticket.allowedActions?.includes('CANCEL_FULL') && !suppressCancel && !majorChangeUsed;

    const refundPolicyHint = ticket.refundPolicyDepartureTime
        ? ` (theo chuyến lúc đặt: ${formatDateTime(ticket.refundPolicyDepartureTime)})`
        : '';

    const policyHint = ticket.hoursUntilDeparture != null && ticket.hoursUntilDeparture >= 0
        ? `Còn ${ticket.hoursUntilDeparture} giờ trước giờ xe khởi hành • Hoàn tiền: ${ticket.refundTierLabel}${refundPolicyHint}`
        : 'Chuyến đã khởi hành hoặc sắp khởi hành • Không được phép hoàn tiền';

    const majorChangeHint = ticket.majorChangeType
        ? ' • Vé đã dùng quyền đổi chuyến/hủy'
        : '';

    return (
        <div>
            <Card className="shadow-sm border-0 mb-4">
                <Card.Body className="p-4">
                    <div className="d-flex flex-wrap justify-content-between gap-3 mb-3">
                        <div>
                            <Badge bg="primary" className="me-2 mb-2">{formatTicketStatus(ticket.status)}</Badge>
                            <div className="text-muted small">{policyHint}{majorChangeHint}</div>
                        </div>
                        <div className="text-end">
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

                    <PassengerTicketActionsToolbar
                        canChangeItinerary={canChangeItinerary}
                        canTransferTrip={canTransferTrip}
                        canCancelTicket={canCancelTicket}
                        cancelDisabledTooltip={
                            suppressCancel
                                ? 'Không thể hủy vé khi đang thực hiện đổi chuyến'
                                : majorChangeUsed || ticket.majorChangeType
                                    ? 'Vé đã sử dụng quyền đổi chuyến hoặc hủy vé'
                                    : 'Không thể hủy vé này'
                        }
                        transferDisabledTooltip={
                            majorChangeUsed || ticket.majorChangeType
                                ? 'Vé đã sử dụng quyền đổi chuyến hoặc hủy vé'
                                : 'Không thể đổi chuyến cho vé này'
                        }
                        onChangeItinerary={onChangeItinerary}
                        onTransferTrip={onTransferTrip}
                        onCancelTicket={onCancelTicket}
                    />
                </Card.Body>
            </Card>

            <Card className="shadow-sm border-0 mb-4">
                <Card.Header className="bg-white fw-semibold">Danh sách ghế / hành khách</Card.Header>
                <Card.Body className="p-0">
                    <div className="px-3 py-2 text-muted small border-bottom">
                        Sửa thông tin và đổi ghế áp dụng cho từng hành khách.
                    </div>
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
                                <th>Thao tác ghế</th>
                            </tr>
                        </thead>
                        <tbody>
                            {ticket.seats.map((seat) => {
                                const canShowQr = QR_ELIGIBLE_STATUSES.has(seat.status);
                                const canEditPassenger = canChangePassengerInfo && seat.status === 'CONFIRMED';
                                const canEditSeat = canChangeSeat && seat.status === 'CONFIRMED';
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
                                            <div className="d-flex flex-wrap gap-1">
                                                <Button
                                                    variant="outline-secondary"
                                                    size="sm"
                                                    onClick={() => onEditPassenger?.(seat)}
                                                    disabled={!canEditPassenger}
                                                >
                                                    Sửa thông tin
                                                </Button>
                                                <Button
                                                    variant="outline-primary"
                                                    size="sm"
                                                    onClick={() => onChangeSeat?.(seat)}
                                                    disabled={!canEditSeat}
                                                >
                                                    Đổi ghế
                                                </Button>
                                            </div>
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
