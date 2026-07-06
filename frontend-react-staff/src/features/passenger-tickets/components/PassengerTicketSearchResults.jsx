import { Badge, Table } from 'react-bootstrap';
import {
    formatCurrency,
    formatDateTime,
    formatTicketStatus,
} from '../utils/passengerTicketFormatters';

const statusVariant = {
    CONFIRMED: 'success',
    CHANGED: 'warning',
    PENDING: 'primary',
    CANCELLED: 'danger',
};

export default function PassengerTicketSearchResults({
    data,
    loading,
    hasSearched,
    onSelectTicket,
}) {
    if (loading) {
        return <div className="py-5 text-center text-muted">Đang tải dữ liệu...</div>;
    }

    if (!hasSearched) {
        return (
            <div className="py-5 text-center text-muted">
                Nhập SĐT, mã vé hoặc chọn ngày khởi hành để tra cứu.
            </div>
        );
    }

    if (!data?.length) {
        return <div className="py-5 text-center text-muted">Không tìm thấy vé phù hợp.</div>;
    }

    return (
        <Table responsive hover className="align-middle mb-0">
            <thead className="table-light">
                <tr>
                    <th>Mã vé</th>
                    <th>Khách hàng</th>
                    <th>SĐT</th>
                    <th>Tuyến</th>
                    <th>Biển số</th>
                    <th>Giờ khởi hành</th>
                    <th>Ghế</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                </tr>
            </thead>
            <tbody>
                {data.map((item) => (
                    <tr
                        key={item.passengerTicketId}
                        style={{ cursor: 'pointer' }}
                        onClick={() => onSelectTicket(item.ticketCode)}
                    >
                        <td className="fw-semibold">{item.ticketCode}</td>
                        <td>{item.primaryPassengerName || '—'}</td>
                        <td>{item.primaryPhone || '—'}</td>
                        <td>{item.routeName || '—'}</td>
                        <td className="fw-semibold">{item.licensePlate || '—'}</td>
                        <td>{formatDateTime(item.departureTime)}</td>
                        <td>{item.seatCodes?.join(', ') || '—'}</td>
                        <td>{formatCurrency(item.totalPrice)}</td>
                        <td>
                            <Badge bg={statusVariant[item.status] || 'secondary'}>
                                {formatTicketStatus(item.status)}
                            </Badge>
                        </td>
                    </tr>
                ))}
            </tbody>
        </Table>
    );
}
