import { useCallback, useEffect, useMemo, useState } from "react"
import { coachTypeApi } from "../api/coachTypeApi";
import { Alert, Badge, Button, Col, Modal, Row, Spinner } from "react-bootstrap";
import SeatMapBuilder from "./SeatMapBuilder";
import { formatCurrency } from "../../../utils/formatters";
import { BsExclamationTriangleFill } from "react-icons/bs";

const INITIAL_DETAIL = {
    coachTypeId: '',
    coachTypeName: '',
    totalSeat: '',
    currentPrice: '',
    isActive: '',
    seatLayout: ''
}

export default function CoachTypeViewDetailModal({isOpen, data, onClose}) {
    const [detail, setDetail] = useState(INITIAL_DETAIL);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchCoachTypeDetail = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            if(isOpen && data) {
                const res = await coachTypeApi.getCoachTypeDetail(data.coachTypeId)
                setDetail({
                    coachTypeId: res.coachTypeId,
                    coachTypeName: res.coachTypeName,
                    totalSeat: res.totalSeat,
                    currentPrice: res.currentPrice,
                    isActive: res.isActive,
                    seatLayout: res.seatLayout
                });
            }
        } catch(error) {
            setError(error.response?.data?.message || "Có lỗi xảy ra khi lấy dữ liệu.");
        } finally {
            setLoading(false);
        }
    }, [data, isOpen])

    useEffect(() => {
        const load = () => {
            fetchCoachTypeDetail();
        }
        load();
    }, [fetchCoachTypeDetail])

    const parsedLayout = useMemo(() => {
        if (!detail.seatLayout) return null;
        try {
            return typeof detail.seatLayout === 'string'
                ? JSON.parse(detail.seatLayout)
                : detail.seatLayout;
        } catch (e) {
            console.error("Lỗi parse seatLayout:", e);
            return null;
        }
    }, [detail.seatLayout]);

    const formattedPrice = useMemo(() => {
        return formatCurrency(detail.currentPrice);
    }, [detail.currentPrice]);

    if (!isOpen) return null;

    return (
        <Modal show={isOpen} onHide={onClose} size="lg" centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Chi tiết loại xe
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4 py-4">
                {loading ? (
                    <div className="d-flex flex-column align-items-center justify-content-center py-5">
                        <Spinner animation="border" variant="primary" />
                        <span className="mt-2 text-secondary">Đang tải thông tin...</span>
                    </div>
                ) : error ? (
                    <Alert variant="danger" className="mb-3 py-4 px-4 d-flex align-items-center gap-2">
                        <BsExclamationTriangleFill />
                        <span>{error}</span>
                    </Alert>
                ) : (
                    <div className="d-flex flex-column gap-4">
                        <div>
                            <h6 className="fw-bold text-dark border-bottom pb-2 mb-3">Thông tin cơ bản</h6>
                            <Row className="gy-3">
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Mã loại xe</p>
                                    <p className="fw-medium text-dark mb-0">{detail.coachTypeId || '---'}</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Tên loại xe</p>
                                    <p className="fw-medium text-dark mb-0">{detail.coachTypeName || '---'}</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Giá vé mặc định</p>
                                    <p className="fw-bold text-danger mb-0">{formattedPrice}</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Trạng thái hoạt động</p>
                                    <Badge bg={detail.isActive ? 'success' : 'secondary'} className="px-2 py-1">
                                        {detail.isActive ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                                    </Badge>
                                </Col>
                            </Row>
                        </div>

                        <div>
                            <h6 className="fw-bold text-dark border-bottom pb-2 mb-3">Sơ đồ không gian xe</h6>
                            <div className="d-flex align-items-center gap-4 mb-3">
                                <div className="bg-light px-3 py-2 rounded border">
                                    <span className="text-secondary fw-semibold small">Tổng số ghế: </span>
                                    <span className="fw-bold text-success">{detail.totalSeat}</span>
                                </div>
                                {parsedLayout && (
                                    <div className="bg-light px-3 py-2 rounded border">
                                        <span className="text-secondary fw-semibold small">Kích thước: </span>
                                        <span className="fw-medium text-dark">{parsedLayout.rows} hàng x {parsedLayout.cols} cột</span>
                                    </div>
                                )}
                            </div>

                            <div className="bg-light p-4 rounded border d-flex justify-content-center overflow-auto" style={{ maxHeight: '450px' }}>
                                {parsedLayout && parsedLayout.matrix ? (
                                    <SeatMapBuilder 
                                        mode="VIEW"
                                        rows={parsedLayout.rows}
                                        cols={parsedLayout.cols}
                                        initialMatrix={parsedLayout.matrix}
                                    />
                                ) : (
                                    <div className="text-center py-4">
                                        <p className="text-muted mb-0 fst-italic">Không có dữ liệu sơ đồ ghế cho loại xe này</p>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                )}
            </Modal.Body>

            <Modal.Footer className="bg-light border-0 rounded-bottom">
                <Button variant="outline-secondary" onClick={onClose} className="px-4">
                    Đóng
                </Button>
            </Modal.Footer>
        </Modal>
    );
}