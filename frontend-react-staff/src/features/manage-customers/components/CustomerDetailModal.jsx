import { useState, useEffect } from 'react';
import { Modal, Row, Col, Badge, Spinner, Table } from 'react-bootstrap';
import customerApi from '../api/customerApi';

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
        <Modal show={isOpen} onHide={onClose} centered>
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
                            <Col md={6}><strong>ID:</strong> {detail.customerId}</Col>
                            <Col md={6}>
                                <strong>Trạng thái:</strong>{' '}
                                <Badge bg={detail.active !== false ? 'success' : 'danger'}>
                                    {detail.active !== false ? 'Hoạt động' : 'Đã khóa'}
                                </Badge>
                            </Col>
                            <Col md={6} className="mt-2"><strong>Họ tên:</strong> {detail.customerName}</Col>
                            <Col md={6} className="mt-2"><strong>SĐT:</strong> {detail.phone || '—'}</Col>
                            <Col md={6} className="mt-2"><strong>Email:</strong> {detail.email || '—'}</Col>
                            <Col md={6} className="mt-2"><strong>Ngày sinh:</strong> {detail.dob || '—'}</Col>
                        </Row>
                        <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin hệ thống</h6>
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
