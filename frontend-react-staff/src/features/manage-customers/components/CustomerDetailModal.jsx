import { useState, useEffect } from 'react';
import { Modal, Row, Col, Badge, Spinner, Table } from 'react-bootstrap';
import customerApi from '../api/customerApi';

const formatCurrency = (v) => {
    if (v == null) return '—';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
};

const statusBadge = (status) => {
    const map = {
        'CONFIRMED': 'primary',
        'PAID': 'success',
        'CANCELLED': 'danger',
        'REFUNDED': 'warning',
        'PENDING': 'secondary',
    };
    return <Badge bg={map[status] || 'light'}>{status || '—'}</Badge>;
};

export default function CustomerDetailModal({ isOpen, data, onClose }) {
    const [detail, setDetail] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen && data) {
            setLoading(true);
            customerApi.getCustomerDetail(data.customerId)
                .then(res => setDetail(res))
                .catch(() => {})
                .finally(() => setLoading(false));
        }
    }, [isOpen, data]);

    return (
        <Modal show={isOpen} onHide={onClose} centered size="lg">
            <Modal.Header closeButton>
                <Modal.Title>Chi tiết khách hàng</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {loading ? (
                    <div className="text-center py-4"><Spinner animation="border" /></div>
                ) : detail ? (
                    <>
                        <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin khách hàng</h6>
                        <Row className="mb-3">
                            <Col md={4}><strong>ID:</strong> {detail.customerId}</Col>
                            <Col md={4}>
                                <strong>Trạng thái:</strong>{' '}
                                <Badge bg={detail.active !== false ? 'success' : 'danger'}>
                                    {detail.active !== false ? 'Hoạt động' : 'Đã khóa'}
                                </Badge>
                            </Col>
                            <Col md={4}>
                                <strong>Loại TK:</strong>{' '}
                                <Badge bg={detail.accountId ? 'primary' : 'secondary'}>
                                    {detail.accountId ? 'Đã đăng ký' : 'CRM'}
                                </Badge>
                            </Col>
                            <Col md={6} className="mt-2"><strong>Họ tên:</strong> {detail.customerName}</Col>
                            <Col md={6} className="mt-2"><strong>SĐT:</strong> {detail.phone || '—'}</Col>
                            <Col md={6} className="mt-2"><strong>Email:</strong> {detail.email || '—'}</Col>
                            <Col md={6} className="mt-2"><strong>Ngày sinh:</strong> {detail.dob || '—'}</Col>
                        </Row>
                        <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin hoạt động</h6>
                        <Row className="mb-3">
                            <Col md={4}><strong>Số chuyến:</strong> {detail.totalTrips ?? 0}</Col>
                            <Col md={4}><strong>Tổng chi tiêu:</strong> {formatCurrency(detail.totalSpent)}</Col>
                            <Col md={4}><strong>Đặt gần nhất:</strong> {detail.lastBooking ? new Date(detail.lastBooking).toLocaleDateString('vi-VN') : '—'}</Col>
                        </Row>
                        <h6 className="fw-bold text-secondary border-bottom pb-2">Lịch sử đặt vé</h6>
                        {detail.bookings && detail.bookings.length > 0 ? (
                            <div style={{ maxHeight: '240px', overflowY: 'auto' }}>
                            <Table size="sm" hover>
                                <thead>
                                    <tr>
                                        <th>Mã vé</th>
                                        <th>Ngày đặt</th>
                                        <th>Tổng tiền</th>
                                        <th>Trạng thái</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {detail.bookings.map(b => (
                                        <tr key={b.passengerTicketId}>
                                            <td className="fw-medium">{b.ticketCode}</td>
                                            <td>{b.createdAt ? new Date(b.createdAt).toLocaleString('vi-VN') : '—'}</td>
                                            <td>{formatCurrency(b.totalPrice)}</td>
                                            <td>{statusBadge(b.status)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                            </div>
                        ) : (
                            <p className="text-muted">Khách hàng chưa có vé đặt nào.</p>
                        )}
                        <h6 className="fw-bold text-secondary border-bottom pb-2 mt-3">Thông tin hệ thống</h6>
                        <Table size="sm" borderless>
                            <tbody>
                                <tr><td className="fw-medium" style={{width: '140px'}}>Ngày tạo</td><td>{detail.createdAt ? new Date(detail.createdAt).toLocaleString('vi-VN') : '—'}</td></tr>
                                <tr><td className="fw-medium">Người tạo</td><td>{detail.createdBy ?? '—'}</td></tr>
                                <tr><td className="fw-medium">Cập nhật</td><td>{detail.updatedAt ? new Date(detail.updatedAt).toLocaleString('vi-VN') : '—'}</td></tr>
                                <tr><td className="fw-medium">Người cập nhật</td><td>{detail.updatedBy ?? '—'}</td></tr>
                            </tbody>
                        </Table>
                    </>
                ) : (
                    <p className="text-muted">Không thể tải thông tin khách hàng.</p>
                )}
            </Modal.Body>
        </Modal>
    );
}