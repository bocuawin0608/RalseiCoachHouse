import { useState } from 'react';
import { Alert, Badge, Button, Table } from 'react-bootstrap';
import { BsEye, BsPencil, BsTelephone, BsTrash } from 'react-icons/bs';
import Pagination from '../../../components/common/Pagination';
import { formatCurrency } from '../../../utils/formatters';
import { useCargoTickets } from '../hooks/useCargoTickets';
import { cargoTicketApi } from '../api/cargoTicketApi';
import CargoTicketUpdateModal from './CargoTicketUpdateModal';
import CargoTicketDetailViewModal from './CargoTicketDetailViewModal';

const QUEUE_STATUS_PRESENTATION = {
    RECEIVED: { label: 'Đang chờ', badge: 'primary' },
    ARRIVED: { label: 'Chờ nhận hàng', badge: 'warning' },
    DELIVERED: { label: 'Đã nhận hàng', badge: 'success' }
};

/**
 * Shared operational queue. Pending orders expose update/cancel actions while
 * arrived orders expose only read-only detail and receiver confirmation.
 */
export default function CargoQueuePanel({
    status,
    tripId = null,
    editable = false,
    confirmable = false,
    onQueueChanged
}) {
    const { tickets, loading, error, pageInfo, setPageInfo, refetch } = useCargoTickets(status, tripId);
    const [editTicket, setEditTicket] = useState(null);
    const [viewTicket, setViewTicket] = useState(null);
    const [actionId, setActionId] = useState(null);

    const cancelOrder = async ticket => {
        if (!window.confirm(`Hủy đơn ${ticket.ticketCode}? Thao tác này không thể hoàn tác.`)) return;
        await runAction(ticket.cargoTicketId, () => cargoTicketApi.disableCargoTicket(ticket.cargoTicketId));
    };

    const confirmOrder = async ticket => {
        if (!window.confirm(`Xác nhận ${ticket.receiverName} đã nhận đơn ${ticket.ticketCode}?`)) return;
        await runAction(ticket.cargoTicketId, () => cargoTicketApi.confirmReceived(ticket.cargoTicketId));
    };

    const runAction = async (id, action) => {
        setActionId(id);
        try {
            await action();
            await refetch();
            await onQueueChanged?.();
        } catch (requestError) {
            window.alert(requestError.response?.data?.message || 'Không thể cập nhật đơn hàng.');
        } finally {
            setActionId(null);
        }
    };

    if (loading) return <div className="cargo-loading">Đang tải đơn hàng...</div>;
    return <section className="cargo-queue-card">
        {error && <Alert variant="danger">{error}</Alert>}
        <Table responsive hover className="cargo-queue-table align-middle mb-0">
            <thead><tr><th>Mã đơn</th><th>Người gửi / nhận</th><th>Hành trình</th><th>Chuyến xe</th><th>Trách nhiệm</th><th>Tiền hàng</th><th>Hành động</th></tr></thead>
            <tbody>
                {tickets.length === 0 && <tr><td colSpan="7" className="cargo-empty">Không có đơn hàng trong trạng thái này.</td></tr>}
                {tickets.map(ticket => <tr key={ticket.cargoTicketId}>
                    <td>
                        <strong>{ticket.ticketCode}</strong>
                        <div>
                            <Badge bg={(QUEUE_STATUS_PRESENTATION[ticket.status] || QUEUE_STATUS_PRESENTATION[status])?.badge || 'secondary'}>
                                {(QUEUE_STATUS_PRESENTATION[ticket.status] || QUEUE_STATUS_PRESENTATION[status])?.label || ticket.status}
                            </Badge>
                        </div>
                    </td>
                    <td><Contact label="Gửi" name={ticket.senderName} phone={ticket.senderPhone} /><Contact label="Nhận" name={ticket.receiverName} phone={ticket.receiverPhone} /></td>
                    <td><strong>{ticket.routeName || '—'}</strong><small>{ticket.pickupStopName} → {ticket.dropoffStopName}</small><small>Văn phòng nhận: {ticket.destinationAgencyName || 'Chưa gán'}</small></td>
                    <td>
                        <strong>{ticket.licensePlate || 'Chưa gán xe'}</strong>
                        {editable && <small>Chuyến #{ticket.tripId || '—'}</small>}
                    </td>
                    <td><small>Tài xế: {ticket.driverName || '—'} · {ticket.driverPhone || '—'}</small><small>Phụ xe: {ticket.attendantName || '—'} · {ticket.attendantPhone || '—'}</small></td>
                    <td><strong>{formatCurrency(ticket.totalPrice)}</strong><small>COD: {formatCurrency(ticket.codAmount)}</small></td>
                    <td><div className="cargo-actions">
                        <Button variant="outline-success" title="Xem đầy đủ" onClick={() => setViewTicket(ticket)}><BsEye /></Button>
                        {editable && <Button variant="success" title="Cập nhật" onClick={() => setEditTicket(ticket)}><BsPencil /></Button>}
                        {editable && <Button variant="outline-danger" title="Hủy đơn" disabled={actionId === ticket.cargoTicketId} onClick={() => cancelOrder(ticket)}><BsTrash /></Button>}
                        {confirmable && <Button className="cargo-primary-button text-nowrap" disabled={actionId === ticket.cargoTicketId} onClick={() => confirmOrder(ticket)}>Đã nhận hàng</Button>}
                    </div></td>
                </tr>)}
            </tbody>
        </Table>
        <div className="cargo-pagination"><Pagination pageInfo={pageInfo} onPageChange={setPageInfo} /></div>
        {editTicket && <CargoTicketUpdateModal key={editTicket.cargoTicketId} data={editTicket} onClose={() => { setEditTicket(null); refetch(); }} onSuccess={refetch} />}
        {viewTicket && <CargoTicketDetailViewModal key={viewTicket.cargoTicketId} ticket={viewTicket} onClose={() => { setViewTicket(null); refetch(); }} readOnly={!editable} />}
    </section>;
}

/** Renders one party without hiding the phone number needed for hand-over. */
function Contact({ label, name, phone }) {
    return <div className="cargo-contact"><span>{label}</span><strong>{name}</strong><small><BsTelephone /> {phone}</small></div>;
}
