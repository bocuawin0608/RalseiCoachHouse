import { useState, useEffect } from 'react';
import { Modal, Row, Col, Badge, Spinner, Table } from 'react-bootstrap';
import accountApi from '../api/accountApi';

export default function AccountDetailModal({ isOpen, data, onClose }) {
    const [detail, setDetail] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen && data) {
            setLoading(true);
            accountApi.getAccountDetail(data.accountId)
                .then(res => setDetail(res))
                .catch(() => {})
                .finally(() => setLoading(false));
        }
    }, [isOpen, data]);

    return (
        <Modal show={isOpen} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>Chi tiết tài khoản</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {loading ? (
                    <div className="text-center py-4"><Spinner animation="border" /></div>
                ) : detail ? (
                    <>
                        <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin tài khoản</h6>
                        <Row className="mb-3">
                            <Col md={4}><strong>Username:</strong> {detail.username}</Col>
                            <Col md={4}><strong>Loại:</strong> {detail.authProvider}</Col>
                            <Col md={4}>
                                <strong>Trạng thái:</strong>{' '}
                                <Badge bg={detail.active !== false ? 'success' : 'secondary'}>
                                    {detail.active !== false ? 'Hoạt động' : 'Đã khóa'}
                                </Badge>
                            </Col>
                            <Col md={4} className="mt-2"><strong>Lần cuối:</strong> {detail.lastLogin ? new Date(detail.lastLogin).toLocaleString('vi-VN') : '—'}</Col>
                            <Col md={4} className="mt-2"><strong>Ngày tạo:</strong> {detail.createdAt ? new Date(detail.createdAt).toLocaleString('vi-VN') : '—'}</Col>
                        </Row>

                        <h6 className="fw-bold text-secondary border-bottom pb-2">Vai trò</h6>
                        <div className="mb-3">
                            {detail.roles && detail.roles.length > 0
                                ? detail.roles.map(r => <Badge key={r.roleId} bg="primary" className="me-1">{r.roleName}</Badge>)
                                : <span className="text-muted">Chưa gán</span>
                            }
                        </div>

                        {detail.staff && (
                            <>
                                <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin nhân sự</h6>
                                <Table size="sm" borderless>
                                    <tbody>
                                        <tr><td className="fw-medium" style={{width: '140px'}}>Họ tên</td><td>{detail.staff.staffName}</td></tr>
                                        <tr><td className="fw-medium">SĐT</td><td>{detail.staff.phone}</td></tr>
                                        <tr><td className="fw-medium">Email</td><td>{detail.staff.email || '—'}</td></tr>
                                        <tr><td className="fw-medium">CCCD</td><td>{detail.staff.cccd || '—'}</td></tr>
                                        <tr><td className="fw-medium">Ngày sinh</td><td>{detail.staff.dob || '—'}</td></tr>
                                        <tr><td className="fw-medium">Chức vụ</td><td>{detail.staff.staffPosition}</td></tr>
                                        <tr><td className="fw-medium">ID bến xe</td><td>{detail.staff.ticketAgencyId || '—'}</td></tr>
                                        <tr><td className="fw-medium">Ngày vào làm</td><td>{detail.staff.hireDate || '—'}</td></tr>
                                    </tbody>
                                </Table>
                            </>
                        )}
                    </>
                ) : (
                    <p className="text-muted">Không thể tải thông tin tài khoản.</p>
                )}
            </Modal.Body>
        </Modal>
    );
}
