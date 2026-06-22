import { useEffect, useMemo, useState } from 'react'
import { coachApi } from '../api/coachApi';
import { Alert, Badge, Button, Col, Modal, Row, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import SeatMapBuilder from './SeatMapBuilder';
const INITIAL_FORM = {
    coachId: '',
    routeName: '',
    coachTypeName: '',
    licensePlate: '',
    manufacturer: '',
    year: '',
    status: '',
    totalActiveSeats: '',
    seats: []
}

export default function CoachViewDetailModal({isOpen, data, onClose, statusLabels}) {
    const [detail, setDetail] = useState(INITIAL_FORM);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDetail = async () => {
            setLoading(true);
            setError(null);
            
            try {
                if(data && isOpen) {
                    const res = await coachApi.getCoachDetailForView(data.coachId);
                    setDetail({
                        coachId: res.coachId,
                        routeName: res.routeName,
                        coachTypeName: res.coachTypeName,
                        licensePlate: res.licensePlate,
                        manufacturer: res.manufacturer,
                        year: res.year,
                        status: res.status,
                        totalActiveSeats: res.totalActiveSeats,
                        seats: res.seats
                    })
                }
            } catch(error) {
                setError(error?.response?.data?.message || "Có lỗi xảy ra khi lấy dữ liệu chi tiết.");
            } finally {
                setLoading(false);
            }
        }

        fetchDetail();
    }, [data, isOpen]);

    const parsedLayout = useMemo(() => {
        if(!detail.seats || detail.seats.length == 0) {
            return {floors: [], cols: 0, rows:0};
        }

        let maxFloor = 0;
        let maxRow = 0;
        let maxCol = 0;

        detail.seats.forEach(seat => {
            if(seat.floorIndex > maxFloor) maxFloor = seat.floorIndex;
            if(seat.rowIndex > maxRow) maxRow = seat.rowIndex;
            if(seat.colIndex > maxCol) maxCol = seat.colIndex;
        })

        const floorMatrix = Array(maxFloor).fill().map(() => Array(maxRow).fill().map(() => Array(maxCol).fill(null)));
        detail.seats.forEach(seat => {
            floorMatrix[seat.floorIndex-1][seat.rowIndex-1][seat.colIndex-1] = seat;
        })

        return {
            floors: floorMatrix,
            rows: maxRow,
            cols: maxCol
        };
    }, [detail.seats]);

    if(!isOpen) return;

    return (
        <Modal show={isOpen} onHide={onClose} size='lg' centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Xem chi tiết xe
                </Modal.Title>
            </Modal.Header>

            <Modal.Body>
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
                                    <p className="text-secondary fw-semibold mb-1 small">Biển số xe</p>
                                    <p className="fw-bold text-dark mb-0">{detail.licensePlate || '---'}</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Loại xe</p>
                                    <p className="fw-medium text-dark mb-0">{detail.coachTypeName || '---'}</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Tuyến xe</p>
                                    <p className="fw-medium text-dark mb-0">{detail.routeName || '---'}</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Hãng xe</p>
                                    <p className="fw-medium text-dark mb-0">{detail.manufacturer || '---'}</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Năm sản xuất</p>
                                    <p className="fw-medium text-dark mb-0">{detail.year || '---'}</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Trạng thái hoạt động</p>
                                    <Badge bg={statusLabels[detail.status]?.bg || 'secondary'} className="px-2 py-1">
                                        {statusLabels[detail.status]?.text || 'N/A'}
                                    </Badge>
                                </Col>
                            </Row>
                        </div>

                        <div>
                            <h6 className="fw-bold text-dark border-bottom pb-2 mb-3">Sơ đồ không gian xe</h6>
                            <div className="d-flex align-items-center justify-content-between gap-4 mb-3">
                                <div className="bg-light px-3 py-2 rounded border">
                                    <span className="text-secondary fw-semibold small">Tổng số ghế khả dụng: </span>
                                    <span className="fw-bold text-success">{detail.totalActiveSeats}</span>
                                </div>
                                <div className="bg-light px-3 py-2 rounded border">
                                    <span className="text-secondary fw-semibold small">Chú thích: Ghế khả dụng <span className='text-success'>màu xanh</span>, ghế không hoạt động <span className='text-danger'>màu đỏ</span>.</span>
                                </div>
                            </div>

                            <div className="bg-light p-4 rounded border d-flex gap-2 justify-content-center overflow-auto" style={{ maxHeight: '450px' }}>
                                {parsedLayout && parsedLayout.floors ? (parsedLayout.floors.map((_, index) => (
                                    <div key={index} className="floor-wrapper text-center">
                                        <p className="mb-3 fw-medium">Tầng {index+1}</p>
                                        <div className="border border-secondary rounded p-3 bg-light shadow-sm">
                                            <SeatMapBuilder 
                                                mode="VIEW-SEAT"
                                                rows={parsedLayout.rows}
                                                cols={parsedLayout.cols}
                                                initialMatrix={parsedLayout.floors[index]}
                                            />
                                        </div>
                                    </div>
                                ))) : (
                                    <div className="text-center py-4">
                                        <p className="text-muted mb-0 fst-italic">Không có dữ liệu ghế cho xe này.</p>
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
    )
}