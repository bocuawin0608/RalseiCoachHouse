import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { Alert, Badge, Button, Card, Col, Container, Form, Modal, Row, Spinner, Table, Toast, ToastContainer } from 'react-bootstrap';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill, BsPencilFill } from 'react-icons/bs';
import { coachApi } from '../../../features/coaches/api/coachApi';
import SeatMapBuilder from '../../../features/coaches/components/SeatMapBuilder';
import { useCoachTypeDropdown } from '../../../hooks/useCoachTypeDropdown';
import { useRouteDropdown } from '../../../hooks/useRouteDropdown';
import {
    COACH_VALIDATION,
    validateAndFormatLicensePlate,
    getMinDateTime,
    isFutureDateTimeLocal,
    isValidSeatCode,
    normalizeSeatCodeInput,
} from '../../../utils/coachValidation';

const STATUS_LABELS = {
    ACTIVE: { text: 'Đang hoạt động', bg: 'success' },
    MAINTENANCE: { text: 'Đang bảo trì', bg: 'warning' },
    RETIRED: { text: 'Ngừng hoạt động', bg: 'danger' },
};

const LOG_STATUS_LABELS = {
    ACTIVE: 'Đang hoạt động',
    MAINTENANCE: 'Đang bảo trì',
    RETIRED: 'Ngừng hoạt động',
};

