import { useCallback, useEffect, useState } from "react";
import { coachStopApi } from "../api/coachStopApi";
import { routeApi } from "../../routes/api/routeApi";
import axiosClient from "../../../api/axiosClient";
import { Alert, Badge, Button, Col, Modal, Row, Spinner, Table } from "react-bootstrap";
import { BsExclamationTriangleFill } from "react-icons/bs";
import { MdDangerous } from "react-icons/md";

const INITIAL_DETAIL = {
    stopPointId: '',
    stopPointName: '',
    address: '',
    city: '',
    active: '',
    routes: []
};

export default function CoachStopViewDetailModal({ isOpen, data, onClose }) {
    const [detail, setDetail] = useState(INITIAL_DETAIL);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchDetailAndRoutes = useCallback(async () => {
        if (!isOpen || !data) return;

        setLoading(true);
        setError(null);
        try {
            // Fetch basic coach stop detail
            const stopRes = await coachStopApi.getCoachStopById(data.stopPointId);

            // Fetch associated route stops (routes passing through this coach stop)
            const routeStopsRes = await axiosClient.get('/v1/route-stops', {
                params: {
                    stopPointId: data.stopPointId,
                    size: 100 // Fetch up to 100 routes
                }
            });

            const routeStops = routeStopsRes.content || [];

            const routesWithActiveStatus = await Promise.all(
                routeStops.map(async (route) => {
                    try {
                        const rRes = await routeApi.getRouteDetail(route.routeId);
                        return { ...route, routeActive: rRes.active !== undefined ? rRes.active : true };
                    } catch (e) {
                        return { ...route, routeActive: true };
                    }
                })
            );

            setDetail({
                stopPointId: stopRes.stopPointId,
                stopPointName: stopRes.stopPointName,
                address: stopRes.address,
                city: stopRes.city,
                active: stopRes.active !== undefined ? stopRes.active : stopRes.isActive,
                routes: routesWithActiveStatus
            });
        } catch (error) {
            setError(error.response?.data?.message || "Có lỗi xảy ra khi lấy dữ liệu điểm dừng.");
        } finally {
            setLoading(false);
        }
    }, [data, isOpen]);

    useEffect(() => {
        fetchDetailAndRoutes();
    }, [fetchDetailAndRoutes]);

    if (!isOpen) return null;

    return (
        <Modal show={isOpen} onHide={onClose} size="lg" centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Chi tiết Điểm Dừng
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
                            <h6 className="fw-bold text-dark border-bottom pb-2 mb-3">Các tuyến đường đi qua điểm này</h6>
                            {detail.routes && detail.routes.length > 0 ? (
                                <div className="table-responsive">
                                    <Table size="sm" bordered hover className="align-middle text-center mb-0">
                                        <thead className="table-light text-secondary">
                                            <tr>
                                                <th className="fw-semibold">Mã Tuyến</th>
                                                <th className="fw-semibold text-start">Tên Tuyến Đường</th>
                                                <th className="fw-semibold">Thứ tự tại điểm dừng</th>
                                                <th className="fw-semibold">Cách điểm xuất phát (km)</th>
                                                <th className="fw-semibold">Cách điểm xuất phát (phút)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {detail.routes.map(route => (
                                                <tr key={route.routeStopId} className={!route.routeActive ? 'table-danger' : ''}>
                                                    <td className={`fw-bold ${!route.routeActive ? 'text-danger' : 'text-primary'}`}>#{route.routeId}</td>
                                                    <td className="text-start fw-medium">
                                                        {route.routeName}
                                                        {!route.routeActive && (
                                                            <MdDangerous size={18} className="ms-2 text-danger" title="Ngừng HĐ" />
                                                        )}
                                                    </td>
                                                    <td>{route.stopOrder}</td>
                                                    <td>{route.kilometersFromStart} km</td>
                                                    <td>{route.minutesFromStart} phút</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </Table>
                                </div>
                            ) : (
                                <div className="bg-light p-4 rounded border text-center">
                                    <p className="text-muted mb-0 fst-italic">Không có tuyến đường nào đi qua điểm dừng này.</p>
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
