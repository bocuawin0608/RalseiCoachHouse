import { useState, useEffect } from 'react';
import { Modal, Row, Col, Badge, Spinner, Table, ListGroup } from 'react-bootstrap';
import ticketAgencyApi from '../api/ticketAgencyApi';

export default function TicketAgencyDetailModal({ isOpen, data, onClose }) {
    const [detail, setDetail] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen && data) {
            setLoading(true);
            ticketAgencyApi.getDetail(data.ticketAgencyId)
                .then(res => setDetail(res))
                .catch(() => {})
                .finally(() => setLoading(false));
        }
    }, [isOpen, data]);

    return (
        <Modal show={isOpen} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton><Modal.Title>Chi tiết đại lý</Modal.Title></Modal.Header>
            <Modal.Body>
                {loading ? (
                    <div className="text-center py-4"><Spinner animation="border" /></div>
                ) : detail ? (
                    <>
                        <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin đại lý</h6>
                        <Row className="mb-3">
                            <Col md={6}><strong>ID:</strong> {detail.ticketAgencyId}</Col>
                            <Col md={6}><strong>Trạng thái:</strong>{' '}
                                <Badge bg={detail.active !== false ? 'success' : 'danger'}>{detail.active !== false ? 'Hoạt động' : 'Đã khóa'}</Badge>
                            </Col>
                            <Col md={12} className="mt-2"><strong>Tên:</strong> {detail.ticketAgencyName}</Col>
                            <Col md={6} className="mt-2"><strong>Điểm dừng:</strong> {detail.stopPointName || '—'}</Col>
                            <Col md={6} className="mt-2"><strong>Thành phố:</strong> {detail.city || '—'}</Col>
                            <Col md={12} className="mt-2"><strong>Địa chỉ:</strong> {detail.address || '—'}</Col>
                        </Row>

                        <h6 className="fw-bold text-secondary border-bottom pb-2">Nhân viên đang làm việc ({detail.staffCount ?? 0})</h6>
                        {detail.staffList && detail.staffList.length > 0 ? (
                            <ListGroup variant="flush" className="mb-3">
                                {detail.staffList.map(s => (
                                    <ListGroup.Item key={s.staffId} className="d-flex justify-content-between align-items-center py-1">
                                        <span>{s.staffName}</span>
                                        <Badge bg="info">{s.staffPosition}</Badge>
                                    </ListGroup.Item>
                                ))}
                            </ListGroup>
                        ) : (
                            <p className="text-muted mb-3">Không có nhân viên nào.</p>
                        )}

                        <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin hệ thống</h6>
                        <Table size="sm" borderless>
                            <tbody>
                                <tr><td className="fw-medium" style={{width:'140px'}}>Ngày tạo</td><td>{detail.createdAt ? new Date(detail.createdAt).toLocaleString('vi-VN') : '—'}</td></tr>
                                <tr><td className="fw-medium">Người tạo</td><td>{detail.createdBy ?? '—'}</td></tr>
                                <tr><td className="fw-medium">Cập nhật</td><td>{detail.updatedAt ? new Date(detail.updatedAt).toLocaleString('vi-VN') : '—'}</td></tr>
                                <tr><td className="fw-medium">Người cập nhật</td><td>{detail.updatedBy ?? '—'}</td></tr>
                            </tbody>
                        </Table>
                    </>
                ) : (
                    <p className="text-muted">Không thể tải thông tin.</p>
                )}
            </Modal.Body>
        </Modal>
    );
}
