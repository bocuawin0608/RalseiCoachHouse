import { useCallback, useEffect, useMemo, useState } from "react"
import { routeApi } from "../api/routeApi";
import { coachStopApi } from "../../coachStops/api/coachStopApi";
import { routeStopApi } from "../api/routeStopApi";
import RouteStopUpdateInfoModal from "./RouteStopUpdateInfoModal";
import { Alert, Badge, Button, Form, Modal, Spinner, Table } from "react-bootstrap";
import { BsExclamationTriangleFill, BsTrash, BsPencilFill, BsPlusCircleFill, BsGeoAltFill, BsXLg, BsSearch } from "react-icons/bs";
import { FaArrowRightLong } from "react-icons/fa6";
import { TiTick } from "react-icons/ti";
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

    // Right panel state
    const [showRightPanel, setShowRightPanel] = useState(false);
    const [coachStops, setCoachStops] = useState([]);
    const [coachStopsLoading, setCoachStopsLoading] = useState(false);
    const [coachStopSearch, setCoachStopSearch] = useState('');
    const [addingStopId, setAddingStopId] = useState(null);
    const [isClosingRightPanel, setIsClosingRightPanel] = useState(false);
    const [pendingCoachStop, setPendingCoachStop] = useState(null); // coach stop waiting to be placed at a row
    const [calculatingDistances, setCalculatingDistances] = useState(false);

    const firstStop = useMemo(() => {
        if (!detail.routeStops || detail.routeStops.length === 0) return null;
        return detail.routeStops.reduce((min, s) => s.stopOrder < min.stopOrder ? s : min, detail.routeStops[0]);
    }, [detail.routeStops]);

    const lastStop = useMemo(() => {
        if (!detail.routeStops || detail.routeStops.length === 0) return null;
        return detail.routeStops.reduce((max, s) => s.stopOrder > max.stopOrder ? s : max, detail.routeStops[0]);
    }, [detail.routeStops]);

    // Filter out stops already assigned to this route
    const availableCoachStops = useMemo(() => {
        const existingStopIds = (detail.routeStops || []).map(rs => rs.stopPointId);
        let filtered = coachStops.filter(cs => !existingStopIds.includes(cs.stopPointId));
        if (coachStopSearch.trim()) {
            const search = coachStopSearch.toLowerCase().trim();
            filtered = filtered.filter(cs =>
                cs.stopPointName?.toLowerCase().includes(search) ||
                cs.address?.toLowerCase().includes(search) ||
                cs.city?.toLowerCase().includes(search)
            );
        }
        return filtered;
    }, [coachStops, detail.routeStops, coachStopSearch]);

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
                await routeStopApi.calculateDistances({ routeId: Number(detail.routeId) });
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

    const fetchCoachStops = useCallback(async () => {
        setCoachStopsLoading(true);
        try {
            const data = await coachStopApi.getAllCoachStops('', true, 0, 1000);
            setCoachStops(data.content || []);
        } catch (err) {
            console.error("Lỗi khi tải danh sách trạm dừng:", err);
        } finally {
            setCoachStopsLoading(false);
        }
    }, []);

    useEffect(() => {
        const load = () => {
            fetchRouteDetail();
        }
        load();
    }, [fetchRouteDetail])

    // Fetch coach stops when right panel opens
    useEffect(() => {
        if (showRightPanel) {
            fetchCoachStops();
        }
    }, [showRightPanel, fetchCoachStops]);

    // Reset right panel when modal closes
    useEffect(() => {
        if (!isOpen) {
            setShowRightPanel(false);
            setIsClosingRightPanel(false);
            setCoachStopSearch('');
        }
    }, [isOpen]);

    const handleDeleteRouteStop = async (routeStopId) => {
        if (!window.confirm("Bạn có chắc muốn xóa điểm dừng này khỏi tuyến đường?")) return;
        try {
            await routeStopApi.deleteRouteStop(routeStopId);
            fetchRouteDetail();
        } catch (error) {
            alert(error.response?.data?.message || "Có lỗi xảy ra khi xóa điểm dừng.");
        }
    };

    // Step 1: user picks a coach stop from the right panel → store it as pending
    const handleSelectCoachStopForInsert = (coachStop) => {
        setPendingCoachStop(coachStop);
    };

    // Step 2: user clicks the green + on a row → insert pending coach stop after that row's order
    const handleInsertCoachStopAtOrder = async (afterStopOrder) => {
        if (!pendingCoachStop) return;
        const cs = pendingCoachStop;
        setPendingCoachStop(null);
        setAddingStopId(cs.stopPointId);
        try {
            // Shift all existing stops with order >= afterStopOrder + 1 up by 1
            const insertOrder = afterStopOrder + 1;
            const stopsToShift = detail.routeStops
                .filter(s => s.stopOrder >= insertOrder)
                .map(s => ({ routeStopId: s.routeStopId, stopOrder: s.stopOrder + 1 }));

            if (stopsToShift.length > 0) {
                await routeStopApi.bulkUpdateOrders(stopsToShift);
            }

            await routeStopApi.createRouteStop({
                routeId: Number(detail.routeId),
                stopPointId: cs.stopPointId,
                stopOrder: insertOrder,
                kilometersFromStart: 0,
                minutesFromStart: 0
            });

            await routeStopApi.calculateDistances({ routeId: Number(detail.routeId) });

            await fetchRouteDetail();
        } catch (err) {
            alert(err.response?.data?.message || "Có lỗi xảy ra khi thêm điểm dừng.");
        } finally {
            setAddingStopId(null);
        }
    };

    // Fallback: add to the very end (used when there are no existing stops)
    const handleAddCoachStopAtEnd = async (coachStop) => {
        setAddingStopId(coachStop.stopPointId);
        try {
            const nextOrder = detail.routeStops.length > 0
                ? Math.max(...detail.routeStops.map(s => s.stopOrder)) + 1
                : 1;

            await routeStopApi.createRouteStop({
                routeId: Number(detail.routeId),
                stopPointId: coachStop.stopPointId,
                stopOrder: nextOrder,
                kilometersFromStart: 0,
                minutesFromStart: 0
            });

            await routeStopApi.calculateDistances({ routeId: Number(detail.routeId) });

            await fetchRouteDetail();
        } catch (err) {
            alert(err.response?.data?.message || "Có lỗi xảy ra khi thêm điểm dừng.");
        } finally {
            setAddingStopId(null);
        }
    };

    // Handle "Hoàn Tất" — calculate distances for all stops with 0 km/minutes
    const handleCalculateDistances = async () => {
        const hasZeroStops = detail.routeStops.some(
            s => Number(s.kilometersFromStart) === 0 && Number(s.minutesFromStart) === 0
        );

        if (!hasZeroStops) {
            // No stops need calculating, just close the panel
            setIsClosingRightPanel(true);
            setTimeout(() => {
                setShowRightPanel(false);
                setIsClosingRightPanel(false);
            }, 240);
            return;
        }

        setCalculatingDistances(true);
        try {
            await routeStopApi.calculateDistances({ routeId: Number(detail.routeId) });
            await fetchRouteDetail();
            // Close the right panel after success
            setIsClosingRightPanel(true);
            setTimeout(() => {
                setShowRightPanel(false);
                setIsClosingRightPanel(false);
            }, 240);
        } catch (err) {
            alert(err.response?.data?.message || "Có lỗi xảy ra khi tính khoảng cách.");
        } finally {
            setCalculatingDistances(false);
        }
    };

    if (!isOpen) return null;

    return (
        <Modal
            show={isOpen}
            onHide={onClose}
            size={showRightPanel ? 'xl' : 'lg'}
            centered
            backdrop="static"
        >
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Chi tiết tuyến đường
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-0 py-0">
                <div className="d-flex" style={{ minHeight: '400px' }}>
                    {/* ===== LEFT COLUMN: Route detail ===== */}
                    <div
                        className="px-4 py-4 overflow-auto"
                        style={{
                            flex: showRightPanel ? '2 1 0%' : '1 1 100%',
                            transition: 'flex 0.3s ease',
                            maxHeight: '70vh'
                        }}
                    >
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
                                {/* Route Origin → Destination Summary */}
                                <div className="bg-light rounded-3 p-3 border">
                                    <div className="d-flex align-items-center justify-content-between mb-3">
                                        <span className="fw-bold text-dark">Tổng quan tuyến đường</span>
                                        <div className="d-flex gap-2">
                                            {showRightPanel && (
                                                <Button
                                                    size="sm"
                                                    variant="success"
                                                    className="d-flex align-items-center gap-1 text-white"
                                                    onClick={handleCalculateDistances}
                                                    disabled={isClosingRightPanel || calculatingDistances}
                                                >
                                                    {calculatingDistances ? (
                                                        <>
                                                            <Spinner animation="border" size="sm" style={{ width: '16px', height: '16px' }} />
                                                            Đang tính...
                                                        </>
                                                    ) : (
                                                        <>
                                                            <TiTick size={18} />
                                                            Hoàn tất
                                                        </>
                                                    )}
                                                </Button>
                                            )}
                                            <Button
                                                size="sm"
                                                variant={showRightPanel ? "danger" : undefined}
                                                className={`d-flex align-items-center gap-1 ${!showRightPanel ? 'custom-btn-general' : ''}`}
                                                onClick={() => {
                                                    if (showRightPanel) {
                                                        setIsClosingRightPanel(true);
                                                        setTimeout(() => {
                                                            setShowRightPanel(false);
                                                            setIsClosingRightPanel(false);
                                                        }, 240);
                                                    } else {
                                                        setShowRightPanel(true);
                                                        setPendingCoachStop(null);
                                                    }
                                                }}
                                                disabled={isClosingRightPanel}
                                            >
                                                {showRightPanel ? (
                                                    <>
                                                        <BsXLg size={12} />
                                                        Hủy
                                                    </>
                                                ) : (
                                                    <>
                                                        <BsPlusCircleFill size={14} />
                                                        Thêm điểm dừng
                                                    </>
                                                )}
                                            </Button>
                                        </div>
                                    </div>
                                    <div className="d-flex align-items-center gap-2">
                                        {/* First stop (origin) */}
                                        <div className="d-flex align-items-center gap-2 flex-fill">
                                            <BsGeoAltFill size={18} className="text-success flex-shrink-0" />
                                            <div>
                                                <small className="text-secondary d-block" style={{ fontSize: '0.7rem' }}>ĐIỂM ĐẦU</small>
                                                <span className="fw-semibold text-dark" style={{ fontSize: '0.9rem' }}>
                                                    {firstStop ? firstStop.stopPointName : '—'}
                                                </span>
                                            </div>
                                        </div>

                                        {/* Connector */}
                                        <div className="flex-fill d-flex align-items-center px-2 justify-content-center" style={{ minWidth: '40px' }}>
                                            <FaArrowRightLong size={30} className="text-secondary flex-shrink-0" />
                                        </div>

                                        {/* Last stop (destination) */}
                                        <div className="d-flex align-items-center gap-2 flex-fill justify-content-end text-end">
                                            <div>
                                                <small className="text-secondary d-block" style={{ fontSize: '0.7rem' }}>ĐIỂM CUỐI</small>
                                                <span className="fw-semibold text-dark" style={{ fontSize: '0.9rem' }}>
                                                    {lastStop ? lastStop.stopPointName : '—'}
                                                </span>
                                            </div>
                                            <BsGeoAltFill size={18} className="text-danger flex-shrink-0" />
                                        </div>
                                    </div>
                                </div>

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
                                                            <th
                                                                style={{
                                                                    width: pendingCoachStop ? '36px' : '0px',
                                                                    padding: pendingCoachStop ? undefined : '0px',
                                                                    overflow: 'hidden',
                                                                    transition: 'width 0.2s ease, padding 0.2s ease',
                                                                    borderRight: pendingCoachStop ? undefined : 'none',
                                                                }}
                                                            />
                                                            <th className="fw-semibold" style={{ width: '120px' }}>Thứ tự</th>
                                                            <th className="fw-semibold text-start">Tên trạm</th>
                                                            <th className="fw-semibold text-start">Thành phố / Tỉnh</th>
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
                                                                    showAddButton={!!pendingCoachStop}
                                                                    onAddAtOrder={handleInsertCoachStopAtOrder}
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
                    </div>

                    {/* ===== RIGHT COLUMN: Coach stops list ===== */}
                    {showRightPanel && (
                        <div
                            className="border-start bg-white overflow-auto px-3 py-4"
                            style={{
                                flex: '1 1 0%',
                                maxHeight: '70vh',
                                animation: isClosingRightPanel ? 'fadeOut 0.25s ease forwards' : 'slideInRight 0.25s ease'
                            }}
                        >
                            <div className="d-flex align-items-center justify-content-between mb-3">
                                <h6 className="fw-bold text-dark mb-0" style={{ fontSize: '0.95rem' }}>
                                    Danh sách trạm dừng
                                </h6>
                                <Badge bg="secondary" pill>
                                    {availableCoachStops.length}
                                </Badge>
                            </div>

                            {/* Search box */}
                            <div className="position-relative mb-3">
                                <BsSearch
                                    size={14}
                                    className="position-absolute text-secondary"
                                    style={{ left: '10px', top: '50%', transform: 'translateY(-50%)' }}
                                />
                                <Form.Control
                                    type="text"
                                    size="sm"
                                    placeholder="Tìm kiếm trạm dừng..."
                                    value={coachStopSearch}
                                    onChange={(e) => setCoachStopSearch(e.target.value)}
                                    style={{ paddingLeft: '32px' }}
                                    className="rounded-pill"
                                />
                            </div>

                            {coachStopsLoading ? (
                                <div className="d-flex flex-column align-items-center justify-content-center py-4">
                                    <Spinner animation="border" size="sm" variant="primary" />
                                    <small className="mt-2 text-secondary">Đang tải...</small>
                                </div>
                            ) : availableCoachStops.length === 0 ? (
                                <div className="text-center py-4">
                                    <p className="text-muted mb-0 fst-italic" style={{ fontSize: '0.85rem' }}>
                                        {coachStopSearch ? 'Không tìm thấy trạm dừng phù hợp.' : 'Tất cả trạm dừng đã được thêm vào tuyến.'}
                                    </p>
                                </div>
                            ) : (
                                <div className="d-flex flex-column gap-2">
                                    {pendingCoachStop && (
                                        <div className="alert alert-success py-2 px-2 mb-1 d-flex align-items-center gap-2" style={{ fontSize: '0.8rem' }}>
                                            <BsPlusCircleFill className="text-success flex-shrink-0" />
                                            <span>Chọn vị trí trong bảng để chèn <strong>{pendingCoachStop.stopPointName}</strong></span>
                                            <button
                                                type="button"
                                                className="btn-close ms-auto"
                                                style={{ fontSize: '0.6rem' }}
                                                onClick={() => setPendingCoachStop(null)}
                                                title="Hủy"
                                            />
                                        </div>
                                    )}
                                    {availableCoachStops.map(cs => (
                                        <div
                                            key={cs.stopPointId}
                                            className="d-flex align-items-center gap-2 p-2 rounded border bg-light coach-stop-item"
                                            style={{ cursor: 'pointer', transition: 'all 0.15s ease' }}
                                        >
                                            <div className="flex-fill" style={{ minWidth: 0 }}>
                                                <div className="fw-semibold text-dark text-truncate" style={{ fontSize: '0.85rem' }}>
                                                    {cs.stopPointName}
                                                </div>
                                                <small className="text-muted text-truncate d-block" style={{ fontSize: '0.75rem' }}>
                                                    {cs.address}
                                                </small>
                                            </div>
                                            <Button
                                                size="sm"
                                                variant={pendingCoachStop?.stopPointId === cs.stopPointId ? 'warning' : undefined}
                                                className={`flex-shrink-0 d-flex align-items-center justify-content-center rounded-circle p-0 text-white ${pendingCoachStop?.stopPointId !== cs.stopPointId ? 'custom-btn-general' : ''}`}
                                                style={{ width: '28px', height: '28px' }}
                                                onClick={() => {
                                                    if (detail.routeStops.length === 0) {
                                                        // No existing stops → just add to end directly
                                                        handleAddCoachStopAtEnd(cs);
                                                    } else if (pendingCoachStop?.stopPointId === cs.stopPointId) {
                                                        // Clicking the same btn again → cancel pending
                                                        setPendingCoachStop(null);
                                                    } else {
                                                        handleSelectCoachStopForInsert(cs);
                                                    }
                                                }}
                                                disabled={addingStopId === cs.stopPointId}
                                                title="Thêm vào tuyến"
                                            >
                                                {addingStopId === cs.stopPointId ? (
                                                    <Spinner animation="border" size="sm" style={{ width: '14px', height: '14px' }} />
                                                ) : (
                                                    <BsPlusCircleFill size={14} />
                                                )}
                                            </Button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}
                </div>
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

            {/* Inline styles for slide-in animation and hover */}
            <style>{`
                @keyframes slideInRight {
                    from {
                        opacity: 0;
                        transform: translateX(20px);
                    }
                    to {
                        opacity: 1;
                        transform: translateX(0);
                    }
                }
                @keyframes fadeOut {
                    from {
                        opacity: 1;
                    }
                    to {
                        opacity: 0;
                    }
                }
                .coach-stop-item:hover {
                    background-color: #e8f0fe !important;
                    border-color: #4285f4 !important;
                }
            `}</style>
        </Modal>
    );
}
