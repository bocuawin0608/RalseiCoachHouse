import { useState } from 'react';
import { Table, Badge, Button, Spinner } from 'react-bootstrap';
import { BsEye, BsExclamationTriangleFill, BsArrowUp, BsArrowDown } from 'react-icons/bs';

const formatCurrency = (v) => {
    if (v == null) return '—';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
};

const formatDate = (d) => {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('vi-VN');
};

export default function CustomerTable({ customers, loading, error, onViewDetail, sortBy, sortDir, onSort }) {
    const [tooltipRow, setTooltipRow] = useState(null);

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2 text-muted">Đang tải dữ liệu...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="alert alert-danger d-flex align-items-center gap-2">
                <BsExclamationTriangleFill />
                <span>{error}</span>
            </div>
        );
    }

    if (!customers || customers.length === 0) {
        return (
            <div className="text-center py-5 text-muted">
                <p>Không tìm thấy khách hàng nào.</p>
            </div>
        );
    }

    const SortIcon = ({ col }) => {
        if (sortBy !== col) return <BsArrowUp className="text-muted opacity-25 ms-1" style={{ fontSize: '0.7rem' }} />;
        return sortDir === 'asc'
            ? <BsArrowUp className="ms-1" style={{ fontSize: '0.7rem' }} />
            : <BsArrowDown className="ms-1" style={{ fontSize: '0.7rem' }} />;
    };

    const handleSort = (col) => {
        if (!onSort) return;
        if (sortBy === col) {
            onSort(col, sortDir === 'asc' ? 'desc' : 'asc');
        } else {
            onSort(col, 'desc');
        }
    };

    const thSortable = (label, col) => (
        <th role="button" onClick={() => handleSort(col)} className="user-select-none">
            {label} <SortIcon col={col} />
        </th>
    );

    return (
        <div className="table-responsive">
            <Table hover className="align-middle mb-0">
                <thead className="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>SĐT</th>
                        <th>Loại TK</th>
                        {thSortable('Số chuyến', 'totalTrips')}
                        {thSortable('Tổng chi tiêu', 'totalSpent')}
                        <th>Đặt gần nhất</th>
                        <th>Trạng thái</th>
                        <th className="text-center">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    {customers.map(c => {
                        const isActive = c.active !== false;
                        const hasAccount = c.accountId != null;
                        const trips = c.totalTrips ?? 0;
                        const neverBooked = trips === 0;
                        return (
                            <tr key={c.customerId}
                                className={!isActive ? 'table-danger' : ''}
                                onMouseEnter={() => setTooltipRow(c.customerId)}
                                onMouseLeave={() => setTooltipRow(null)}
                                style={{ position: 'relative' }}>
                                <td>{c.customerId}</td>
                                <td className="fw-medium">
                                    {c.customerName}
                                    {neverBooked && <Badge bg="dark" className="ms-1" style={{ fontSize: '0.65rem' }}>Chưa đặt vé</Badge>}
                                </td>
                                <td>{c.phone || <span className="text-muted">—</span>}</td>
                                <td>
                                    <Badge bg={hasAccount ? 'primary' : 'warning'} text={hasAccount ? 'white' : 'dark'} style={{ fontSize: '0.75rem' }}>
                                        {hasAccount ? 'Đã đăng ký' : 'CRM'}
                                    </Badge>
                                </td>
                                <td>{trips}</td>
                                <td>{formatCurrency(c.totalSpent)}</td>
                                <td style={{ fontSize: '0.85rem' }}>{formatDate(c.lastBooking)}</td>
                                <td>
                                    <Badge bg={isActive ? 'success' : 'danger'}>
                                        {isActive ? 'Hoạt động' : 'Đã khóa'}
                                    </Badge>
                                </td>
                                <td className="text-center">
                                    <Button variant="outline-primary" size="sm" title="Xem chi tiết" onClick={() => onViewDetail(c)}>
                                        <BsEye />
                                    </Button>
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </Table>
        </div>
    );
}