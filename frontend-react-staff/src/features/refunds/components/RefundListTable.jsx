import { Spinner, Table } from 'react-bootstrap';
import {
    formatCurrency,
    formatRefundMethod,
    formatRefundStatus,
    truncateText,
} from '../utils/refundFormatters';

export default function RefundListTable({ data, loading, onSelectRefund }) {
    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="success" />
            </div>
        );
    }

    if (!data.length) {
        return (
            <div className="text-center text-muted py-5">
                Không có yêu cầu hoàn tiền phù hợp.
            </div>
        );
    }

    return (
        <Table hover responsive className="mb-0 align-middle">
            <thead className="table-light">
                <tr>
                    <th>Mã vé</th>
                    <th>Khách hàng</th>
                    <th>SĐT</th>
                    <th>Số tiền hoàn</th>
                    <th>Phương thức</th>
                    <th>Trạng thái</th>
                    <th>Lý do</th>
                </tr>
            </thead>
            <tbody>
                {data.map((refund) => (
                    <tr
                        key={refund.refundId}
                        onClick={() => onSelectRefund(refund)}
                        style={{ cursor: 'pointer' }}
                    >
                        <td className="fw-semibold">{refund.ticketCode || '—'}</td>
                        <td>{refund.customerName || '—'}</td>
                        <td>{refund.customerPhone || '—'}</td>
                        <td className="text-primary fw-semibold">{formatCurrency(refund.amount)}</td>
                        <td>{formatRefundMethod(refund.refundMethod)}</td>
                        <td>{formatRefundStatus(refund.status)}</td>
                        <td>{truncateText(refund.reason)}</td>
                    </tr>
                ))}
            </tbody>
        </Table>
    );
}