export default function CoachDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [detail, setDetail] = useState(null);
    const [statusLogs, setStatusLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [editInfo, setEditInfo] = useState(false);
    const { register, handleSubmit, setValue, reset, formState: { errors } } = useForm({
        defaultValues: {
            licensePlate: '',
            manufacturer: '',
            year: new Date().getFullYear(),
            coachTypeId: '',
            routeId: ''
        }
    });
    const [savingInfo, setSavingInfo] = useState(false);

    const [editSeats, setEditSeats] = useState(false);
    const [seatMatrix, setSeatMatrix] = useState([]);
    const [savingSeats, setSavingSeats] = useState(false);

    const [actionModal, setActionModal] = useState(null);
    const [actionForm, setActionForm] = useState({ reason: '', expectedEndAt: '' });
    const [actionCheck, setActionCheck] = useState(null);
    const [submittingAction, setSubmittingAction] = useState(false);

    const { coachTypes, loadingCoachTypes } = useCoachTypeDropdown(true);
    const { routes, loadingRoutes } = useRouteDropdown(true);
    const isDropdownLoading = loadingCoachTypes || loadingRoutes;

    const fetchDetail = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const [detailRes, logsRes] = await Promise.all([
                coachApi.getCoachDetail(id),
                coachApi.getStatusLogs(id, { page: 0, size: 20 }),
            ]);
            setDetail(detailRes);
            setStatusLogs(logsRes.content ?? []);
            reset({
                licensePlate: detailRes.licensePlate,
                manufacturer: detailRes.manufacturer,
                year: detailRes.year,
                coachTypeId: detailRes.coachTypeId,
                routeId: detailRes.routeId ?? '',
            });
        } catch (err) {
            setError({message: err.response?.data?.message || 'Có lỗi khi tải thông tin xe.'});
        } finally {
            setLoading(false);
        }
    }, [id, reset]);

    useEffect(() => {
        fetchDetail();
    }, [fetchDetail]);

    const parsedLayout = useMemo(() => {
        if (!detail?.seats?.length) {
            return { floors: [], rows: 0, cols: 0 };
        }
        let maxFloor = 0;
        let maxRow = 0;
        let maxCol = 0;
        detail.seats.forEach((seat) => {
            if (seat.floorIndex > maxFloor) maxFloor = seat.floorIndex;
            if (seat.rowIndex > maxRow) maxRow = seat.rowIndex;
            if (seat.colIndex > maxCol) maxCol = seat.colIndex;
        });
        const floorMatrix = Array(maxFloor).fill().map(() =>
            Array(maxRow).fill().map(() => Array(maxCol).fill(null))
        );
        detail.seats.forEach((seat) => {
            floorMatrix[seat.floorIndex - 1][seat.rowIndex - 1][seat.colIndex - 1] = seat;
        });
        return { floors: floorMatrix, rows: maxRow, cols: maxCol };
    }, [detail?.seats]);

    useEffect(() => {
        if (!detail?.seats?.length) return;
        const deepClonedMatrix = parsedLayout.floors.map((floor) =>
            floor.map((row) =>
                row.map((cell) => (cell ? { ...cell } : null))
            )
        );
        setSeatMatrix(deepClonedMatrix);
    }, [detail?.coachId, detail?.seats?.length, parsedLayout]);

    const handleSaveInfo = async (data) => {
        setSavingInfo(true);
        setError(null);
        try {
            const plateResult = validateAndFormatLicensePlate(data.licensePlate);
            if (!plateResult.valid) {
                setError({ message: COACH_VALIDATION.LICENSE_PLATE_MESSAGE });
                return;
            }

            const payload = {
                licensePlate: plateResult.data,
                manufacturer: data.manufacturer.trim(),
                routeId: data.routeId === '' ? null : Number(data.routeId),
                coachTypeId: Number(data.coachTypeId),
                year: Number(data.year),
            };
            await coachApi.updateCoachInfo(id, payload);
            setEditInfo(false);
            await fetchDetail();
        } catch (err) {
            setError({message: err.response?.data?.message || 'Có lỗi khi cập nhật thông tin xe.'});
        } finally {
            setSavingInfo(false);
        }
    };

    const handleSeatMatrixChange = (floorIndex, newMatrix) => {
        setSeatMatrix((prev) => {
            const next = [...prev];
            next[floorIndex] = newMatrix.map((row) => [...row]);
            return next;
        });
    };

    const handleSaveSeats = async () => {
        setSavingSeats(true);
        setError(null);
        try {
            const seats = [];
            const seenCodes = new Set();

            seatMatrix.forEach((floor) => {
                floor.forEach((row) => {
                    row.forEach((cell) => {
                        if (cell) {
                            const seatCode = normalizeSeatCodeInput(cell.seatCode);
                            if (!isValidSeatCode(seatCode)) {
                                throw new Error(COACH_VALIDATION.SEAT_CODE_MESSAGE);
                            }
                            if (seenCodes.has(seatCode)) {
                                throw new Error(`Mã ghế trùng lặp trong cùng xe: ${seatCode}`);
                            }
                            seenCodes.add(seatCode);
                            seats.push({ seatId: cell.seatId, isActive: cell.isActive, seatCode });
                        }
                    });
                });
            });
            await coachApi.updateCoachSeats(id, { seats });
            setEditSeats(false);
            await fetchDetail();
        } catch (err) {
            if (err.message && !err.response) {
                setError({ message: err.message });
                return;
            }
            const backendError = err.response?.data;
            
            if (backendError) {
                setError({
                    message: backendError.message || 'Dữ liệu không hợp lệ.',
                    fieldErrors: backendError.fieldErrors
                });
            } else {
                setError({
                    message: err.message || 'Có lỗi khi cập nhật ghế.'
                });
            }
        } finally {
            setSavingSeats(false);
        }
    };

    const openAction = async (type, targetStatus) => {
        setActionModal(type);
        setActionForm({ reason: '', expectedEndAt: '' });
        setActionCheck(null);
        if (targetStatus === 'MAINTENANCE' || targetStatus === 'RETIRED') {
            try {
                const check = await coachApi.getStatusChangeCheck(id, targetStatus);
                setActionCheck(check);
            } catch (err) {
                setError({message: err.response?.data?.message || 'Không thể kiểm tra điều kiện đổi trạng thái.'});
                setActionModal(null);
            }
        }
    };

    const closeAction = () => {
        setActionModal(null);
        setActionForm({ reason: '', expectedEndAt: '' });
        setActionCheck(null);
    };

    const handleSubmitAction = async (e) => {
        e.preventDefault();
        setSubmittingAction(true);
        setError(null);
        try {
            if (actionModal === 'maintenance') {
                if (actionForm.expectedEndAt && !isFutureDateTimeLocal(actionForm.expectedEndAt)) {
                    setError({ message: 'Thời gian dự kiến hoàn thành phải ở tương lai.' });
                    return;
                }
                await coachApi.reportMaintenance(id, {
                    reason: actionForm.reason.trim(),
                    expectedEndAt: actionForm.expectedEndAt || null,
                });
            } else if (actionModal === 'reactivate') {
                await coachApi.reactivate(id, { reason: actionForm.reason.trim() || null });
            } else if (actionModal === 'retire') {
                if(!window.confirm("Xe ngừng hoạt động không thể phục hồi trạng thái. Bạn có chắc chắn muốn ngừng hoạt động xe?")) {
                    closeAction();
                    return;
                }
                await coachApi.retire(id, { reason: actionForm.reason.trim() });
            }
            closeAction();
            await fetchDetail();
        } catch (err) {
            setError({message: err.response?.data?.message || 'Có lỗi khi thực hiện hành động.'});
        } finally {
            setSubmittingAction(false);
        }
    };

    if (loading) {
        return (
            <Container fluid className="py-5 text-center">
                <Spinner animation="border" variant="primary" />
            </Container>
        );
    }

    if (!detail) {
        return (
            <Container fluid className="py-4">
                <Alert variant="danger">{error || 'Không tìm thấy xe.'}</Alert>
            </Container>
        );
    }

    const status = detail.status;
    const statusLabel = STATUS_LABELS[status] || STATUS_LABELS.RETIRED;

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            <Button
                variant="link"
                onClick={() => navigate('/management/coaches')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18} /> Quay lại danh sách
            </Button>

            <div className="d-flex justify-content-between align-items-start mb-4 flex-wrap gap-2">
                <div>
                    <h2 className="fw-bold text-dark mb-1">{detail.licensePlate}</h2>
                    <span className="text-secondary">{detail.coachTypeName}</span>
                </div>
                <Badge bg={statusLabel.bg} className="px-3 py-2 fs-6">{statusLabel.text}</Badge>
            </div>

            {detail.latestStatusLog && (
                <Alert variant="light" className="border mb-4">
                    <strong>Ghi chú trạng thái gần nhất:</strong> {detail.latestStatusLog.reason}
                    {detail.latestStatusLog.expectedEndAt && (
                        <span className="text-secondary ms-2">
                            (Dự kiến xong: {new Date(detail.latestStatusLog.expectedEndAt).toLocaleString('vi-VN')})
                        </span>
                    )}
                </Alert>
            )}

            <Card className="shadow-sm border-0 mb-4">
                <Card.Body className="p-4">
                    <h5 className="fw-bold mb-3">Hành động nghiệp vụ</h5>
                    <div className="d-flex flex-wrap gap-2">
                        {detail.canReportMaintenance && (
                            <Button variant="warning" onClick={() => openAction('maintenance', 'MAINTENANCE')}>
                                Báo bảo trì
                            </Button>
                        )}
                        {detail.canReactivate && (
                            <Button variant="success" onClick={() => openAction('reactivate', 'ACTIVE')}>
                                Đưa vào hoạt động
                            </Button>
                        )}
                        {detail.canRetire && (
                            <Button variant="danger" onClick={() => openAction('retire', 'RETIRED')}>
                                Ngừng sử dụng
                            </Button>
                        )}
                        {status === 'RETIRED' && (
                            <span className="text-muted align-self-center">Xe đã ngừng hoạt động, không có hành động khả dụng.</span>
                        )}
                    </div>
                </Card.Body>
            </Card>

            <Card className="shadow-sm border-0 mb-4">
                <Card.Body className="p-4">
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h5 className="fw-bold mb-0">Thông tin xe</h5>
                        {!editInfo ? (
                            <Button size="sm" variant="outline-primary" onClick={() => setEditInfo(true)}>
                                <BsPencilFill className="me-1" /> Chỉnh sửa
                            </Button>
                        ) : (
                            <div className="d-flex gap-2">
                                <Button size="sm" variant="outline-secondary" onClick={() => {
                                    setEditInfo(false);
                                    reset({
                                        licensePlate: detail.licensePlate,
                                        manufacturer: detail.manufacturer,
                                        year: detail.year,
                                        coachTypeId: detail.coachTypeId,
                                        routeId: detail.routeId ?? '',
                                    });
                                }}>
                                    Hủy
                                </Button>
                                <Button size="sm" className="custom-btn-general" disabled={savingInfo} onClick={handleSubmit(handleSaveInfo)}>
                                    {savingInfo ? 'Đang lưu...' : 'Lưu'}
                                </Button>
                            </div>
                        )}
                    </div>
                    {editInfo ? (
                        <Form>
                            <Row className="g-3">
                                <Col md={6}>
                                    <Form.Label className="small fw-semibold">Biển số xe</Form.Label>
                                    <Form.Control 
                                        maxLength={20}
                                        isInvalid={!!errors.licensePlate} 
                                        {...register('licensePlate', {
                                            required: 'Biển số xe không được để trống.',
                                            validate: (value) => {
                                                const result = validateAndFormatLicensePlate(value);
                                                return result.valid || COACH_VALIDATION.LICENSE_PLATE_MESSAGE;
                                            },
                                            onBlur: (e) => {
                                                const result = validateAndFormatLicensePlate(e.target.value);
                                                if (result.valid) {
                                                    setValue('licensePlate', result.data);
                                                }
                                            }
                                        })}
                                    />
                                    <Form.Control.Feedback type="invalid">
                                        {errors.licensePlate?.message}
                                    </Form.Control.Feedback>
                                </Col>
                                <Col md={6}>
                                    <Form.Label className="small fw-semibold">Hãng xe</Form.Label>
                                    <Form.Control 
                                        isInvalid={!!errors.manufacturer}
                                        {...register('manufacturer', { 
                                            required: 'Hãng xe không được để trống.',
                                            validate: (value) => value.trim().length > 0 || 'Hãng xe không được để trống.',
                                        })} 
                                    />
                                    <Form.Control.Feedback type="invalid">
                                        {errors.manufacturer?.message}
                                    </Form.Control.Feedback>
                                </Col>
                                <Col md={6}>
                                    <Form.Label className="small fw-semibold">Năm sản xuất</Form.Label>
                                    <Form.Control 
                                        type="number" 
                                        isInvalid={!!errors.year}
                                        {...register('year', {
                                            required: 'Năm sản xuất không được để trống.',
                                            min: { value: COACH_VALIDATION.YEAR_MIN, message: 'Năm sản xuất phải lớn hơn hoặc bằng 2000.' },
                                            max: { value: COACH_VALIDATION.getYearMax(), message: 'Năm sản xuất không được lớn hơn năm hiện tại.' }
                                        })}
                                    />
                                    <Form.Control.Feedback type="invalid">
                                        {errors.year?.message}
                                    </Form.Control.Feedback>
                                </Col>
                                <Col md={6}>
                                    <Form.Label className="small fw-semibold">Loại xe</Form.Label>
                                    <Form.Select 
                                        isInvalid={!!errors.coachTypeId}
                                        disabled={isDropdownLoading}
                                        {...register('coachTypeId', { 
                                            required: 'Vui lòng chọn loại xe.' 
                                        })}
                                    >
                                        {coachTypes.map((ct) => (
                                            <option key={ct.coachTypeId} value={ct.coachTypeId}>{ct.coachTypeName}</option>
                                        ))}
                                    </Form.Select>
                                    <Form.Control.Feedback type="invalid">
                                        {errors.coachTypeId?.message}
                                    </Form.Control.Feedback>
                                </Col>
                                <Col md={6}>
                                    <Form.Label className="small fw-semibold">Tuyến (tùy chọn)</Form.Label>
                                    <Form.Select 
                                        disabled={isDropdownLoading}
                                        {...register('routeId')}
                                    >
                                        <option value="">-- Chọn tuyến --</option>
                                        {routes.map((r) => (
                                            <option key={r.routeId} value={r.routeId}>{r.routeName}</option>
                                        ))}
                                    </Form.Select>
                                </Col>
                            </Row>
                        </Form>
                    ) : (
                        <Row className="gy-3">
                            <Col sm={6}><p className="text-secondary small mb-1">Biển số xe</p><p className="mb-0">{detail.licensePlate}</p></Col>
                            <Col sm={6}><p className="text-secondary small mb-1">Hãng xe</p><p className="mb-0">{detail.manufacturer}</p></Col>
                            <Col sm={6}><p className="text-secondary small mb-1">Năm sản xuất</p><p className="mb-0">{detail.year}</p></Col>
                            <Col sm={6}><p className="text-secondary small mb-1">Loại xe</p><p className="mb-0">{detail.coachTypeName}</p></Col>
                            <Col sm={6}><p className="text-secondary small mb-1">Tuyến xe</p><p className="mb-0">{detail.routeName}</p></Col>
                            <Col sm={6}><p className="text-secondary small mb-1">Ghế khả dụng</p><p className="mb-0 text-success fw-bold">{detail.totalActiveSeats}</p></Col>
                        </Row>
                    )}
                </Card.Body>
            </Card>

            <Card className="shadow-sm border-0 mb-4">
                <Card.Body className="p-4">
                    <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                        <div>
                            <h5 className="fw-bold">Sơ đồ ghế</h5>
                            <p className="text-secondary small mb-1">
                                * <span className="text-success fw-bold">Màu xanh</span>: Ghế ngồi (Khả dụng). <span className="text-danger fw-bold">Màu đỏ</span>: Ghế khóa (Tạm ngừng sử dụng).
                            </p>
                            <p className="text-secondary small">* Ở chế độ chỉnh sửa: Click vào ghế để chuyển đổi trạng thái ghế. Click vào tên ghế để đổi tên.</p>
                        </div>
                        {status !== 'RETIRED' && !editSeats && (
                            <Button size="sm" variant="outline-primary" onClick={() => setEditSeats(true)}>
                                <BsPencilFill className="me-1" /> Chỉnh sửa ghế
                            </Button>
                        )}
                        {editSeats && (
                            <div className="d-flex gap-2">
                                <Button size="sm" variant="outline-secondary" onClick={() => { 
                                    setEditSeats(false); 
                                    reset({
                                        licensePlate: detail.licensePlate,
                                        manufacturer: detail.manufacturer,
                                        year: detail.year,
                                        coachTypeId: detail.coachTypeId,
                                        routeId: detail.routeId ?? '',
                                    });
                                    if (parsedLayout?.floors) {
                                        const resetMatrix = parsedLayout.floors.map((floor) =>
                                            floor.map((row) =>
                                                row.map((cell) => (cell ? { ...cell } : null))
                                            )
                                        );
                                        setSeatMatrix(resetMatrix);
                                    }
                                }}>
                                    Hủy
                                </Button>
                                <Button size="sm" className="custom-btn-general" disabled={savingSeats} onClick={handleSaveSeats}>
                                    <BsCheckCircle className="me-1" />
                                    {savingSeats ? 'Đang lưu...' : 'Lưu ghế'}
                                </Button>
                            </div>
                        )}
                    </div>
                    <div className="d-flex justify-content-center gap-2 overflow-auto bg-light rounded border p-4">
                        {seatMatrix.map((matrix, index) => (
                            <div key={index} className="text-center">
                                <p className="mb-2 fw-medium">Tầng {index + 1}</p>
                                <div className="border border-secondary rounded p-3 bg-white shadow-sm">
                                    <SeatMapBuilder
                                        mode={editSeats ? 'EDIT-SEAT' : 'VIEW-SEAT'}
                                        rows={parsedLayout.rows}
                                        cols={parsedLayout.cols}
                                        initialMatrix={matrix}
                                        onChange={editSeats ? (m) => handleSeatMatrixChange(index, m) : undefined}
                                    />
                                </div>
                            </div>
                        ))}
                    </div>
                </Card.Body>
            </Card>

            <Card className="shadow-sm border-0">
                <Card.Body className="p-0">
                    <div className="p-4 border-bottom">
                        <h5 className="fw-bold mb-0">Lịch sử trạng thái</h5>
                    </div>
                    <Table responsive hover className="mb-0 align-middle">
                        <thead className="table-light">
                            <tr>
                                <th className="px-3 py-3">Thời gian</th>
                                <th className="px-3 py-3">Từ</th>
                                <th className="px-3 py-3">Sang</th>
                                <th className="px-3 py-3">Lý do</th>
                            </tr>
                        </thead>
                        <tbody>
                            {statusLogs.length === 0 ? (
                                <tr><td colSpan="4" className="text-center p-4 text-muted">Chưa có lịch sử</td></tr>
                            ) : (
                                statusLogs.map((log) => (
                                    <tr key={log.coachStatusLogId}>
                                        <td className="px-3">{log.createdAt ? new Date(log.createdAt).toLocaleString('vi-VN') : '—'}</td>
                                        <td className="px-3">{log.fromStatus ? LOG_STATUS_LABELS[log.fromStatus] : '—'}</td>
                                        <td className="px-3">{LOG_STATUS_LABELS[log.toStatus] ?? log.toStatus}</td>
                                        <td className="px-3">{log.reason}</td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </Table>
                </Card.Body>
            </Card>

            <Modal show={!!actionModal} onHide={closeAction} centered backdrop="static">
                <Modal.Header closeButton>
                    <Modal.Title className="fs-5 fw-bold">
                        {actionModal === 'maintenance' && 'Báo bảo trì'}
                        {actionModal === 'reactivate' && 'Đưa xe vào hoạt động'}
                        {actionModal === 'retire' && 'Ngừng sử dụng xe'}
                    </Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleSubmitAction}>
                    <Modal.Body>
                        {actionCheck && !actionCheck.allowed && (
                            <Alert variant="danger">
                                {actionCheck.message}
                                {actionCheck.upcomingTripCount > 0 && (
                                    <span> ({actionCheck.upcomingTripCount} chuyến liên quan)</span>
                                )}
                            </Alert>
                        )}
                        {(actionModal === 'maintenance' || actionModal === 'retire') && (
                            <Form.Group className="mb-3">
                                <Form.Label className="fw-semibold">Lý do <span className="text-danger">*</span></Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    required
                                    maxLength={500}
                                    value={actionForm.reason}
                                    onChange={(e) => setActionForm((p) => ({ ...p, reason: e.target.value }))}
                                    disabled={actionCheck && !actionCheck.allowed}
                                />
                            </Form.Group>
                        )}
                        {actionModal === 'maintenance' && (
                            <Form.Group className="mb-3">
                                <Form.Label className="fw-semibold">Dự kiến hoàn thành (tùy chọn)</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    min={getMinDateTime()}
                                    value={actionForm.expectedEndAt}
                                    onChange={(e) => setActionForm((p) => ({ ...p, expectedEndAt: e.target.value }))}
                                    disabled={actionCheck && !actionCheck.allowed}
                                />
                            </Form.Group>
                        )}
                        {actionModal === 'reactivate' && (
                            <Form.Group className="mb-3">
                                <Form.Label className="fw-semibold">Ghi chú (tùy chọn)</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={2}
                                    maxLength={500}
                                    value={actionForm.reason}
                                    onChange={(e) => setActionForm((p) => ({ ...p, reason: e.target.value }))}
                                />
                            </Form.Group>
                        )}
                    </Modal.Body>
                    <Modal.Footer className="bg-light border-0">
                        <Button variant="outline-secondary" onClick={closeAction}>Hủy</Button>
                        <Button
                            type="submit"
                            className="custom-btn-general"
                            disabled={submittingAction || (actionCheck && !actionCheck.allowed)}
                        >
                            {submittingAction ? 'Đang xử lý...' : 'Xác nhận'}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>

            <ToastContainer 
                position="top-end" 
                className="p-3" 
                style={{ position: 'fixed', zIndex: 9999 }}
            >
                <Toast 
                    show={!!error} 
                    onClose={() => setError(null)} 
                    delay={9000} 
                    autohide 
                    bg="danger" 
                    text="white"
                >
                    <Toast.Header closeButton={true} className="bg-danger text-white border-0">
                        <strong className="me-auto d-inline-flex align-items-center gap-2">
                            <BsExclamationTriangleFill />
                            <span>Có lỗi xảy ra</span>
                        </strong>
                    </Toast.Header>
                    <Toast.Body className="bg-white text-dark rounded-bottom">
                        {error && (
                            <div>
                                <p className={`fw-semibold mb-2 ${error.fieldErrors ? 'text-danger' : 'mb-0'}`}>
                                    {error.message}
                                </p>
                                {error.fieldErrors && Object.keys(error.fieldErrors).length > 0 && (
                                    <ul className="mb-0 ps-3 text-danger" style={{ fontSize: '0.85rem', lineHeight: '1.4' }}>
                                        {[...new Set(Object.values(error.fieldErrors))].map((errorMessage, index) => (
                                            <li key={index} className="mb-1">
                                                {errorMessage}
                                            </li>
                                        ))}
                                    </ul>
                                )}
                            </div>
                        )}
                    </Toast.Body>
                </Toast>
            </ToastContainer>
        </Container>
    );
}
