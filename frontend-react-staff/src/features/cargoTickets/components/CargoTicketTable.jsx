import { useState } from 'react';
import { Badge, Button, Table } from 'react-bootstrap';
import { BsPencilFill, BsTrashFill, BsCashCoin } from 'react-icons/bs';
import { formatCurrency } from '../../../utils/formatters';
import { FaArrowDownLong } from "react-icons/fa6";
import QrPaymentModal from './QrPaymentModal';

const statusVariant = {
    RECEIVED: 'primary',
    LOADED: 'info',
    ARRIVED: 'warning',
    DELIVERED: 'success',
    CANCELLED: 'secondary',
    REJECTED: 'danger',
    ABANDONED: 'dark'
};

const statusLabel = {
    RECEIVED: 'Đã nhận hàng',
    LOADED: 'Đã xếp hàng',
    ARRIVED: 'Đã đến nơi',
    DELIVERED: 'Đã giao',
    CANCELLED: 'Đã hủy',
    REJECTED: 'Từ chối',
    ABANDONED: 'Bỏ hàng'
};

const paymentStatusVariant = {
    PENDING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    CANCELLED: 'secondary'
};

const paymentStatusLabel = {
    PENDING: 'Chưa TT',
    COMPLETED: 'Đã TT',
    FAILED: 'Thất bại',
    CANCELLED: 'Đã hủy'
};

export default function CargoTicketTable({ data, loading, onEdit, onDisable, onCompletePayment }) {
    const [qrTicket, setQrTicket] = useState(null);

    if (loading) return <div className="text-center p-5 text-secondary fw-medium">Đang tải dữ liệu...</div>;

    const handlePaymentClick = (ticket) => {
        if (!ticket.payment || ticket.payment.status !== 'PENDING') return;

        if (ticket.payment.paymentMethod === 'CASH') {
            if (window.confirm('Xác nhận đã nhận tiền mặt cho vé ' + ticket.ticketCode + '?')) {
                onCompletePayment(ticket);
            }
        } else if (ticket.payment.paymentMethod === 'BANK_TRANSFER') {
            setQrTicket(ticket);
        }
    };

    return (
        <>
            <Table responsive hover className="align-middle mb-0">
                <thead className="table-light text-secondary">
                    <tr>
                        <th className="py-3 px-3">Mã vé</th>
                        <th className="py-3 px-3">Người gửi</th>
                        <th className="py-3 px-3">Người nhận</th>
                        <th className="py-3 px-3">Điểm nhận → trả</th>
                        <th className="py-3 px-3">Tổng tiền</th>
                        <th className="py-3 px-3">COD</th>
                        <th className="py-3 px-3">Thanh toán</th>
                        <th className="py-3 px-3">Trạng thái</th>
                        <th className="py-3 px-3 text-center">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    {!data.length ? (
                        <tr><td colSpan="9" className="text-center p-5 text-muted">Không tìm thấy dữ liệu</td></tr>
                    ) : data.map((ticket) => (
                        <tr key={ticket.cargoTicketId}>
                            <td className="px-3 fw-bold text-dark">{ticket.ticketCode}</td>
                            <td className="px-3">{ticket.senderPhone}<div className="small text-muted">{ticket.senderName}</div></td>
                            <td className="px-3">{ticket.receiverPhone}<div className="small text-muted">{ticket.receiverName}</div></td>
                            <td className="px-3 text-nowrap">
                                <div>{ticket.pickupStopName}</div>
                                <div className="text-center"><FaArrowDownLong /></div>
                                <div>{ticket.dropoffStopName}</div>
                            </td>
                            <td className="px-3 fw-semibold">{formatCurrency(ticket.totalPrice)}</td>
                            <td className="px-3">{formatCurrency(ticket.codAmount)}</td>
                            <td className="px-3">
                                {ticket.payment ? (
                                    <Badge bg={paymentStatusVariant[ticket.payment.status] || 'secondary'}>
                                        {paymentStatusLabel[ticket.payment.status] || ticket.payment.status}
                                    </Badge>
                                ) : (
                                    <span className="text-muted small">N/A</span>
                                )}
                            </td>
                            <td className="px-3"><Badge bg={statusVariant[ticket.status] || 'secondary'}>{statusLabel[ticket.status] || ticket.status}</Badge></td>
                            <td className="px-3">
                                <div className="d-flex gap-2 justify-content-center">
                                    {ticket.payment?.status === 'PENDING' && (
                                        <Button variant="success" className="d-flex align-items-center" onClick={() => handlePaymentClick(ticket)} title="Hoàn thành thanh toán">
                                            <BsCashCoin size={16} />
                                        </Button>
                                    )}
                                    <Button className="d-flex align-items-center custom-btn-general" onClick={() => onEdit(ticket)} title="Sửa vé">
                                        <BsPencilFill size={16} />
                                    </Button>
                                    <Button variant="danger" className="d-flex align-items-center" onClick={() => onDisable(ticket)} title="Vô hiệu hóa vé">
                                        <BsTrashFill size={16} />
                                    </Button>
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </Table>

            <QrPaymentModal ticket={qrTicket} onClose={() => setQrTicket(null)} />
        </>
    );
}
