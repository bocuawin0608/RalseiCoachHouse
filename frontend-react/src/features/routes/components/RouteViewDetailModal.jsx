import { useCallback, useEffect, useState } from "react"
import { routeApi } from "../api/routeApi";
import { coachStopApi } from "../../coachStops/api/coachStopApi";
import { Alert, Badge, Button, Col, Modal, Row, Spinner, Table } from "react-bootstrap";
import { BsExclamationTriangleFill } from "react-icons/bs";
import { MdDangerous } from "react-icons/md";

const INITIAL_DETAIL = {
    routeId: '',
    routeName: '',
    totalKilometers: '',
    totalMinutes: '',
    active: '',
    routeStops: []
}

export default function RouteViewDetailModal({ isOpen, data, onClose }) {
    const [detail, setDetail] = useState(INITIAL_DETAIL);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchRouteDetail = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            if (isOpen && data) {
                const res = await routeApi.getRouteDetail(data.routeId)
                const routeStops = res.routeStops || [];

                const stopsWithActiveStatus = await Promise.all(
                    routeStops.map(async (stop) => {
                        try {
                            const csRes = await coachStopApi.getCoachStopById(stop.stopPointId);
                            // Set the property we use in the render logic
                            return { ...stop, stopPointActive: csRes.active !== undefined ? csRes.active : true };
                        } catch (e) {
                            return { ...stop, stopPointActive: true };
                        }
                    })
                );

                setDetail({
                    routeId: res.routeId,
                    routeName: res.routeName,
                    totalKilometers: res.totalKilometers,
                    totalMinutes: res.totalMinutes,
                    active: res.active !== undefined ? res.active : res.active, // Handle API field name
                    routeStops: stopsWithActiveStatus
                });
            }
        } catch (error) {
            setError(error.response?.data?.message || "Có lỗi xảy ra khi lấy dữ liệu.");
        } finally {
            setLoading(false);
        }
    }, [data, isOpen])

    useEffect(() => {
        const load = () => {
            fetchRouteDetail();
        }
        load();
    }, [fetchRouteDetail])

    if (!isOpen) return null;

    return (
        <Modal show={isOpen} onHide={onClose} size="lg" centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Chi tiết tuyến đường
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
                                    <p className="text-secondary fw-semibold mb-1 small">Mã tuyến đường</p>
                                    <p className="fw-medium text-dark mb-0">#{detail.routeId || '---'}</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Tên tuyến đường</p>
                                    <p className="fw-medium text-dark mb-0">{detail.routeName || '---'}</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Khoảng cách tổng</p>
                                    <p className="fw-bold text-dark mb-0">{detail.totalKilometers} km</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Thời gian tổng</p>
                                    <p className="fw-bold text-dark mb-0">{detail.totalMinutes} phút</p>
                                </Col>
                                <Col sm={6}>
                                    <p className="text-secondary fw-semibold mb-1 small">Trạng thái hoạt động</p>
                                    <Badge bg={detail.active ? 'success' : 'secondary'} className="px-2 py-1">
                                        {detail.active ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                                    </Badge>
                                </Col>
                            </Row>
                        </div>

                        <div>
                            <h6 className="fw-bold text-dark border-bottom pb-2 mb-3">Các trạm dừng (Route Stops)</h6>
                            {detail.routeStops && detail.routeStops.length > 0 ? (
                                <div className="table-responsive">
                                    <Table size="sm" bordered hover className="align-middle text-center mb-0">
                                        <thead className="table-light text-secondary">
                                            <tr>
                                                <th className="fw-semibold">Thứ tự</th>
                                                <th className="fw-semibold text-start">Tên trạm</th>
                                                <th className="fw-semibold">Khoảng cách từ điểm xuất phát</th>
                                                <th className="fw-semibold">Thời gian từ điểm xuất phát</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {[...detail.routeStops]
                                                .sort((a, b) => a.stopOrder - b.stopOrder)
                                                .map(stop => (
                                                    <tr key={stop.routeStopId} className={!stop.stopPointActive ? 'table-danger' : ''}>
                                                        <td className={`fw-bold ${!stop.stopPointActive ? 'text-danger' : 'text-primary'}`}>
                                                            {stop.stopOrder}
                                                            {!stop.stopPointActive && (
                                                                <MdDangerous size={18} className="ms-2 text-danger" title="Ngừng HĐ" />
                                                            )}
                                                        </td>
                                                        <td className="text-start fw-medium"> {stop.stopPointName}</td>
                                                        <td>{stop.kilometersFromStart} km</td>
                                                        <td>{stop.minutesFromStart} phút</td>
                                                    </tr>
                                                ))}
                                        </tbody>
                                    </Table>
                                </div>
                            ) : (
                                <div className="bg-light p-4 rounded border text-center">
                                    <p className="text-muted mb-0 fst-italic">Tuyến đường này chưa được cấu hình trạm dừng nào.</p>
                                </div>
                            )}
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
