import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Spinner, Modal, Table } from 'react-bootstrap';
import { BsBoxSeam, BsEye, BsExclamationTriangleFill } from 'react-icons/bs';
import { cargoTrackingApi } from '../../features/cargo/api/cargoTrackingApi';

const statusLabels = {
    RECEIVED: 'Đã nhận hàng', LOADED: 'Đã lên xe', ARRIVED: 'Đã đến nơi',
    DELIVERED: 'Đã giao', CANCELLED: 'Đã hủy', REJECTED: 'Từ chối', ABANDONED: 'Bỏ hàng',
};

const statusBadgeVariant = {
    RECEIVED: 'info', LOADED: 'primary', ARRIVED: 'warning',
    DELIVERED: 'success', CANCELLED: 'danger', REJECTED: 'danger', ABANDONED: 'dark',
};

const statusGroups = [
    { key: '', label: 'Tất cả' },
    { key: 'ONGOING', label: 'Đang vận chuyển' },
    { key: 'COMPLETED', label: 'Hoàn thành' },
    { key: 'CANCELLED', label: 'Đã hủy' },
];

const timelineSteps = [
    { key: 'RECEIVED', label: 'Đã nhận hàng' },
    { key: 'LOADED', label: 'Đã lên xe' },
    { key: 'ARRIVED', label: 'Đã đến nơi' },
    { key: 'DELIVERED', label: 'Đã giao' },
];

function StatusTimeline({ status }) {
    if (status === 'CANCELLED' || status === 'REJECTED' || status === 'ABANDONED') {
        return (
            <div className="text-center py-3">
                <Badge bg="danger" className="py-2 px-3 fs-6">{statusLabels[status]}</Badge>
            </div>
        );
    }
    const currentIdx = timelineSteps.findIndex(s => s.key === status);
    return (
        <div className="d-flex justify-content-center gap-2 my-3 flex-wrap">
            {timelineSteps.map((step, i) => (
                <div key={step.key} className={`d-flex align-items-center gap-1 ${i <= currentIdx ? '' : 'opacity-25'}`}>
                    <div className={`rounded-circle d-flex align-items-center justify-content-center fw-bold text-white ${i <= currentIdx ? 'bg-success' : 'bg-secondary'}`}
                        style={{ width: 32, height: 32, fontSize: 14 }}>
                        {i <= currentIdx ? '✓' : i + 1}
                    </div>
                    <small className={i <= currentIdx ? 'fw-medium' : ''}>{step.label}</small>
                    {i < timelineSteps.length - 1 && <span className="mx-1 text-muted" style={{ fontSize: 10 }}>—</span>}
                </div>
            ))}
        </div>
    );
}

