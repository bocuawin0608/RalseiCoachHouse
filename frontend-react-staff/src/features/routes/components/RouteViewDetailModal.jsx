import { useCallback, useEffect, useState } from "react"
import { routeApi } from "../api/routeApi";
import { coachStopApi } from "../../coachStops/api/coachStopApi";
import { routeStopApi } from "../api/routeStopApi";
import RouteStopUpdateInfoModal from "./RouteStopUpdateInfoModal";
import { Alert, Badge, Button, Col, Modal, Row, Spinner, Table } from "react-bootstrap";
import { BsExclamationTriangleFill, BsTrash, BsPencilFill } from "react-icons/bs";
import { DndContext, closestCenter, KeyboardSensor, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import { arrayMove, SortableContext, sortableKeyboardCoordinates, verticalListSortingStrategy } from '@dnd-kit/sortable';
import SortableRouteStopRow from "./SortableRouteStopRow";

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
    const [editingStop, setEditingStop] = useState(null);

    const sensors = useSensors(
        useSensor(PointerSensor),
        useSensor(KeyboardSensor, {
            coordinateGetter: sortableKeyboardCoordinates,
        })
    );

    const handleDragEnd = async (event) => {
        const { active, over } = event;
        if (!over || active.id === over.id) return;

        const oldIndex = detail.routeStops.findIndex(stop => stop.routeStopId === active.id);
        const newIndex = detail.routeStops.findIndex(stop => stop.routeStopId === over.id);

        if (oldIndex !== -1 && newIndex !== -1) {
            const newStops = arrayMove(detail.routeStops, oldIndex, newIndex);

            const updatedStops = newStops.map((stop, index) => ({
                ...stop,
                stopOrder: index + 1
            }));

            setDetail(prev => ({ ...prev, routeStops: updatedStops }));

            try {
                const payload = updatedStops.map(stop => ({
                    routeStopId: stop.routeStopId,
                    stopOrder: stop.stopOrder
                }));
                await routeStopApi.bulkUpdateOrders(payload);
                fetchRouteDetail();
            } catch (err) {
                alert(err.response?.data?.message || "Có lỗi xảy ra khi lưu thứ tự mới.");
                fetchRouteDetail();
            }
        }
    };

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

                stopsWithActiveStatus.sort((a, b) => a.stopOrder - b.stopOrder);

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

    const handleDeleteRouteStop = async (routeStopId) => {
        if (!window.confirm("Bạn có chắc muốn xóa điểm dừng này khỏi tuyến đường?")) return;
        try {
            await routeStopApi.deleteRouteStop(routeStopId);
            fetchRouteDetail();
        } catch (error) {
            alert(error.response?.data?.message || "Có lỗi xảy ra khi xóa điểm dừng.");
        }
    };

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
                            <h6 className="fw-bold text-dark border-bottom pb-2 mb-3">Các trạm dừng (Route Stops)</h6>
                            {detail.routeStops && detail.routeStops.length > 0 ? (
                                <div className="table-responsive">
                                    <DndContext
                                        sensors={sensors}
                                        collisionDetection={closestCenter}
                                        onDragEnd={handleDragEnd}
                                    >
                                        <Table size="sm" bordered hover className="align-middle text-center mb-0">
                                            <thead className="table-light text-secondary">
                                                <tr>
                                                    <th className="fw-semibold" style={{ width: '120px' }}>Thứ tự</th>
                                                    <th className="fw-semibold text-start">Tên trạm</th>
                                                    <th className="fw-semibold">Khoảng cách từ điểm xuất phát</th>
                                                    <th className="fw-semibold">Thời gian từ điểm xuất phát</th>
                                                    <th className="fw-semibold">Hành động</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <SortableContext
                                                    items={detail.routeStops.map(s => s.routeStopId)}
                                                    strategy={verticalListSortingStrategy}
                                                >
                                                    {detail.routeStops.map(stop => (
                                                        <SortableRouteStopRow
                                                            key={stop.routeStopId}
                                                            stop={stop}
                                                            onEdit={setEditingStop}
                                                            onDelete={handleDeleteRouteStop}
                                                        />
                                                    ))}
                                                </SortableContext>
                                            </tbody>
                                        </Table>
                                    </DndContext>
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

            <RouteStopUpdateInfoModal
                isOpen={!!editingStop}
                data={editingStop}
                routeTotalKilometers={detail.totalKilometers}
                routeTotalMinutes={detail.totalMinutes}
                onClose={() => setEditingStop(null)}
                onSuccess={() => {
                    setEditingStop(null);
                    fetchRouteDetail();
                }}
            />
        </Modal>
    );
}