export default function CargoHistoryPage() {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [statusFilter, setStatusFilter] = useState('');
    const [detailModal, setDetailModal] = useState({ show: false, data: null, loading: false });

    const fetchList = () => {
        setLoading(true);
        setError(null);
        cargoTrackingApi.getMyCargo({ status: statusFilter || undefined })
            .then(res => setItems(res || []))
            .catch(err => setError(err.response?.data?.message || 'Có lỗi khi tải danh sách.'))
            .finally(() => setLoading(false));
    };

    useEffect(() => { fetchList(); }, [statusFilter]);

    const openDetail = (item) => {
        setDetailModal({ show: true, data: null, loading: true });
        cargoTrackingApi.getMyCargoDetail(item.cargoTicketId)
            .then(res => setDetailModal({ show: true, data: res, loading: false }))
            .catch(() => setDetailModal({ show: true, data: null, loading: false }));
    };

    return (
        <Container className="py-4">
            <h4 className="fw-bold mb-3 d-flex align-items-center gap-2">
                <BsBoxSeam /> Lịch sử vận đơn hàng hóa
            </h4>

            <div className="d-flex gap-2 mb-3 flex-wrap">
                {statusGroups.map(g => (
                    <Button key={g.key} variant={statusFilter === g.key ? 'dark' : 'outline-dark'} size="sm"
                        onClick={() => setStatusFilter(g.key)} className="rounded-pill">
                        {g.label}
                    </Button>
                ))}
            </div>

            {loading && (
                <div className="text-center py-5"><Spinner animation="border" variant="dark" /><p className="mt-2 text-muted">Đang tải...</p></div>
            )}
            {error && (
                <div className="alert alert-danger d-flex align-items-center gap-2"><BsExclamationTriangleFill /><span>{error}</span></div>
            )}
            {!loading && !error && items.length === 0 && (
                <p className="text-muted text-center py-5">Không có vận đơn nào.</p>
            )}
            {!loading && !error && items.length > 0 && (
                <Row className="g-3">
                    {items.map(item => {
                        return (
                            <Col md={6} lg={4} key={item.cargoTicketId}>
                                <Card className="shadow-sm h-100">
                                    <Card.Body>
                                        <div className="d-flex justify-content-between align-items-start mb-2">
                                            <Badge bg={statusBadgeVariant[item.status] || 'secondary'}>
                                                {statusLabels[item.status] || item.status}
                                            </Badge>
                                            <small className="text-muted">
                                                {item.createdAt ? new Date(item.createdAt).toLocaleDateString('vi-VN') : ''}
                                            </small>
                                        </div>
                                        <p className="fw-bold mb-1" style={{ fontSize: '1.1rem' }}>{item.ticketCode}</p>
                                        <small className="text-muted d-block">{item.tripRouteName || 'N/A'}</small>
                                        <hr className="my-2" />
                                        <div className="small">
                                            <div><strong>Gửi:</strong> {item.senderName} — {item.senderPhone}</div>
                                            <div><strong>Nhận:</strong> {item.receiverName} — {item.receiverPhone}</div>
                                            <div className="mt-1"><strong>Nơi lấy:</strong> {item.pickupStopName}</div>
                                            <div><strong>Nơi trả:</strong> {item.dropoffStopName}</div>
                                            <div className="fw-bold mt-1">{item.totalPrice?.toLocaleString('vi-VN')} đ</div>
                                        </div>
                                    </Card.Body>
                                    <Card.Footer className="bg-white border-top-0 text-end">
                                        <Button variant="outline-dark" size="sm" onClick={() => openDetail(item)}>
                                            <BsEye className="me-1" />Xem chi tiết
                                        </Button>
                                    </Card.Footer>
                                </Card>
                            </Col>
                        );
                    })}
                </Row>
            )}

            <Modal show={detailModal.show} onHide={() => setDetailModal({ show: false, data: null, loading: false })} centered size="lg">
                <Modal.Header closeButton><Modal.Title>Chi tiết vận đơn</Modal.Title></Modal.Header>
                <Modal.Body>
                    {detailModal.loading ? (
                        <div className="text-center py-4"><Spinner animation="border" /></div>
                    ) : detailModal.data ? (
                        <>
                            <StatusTimeline status={detailModal.data.status} />
                            <h6 className="fw-bold text-secondary border-bottom pb-2 mt-3">Thông tin đơn hàng</h6>
                            <Row className="mb-2">
                                <Col md={6}><strong>Mã vận đơn:</strong> {detailModal.data.ticketCode}</Col>
                                <Col md={6}><strong>Trạng thái:</strong> <Badge bg={statusBadgeVariant[detailModal.data.status]}>{statusLabels[detailModal.data.status]}</Badge></Col>
                                <Col md={6} className="mt-2"><strong>Tổng cước:</strong> {detailModal.data.totalPrice?.toLocaleString('vi-VN')} đ</Col>
                                <Col md={6} className="mt-2"><strong>Người trả cước:</strong> {detailModal.data.feePayer === 'SENDER' ? 'Người gửi' : 'Người nhận'}</Col>
                                {detailModal.data.codAmount > 0 && (
                                    <Col md={6} className="mt-2"><strong>COD:</strong> {detailModal.data.codAmount?.toLocaleString('vi-VN')} đ</Col>
                                )}
                                {detailModal.data.description && (
                                    <Col md={12} className="mt-2"><strong>Mô tả:</strong> {detailModal.data.description}</Col>
                                )}
                            </Row>
                            <h6 className="fw-bold text-secondary border-bottom pb-2 mt-2">Người gửi / Nhận</h6>
                            <Row className="mb-2">
                                <Col md={6}><strong>Người gửi:</strong> {detailModal.data.senderName} — {detailModal.data.senderPhone}</Col>
                                <Col md={6}><strong>Người nhận:</strong> {detailModal.data.receiverName} — {detailModal.data.receiverPhone}</Col>
                            </Row>
                            <h6 className="fw-bold text-secondary border-bottom pb-2 mt-2">Điểm giao nhận</h6>
                            <Row className="mb-2">
                                <Col md={6}><strong>Nơi lấy:</strong> {detailModal.data.pickupStopName}</Col>
                                <Col md={6}><strong>Nơi trả:</strong> {detailModal.data.dropoffStopName}</Col>
                                {detailModal.data.tripRouteName && (
                                    <Col md={6} className="mt-2"><strong>Tuyến:</strong> {detailModal.data.tripRouteName}</Col>
                                )}
                                {detailModal.data.tripDepartureTime && (
                                    <Col md={6} className="mt-2"><strong>Khởi hành:</strong> {new Date(detailModal.data.tripDepartureTime).toLocaleString('vi-VN')}</Col>
                                )}
                            </Row>
                            {detailModal.data.items?.length > 0 && (
                                <>
                                    <h6 className="fw-bold text-secondary border-bottom pb-2 mt-2">Chi tiết hàng hóa</h6>
                                    <Table size="sm" bordered className="mb-0">
                                        <thead className="table-light">
                                            <tr>
                                                <th>Mô tả</th>
                                                <th>SL</th>
                                                <th>Kg</th>
                                                <th>Khối</th>
                                                <th>ĐVT</th>
                                                <th>Thành tiền</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {detailModal.data.items.map((it, i) => (
                                                <tr key={i}>
                                                    <td>{it.description || 'N/A'}</td>
                                                    <td>{it.quantity}</td>
                                                    <td>{it.weightKg}</td>
                                                    <td>{it.dimensionVol}</td>
                                                    <td>{it.unit || 'N/A'}</td>
                                                    <td>{it.calculatedPrice?.toLocaleString('vi-VN')} đ</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </Table>
                                </>
                            )}
                        </>
                    ) : (
                        <p className="text-muted">Không thể tải thông tin.</p>
                    )}
                </Modal.Body>
            </Modal>
        </Container>
    );
}
