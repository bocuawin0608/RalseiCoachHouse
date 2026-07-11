import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Alert, Badge, Button, Card, Col, Container, Form, InputGroup, Nav, Row, Spinner, Tab, Table, OverlayTrigger, Tooltip, Toast, ToastContainer } from 'react-bootstrap';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill, BsPencilFill } from 'react-icons/bs';
import { coachTypeApi, formatPriceEndDate, PRICE_STATUS_LABELS } from '../../../features/coaches/api/coachTypeApi';
import SeatMapBuilder from '../../../features/coaches/components/SeatMapBuilder';
import CoachTypeDeactivationDialog from '../../../features/coaches/components/CoachTypeDeactivationDialog';
import { formatCurrency, getMinDateTime } from '../../../utils/formatters';

export default function CoachTypeDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [detail, setDetail] = useState(null);
    const [prices, setPrices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeTab, setActiveTab] = useState('info');

    const [editInfo, setEditInfo] = useState(false);
    const [name, setName] = useState('');
    const [isActive, setIsActive] = useState(true);
    const [savingInfo, setSavingInfo] = useState(false);

    const [deactivationOpen, setDeactivationOpen] = useState(false);
    const [deactivationData, setDeactivationData] = useState(null);

    const [priceForm, setPriceForm] = useState({
        seatPrice: '',
        startEffectiveDate: '',
    });
    const [savingPrice, setSavingPrice] = useState(false);

    const [editSeatMap, setEditSeatMap] = useState(false);
    const [floorData, setFloorData] = useState([]);
    const [layoutMeta, setLayoutMeta] = useState({ rows: 0, cols: 0, totalFloors: 0 });
    const [savingSeatMap, setSavingSeatMap] = useState(false);

    const fetchAll = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const [detailRes, pricesRes] = await Promise.all([
                coachTypeApi.getCoachTypeDetail(id),
                coachTypeApi.getPriceTimeline(id),
            ]);
            setDetail(detailRes);
            setName(detailRes.coachTypeName);
            setIsActive(detailRes.isActive);
            setPrices(pricesRes);

            if (detailRes.seatLayout) {
                const layout = typeof detailRes.seatLayout === 'string'
                    ? JSON.parse(detailRes.seatLayout)
                    : detailRes.seatLayout;
                setLayoutMeta({
                    rows: layout.rows,
                    cols: layout.cols,
                    totalFloors: layout.totalFloors,
                });
                setFloorData(layout.floors ?? []);
            }
        } catch (err) {
            setError({ message: err.response?.data?.message || 'Có lỗi khi tải dữ liệu loại xe.' });
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        fetchAll();
    }, [fetchAll]);

    const parsedLayout = useMemo(() => {
        if (!layoutMeta.rows) return null;
        return { ...layoutMeta, floors: floorData };
    }, [layoutMeta, floorData]);

    const handleSaveInfo = async () => {
        const trimmedName = name.trim();
        if (!trimmedName) {
            setError({ message: 'Tên loại xe không được để trống.' });
            return;
        }
        setSavingInfo(true);
        setError(null);
        try {
            await coachTypeApi.updateCoachTypeInfo(id, { coachTypeName: trimmedName, isActive });
            setEditInfo(false);
            await fetchAll();
        } catch (err) {
            const data = err.response?.data;
            if (data?.code === 'COACH_TYPE_HAS_ACTIVE_COACHES') {
                setDeactivationData(data.details);
                setDeactivationOpen(true);
                setIsActive(true);
            } else {
                setError({
                    message: data?.message || 'Có lỗi khi cập nhật thông tin.',
                    fieldErrors: data?.fieldErrors || null,
                });
            }
        } finally {
            setSavingInfo(false);
        }
    };

    const handleActiveToggle = async (checked) => {
        if (!checked && detail?.isActive) {
            try {
                const check = await coachTypeApi.getDeactivationCheck(id);
                if (!check.canDeactivate) {
                    setDeactivationData(check);
                    setDeactivationOpen(true);
                    return;
                }
            } catch (err) {
                setError({ message: err.response?.data?.message || 'Không thể kiểm tra điều kiện tắt loại xe.' });
                return;
            }
        }
        setIsActive(checked);
    };

    const handleAddPrice = async (e) => {
        e.preventDefault();
        setSavingPrice(true);
        setError(null);
        try {
            const payload = {
                seatPrice: Number(priceForm.seatPrice),
                startEffectiveDate: priceForm.startEffectiveDate,
            };
            await coachTypeApi.addPrice(id, payload);
            setPriceForm({ seatPrice: '', startEffectiveDate: '' });
            const pricesRes = await coachTypeApi.getPriceTimeline(id);
            setPrices(pricesRes);
            const detailRes = await coachTypeApi.getCoachTypeDetail(id);
            setDetail(detailRes);
        } catch (err) {
            const data = err.response?.data;
            setError({
                message: data?.message || 'Có lỗi khi thêm mức giá.',
                fieldErrors: data?.fieldErrors || null,
            });
        } finally {
            setSavingPrice(false);
        }
    };

    const handleMatrixChange = (floorIndex, newMatrix) => {
        setFloorData((prev) => {
            const next = [...prev];
            next[floorIndex] = [...newMatrix];
            return next;
        });
    };

    const SEAT_LAYOUT_LIMITS = { totalFloors: 2, rows: 11, cols: 5 };
    const handleLayoutDimensionChange = (e) => {
        const { name, value } = e.target;
        if (value === '') {
            setLayoutMeta((prev) => ({ ...prev, [name]: '' }));
            return;
        }
        let numValue = parseInt(value, 10);
        if (isNaN(numValue)) return;
        if (numValue < 1) numValue = 1;
        if (numValue > SEAT_LAYOUT_LIMITS[name]) numValue = SEAT_LAYOUT_LIMITS[name];
        setLayoutMeta((prev) => ({ ...prev, [name]: numValue }));
    };
    useEffect(() => {
        if (!editSeatMap) return;
        if (!layoutMeta.rows || !layoutMeta.cols || !layoutMeta.totalFloors) return;
        setFloorData((prevData) => {
            const newFloorData = Array(layoutMeta.totalFloors).fill().map((_, floorIndex) => {
                const oldMatrix = prevData[floorIndex];
                if (oldMatrix && oldMatrix.length === layoutMeta.rows && oldMatrix[0]?.length === layoutMeta.cols) {
                    return oldMatrix;
                }
                return Array(layoutMeta.rows).fill().map(() => Array(layoutMeta.cols).fill("SEAT"));
            });
            return newFloorData;
        });
    }, [editSeatMap, layoutMeta.totalFloors, layoutMeta.rows, layoutMeta.cols]);
    const currentEditSeatCount = useMemo(() => {
        if (!editSeatMap) return 0;
        let count = 0;
        floorData.forEach((matrix) => {
            matrix.forEach((row) => {
                row.forEach((cell) => {
                    if (cell === 'SEAT') count++;
                });
            });
        });
        return count;
    }, [editSeatMap, floorData]);

    const handleSaveSeatMap = async () => {
        setError(null);

        if (!layoutMeta.rows || !layoutMeta.cols || !layoutMeta.totalFloors) {
            setError({ message: 'Vui lòng nhập đầy đủ số tầng, số hàng và số cột!' });
            return;
        }
        if (currentEditSeatCount === 0) {
            setError({ message: 'Vui lòng giữ lại ít nhất 1 vị trí ghế ngồi trên sơ đồ!' });
            return;
        }
        setSavingSeatMap(true);

        try {
            const layoutPayload = {
                totalFloors: layoutMeta.totalFloors,
                rows: layoutMeta.rows,
                cols: layoutMeta.cols,
                floors: floorData,
            };
            await coachTypeApi.updateCoachTypeSeatMap(id, {
                seatLayout: JSON.stringify(layoutPayload),
            });
            setEditSeatMap(false);
            await fetchAll();
        } catch (err) {
            const data = err.response?.data;
            setError({
                message: data?.message || 'Có lỗi khi cập nhật sơ đồ ghế.',
                fieldErrors: data?.fieldErrors || null,
            });
        } finally {
            setSavingSeatMap(false);
        }
    };

    if (loading) {
        return (
            <Container fluid className="py-5 text-center">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2 text-secondary">Đang tải...</p>
            </Container>
        );
    }

    if (!detail) {
        return (
            <Container fluid className="py-4">
                <Alert variant="danger">{error || 'Không tìm thấy loại xe.'}</Alert>
            </Container>
        );
    }

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            <Button
                variant="link"
                onClick={() => navigate('/management/coach-types')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18} /> Quay lại danh sách
            </Button>

            <div className="d-flex justify-content-between align-items-start mb-4 flex-wrap gap-2">
                <div>
                    <h2 className="fw-bold text-dark mb-1">{detail.coachTypeName}</h2>
                    <span className="text-secondary">Mã loại xe #{detail.coachTypeId}</span>
                </div>
            </div>

            <Tab.Container activeKey={activeTab} onSelect={(k) => setActiveTab(k || 'info')}>
                <Nav variant="tabs" className="mb-3">
                    <Nav.Item>
                        <Nav.Link eventKey="info">Thông tin</Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                        <Nav.Link eventKey="prices">Giá vé</Nav.Link>
                    </Nav.Item>
                    <Nav.Item>
                        <Nav.Link eventKey="seatmap">Sơ đồ ghế</Nav.Link>
                    </Nav.Item>
                </Nav>

                <Tab.Content>
                    <Tab.Pane eventKey="info">
                        <Card className="shadow-sm border-0">
                            <Card.Body className="p-4">
                                <div className="d-flex justify-content-between align-items-center mb-3">
                                    <h5 className="fw-bold mb-0">Thông tin cơ bản</h5>
                                    {!editInfo ? (
                                        <Button size="sm" variant="outline-primary" onClick={() => setEditInfo(true)}>
                                            <BsPencilFill className="me-1" /> Chỉnh sửa
                                        </Button>
                                    ) : (
                                        <div className="d-flex gap-2">
                                            <Button size="sm" variant="outline-secondary" onClick={() => {
                                                setEditInfo(false);
                                                setName(detail.coachTypeName);
                                                setIsActive(detail.isActive);
                                            }}>
                                                Hủy
                                            </Button>
                                            <Button size="sm" className="custom-btn-general" disabled={savingInfo} onClick={handleSaveInfo}>
                                                {savingInfo ? 'Đang lưu...' : 'Lưu'}
                                            </Button>
                                        </div>
                                    )}
                                </div>
                                <Row className="gy-3">
                                    <Col md={6}>
                                        <p className="text-secondary small mb-1">Tên loại xe</p>
                                        {editInfo ? (
                                            <Form.Control value={name} onChange={(e) => setName(e.target.value)} maxLength={100} />
                                        ) : (
                                            <p className="fw-medium mb-0">{detail.coachTypeName}</p>
                                        )}
                                    </Col>
                                    <Col md={6}>
                                        <p className="text-secondary small mb-1">Tổng số ghế</p>
                                        <p className="fw-medium mb-0">{detail.totalSeat} ghế</p>
                                    </Col>
                                    <Col md={6}>
                                        <p className="text-secondary small mb-1">Số xe đang sử dụng</p>
                                        <p className="fw-medium mb-0">{detail.activeCoachCount ?? 0} xe</p>
                                    </Col>
                                    <Col md={6}>
                                        <p className="text-secondary small mb-1">Giá đang áp dụng</p>
                                        <p className="fw-bold text-primary mb-0">
                                            {!detail.currentPrice ? '---' : formatCurrency(detail.currentPrice)}
                                            {detail.currentPriceEffectiveFrom && (
                                                <span className="text-secondary fw-normal small ms-2">
                                                    (từ {new Date(detail.currentPriceEffectiveFrom).toLocaleString('vi-VN')})
                                                </span>
                                            )}
                                        </p>
                                    </Col>
                                    <Col md={12}>
                                        <p className="text-secondary small mb-1">Trạng thái hệ thống</p>
                                        {editInfo ? (
                                            <div className="d-flex align-items-center gap-3 p-3 bg-light border rounded">
                                                <Form.Check
                                                    type="switch"
                                                    checked={isActive}
                                                    onChange={(e) => handleActiveToggle(e.target.checked)}
                                                    label="Cho phép hoạt động"
                                                />
                                            </div>
                                        ) : (
                                            <Badge bg={detail.isActive ? 'success' : 'secondary'}>
                                                {detail.isActive ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                                            </Badge>
                                        )}
                                    </Col>
                                </Row>
                            </Card.Body>
                        </Card>
                    </Tab.Pane>

                    <Tab.Pane eventKey="prices">
                        <Card className="shadow-sm border-0 mb-4">
                            <Card.Body className="p-4">
                                <h5 className="fw-bold mb-3">Thêm mức giá mới</h5>
                                <Alert variant="light" className="border small mb-3 py-2">
                                    Mỗi mức giá áp dụng từ ngày bắt đầu cho đến khi có mức giá kế tiếp.
                                    Muốn đổi giá theo từng giai đoạn? Thêm lần lượt 2 mốc với ngày bắt đầu khác nhau
                                    (ví dụ: giá A từ 01/01, giá B từ 01/03 — khi đó giá A chỉ còn hiệu lực đến 01/03).
                                </Alert>
                                <Form onSubmit={handleAddPrice}>
                                    <Row className="g-3 align-items-start">
                                        <Col md={6}>
                                            <Form.Label className="small fw-semibold">Giá vé (VNĐ)</Form.Label>
                                            <InputGroup>
                                                <Form.Control
                                                    type="number"
                                                    required
                                                    min="0"
                                                    max="100000000"
                                                    value={priceForm.seatPrice}
                                                    onChange={(e) => setPriceForm((p) => ({ ...p, seatPrice: e.target.value }))}
                                                />
                                                <InputGroup.Text>VNĐ</InputGroup.Text>
                                            </InputGroup>
                                        </Col>
                                        <Col md={6}>
                                            <Form.Label className="small fw-semibold">Áp dụng từ</Form.Label>
                                            <Form.Control
                                                type="datetime-local"
                                                required
                                                min={getMinDateTime()}
                                                value={priceForm.startEffectiveDate}
                                                onChange={(e) => setPriceForm((p) => ({ ...p, startEffectiveDate: e.target.value }))}
                                            />
                                            <Form.Text className="text-muted">
                                                Ngày kết thúc được hệ thống gán tự động khi có mốc giá sau.
                                            </Form.Text>
                                        </Col>
                                        <Col md={12}>
                                            <Button type="submit" disabled={savingPrice} className="custom-btn-general">
                                                {savingPrice ? 'Đang lưu...' : 'Thêm mức giá'}
                                            </Button>
                                        </Col>
                                    </Row>
                                </Form>
                            </Card.Body>
                        </Card>

                        <Card className="shadow-sm border-0">
                            <Card.Body className="p-0">
                                <Table responsive hover className="mb-0 align-middle">
                                    <thead className="table-light">
                                        <tr>
                                            <th className="px-3 py-3">Giá vé</th>
                                            <th className="px-3 py-3">Từ ngày</th>
                                            <th className="px-3 py-3">Đến ngày</th>
                                            <th className="px-3 py-3">Trạng thái</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {prices.length === 0 ? (
                                            <tr>
                                                <td colSpan="4" className="text-center p-4 text-muted">Chưa có mốc giá</td>
                                            </tr>
                                        ) : (
                                            prices.map((p) => (
                                                <tr key={p.coachTypePriceId}>
                                                    <td className="px-3 fw-bold text-primary">{formatCurrency(p.seatPrice)}</td>
                                                    <td className="px-3">{new Date(p.startEffectiveDate).toLocaleString('vi-VN')}</td>
                                                    <td className="px-3">{formatPriceEndDate(p.endEffectiveDate)}</td>
                                                    <td className="px-3">
                                                        <Badge bg={PRICE_STATUS_LABELS[p.status]?.bg || 'secondary'}>
                                                            {PRICE_STATUS_LABELS[p.status]?.text || p.status}
                                                        </Badge>
                                                    </td>
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </Table>
                            </Card.Body>
                        </Card>
                    </Tab.Pane>

                    <Tab.Pane eventKey="seatmap">
                        <Card className="shadow-sm border-0">
                            <Card.Body className="p-4">
                                <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                                    <div>
                                        <h5 className="fw-bold mb-1">Sơ đồ không gian xe</h5>
                                        {!editSeatMap ? (
                                            parsedLayout && (
                                                <span className="text-secondary small">
                                                    {parsedLayout.totalFloors} tầng × {parsedLayout.rows} hàng × {parsedLayout.cols} cột
                                                </span>
                                            )
                                        ) : (
                                            <span className="text-secondary small">
                                                Điều chỉnh kích thước sơ đồ bên dưới, sau đó click vào ô để đánh dấu khoang trống.
                                            </span>
                                        )}
                                    </div>
                                    {!editSeatMap ? (
                                        detail.canEditSeatLayout ? (
                                            // TRẠNG THÁI 1: Cho phép sửa bình thường
                                            <Button size="sm" variant="outline-primary" onClick={() => setEditSeatMap(true)}>
                                                <BsPencilFill className="me-1" /> Chỉnh sửa sơ đồ
                                            </Button>
                                        ) : (
                                            // TRẠNG THÁI 2: Không cho sửa -> Disable + Hiện Tooltip khi hover
                                            <OverlayTrigger
                                                placement="left"
                                                overlay={
                                                    <Tooltip id="disabled-edit-tooltip">
                                                        Không thể sửa sơ đồ vì loại xe này đã được đưa vào sử dụng thực tế.
                                                    </Tooltip>
                                                }
                                            >
                                                {/* Cần bọc ngoài bằng thẻ span vì Button bị disabled sẽ nuốt mất sự kiện hover của Tooltip */}
                                                <span className="d-inline-block">
                                                    <Button size="sm" variant="outline-secondary" disabled style={{ pointerEvents: 'none' }}>
                                                        <BsPencilFill className="me-1" /> Chỉnh sửa sơ đồ
                                                    </Button>
                                                </span>
                                            </OverlayTrigger>
                                        )
                                    ) : (
                                        <div className="d-flex gap-2">
                                            <Button size="sm" variant="outline-secondary" onClick={() => {
                                                setEditSeatMap(false);
                                                fetchAll();
                                            }}>
                                                Hủy
                                            </Button>
                                            <Button size="sm" className="custom-btn-general" disabled={savingSeatMap} onClick={handleSaveSeatMap}>
                                                <BsCheckCircle className="me-1" />
                                                {savingSeatMap ? 'Đang lưu...' : 'Lưu sơ đồ'}
                                            </Button>
                                        </div>
                                    )}
                                </div>

                                {editSeatMap && (
                                    <Row className="g-3 mb-4">
                                        <Col md={4}>
                                            <Form.Label className="small fw-semibold">Số tầng (1-2)</Form.Label>
                                            <Form.Control
                                                type="number"
                                                name="totalFloors"
                                                min={1}
                                                max={2}
                                                value={layoutMeta.totalFloors}
                                                onChange={handleLayoutDimensionChange}
                                            />
                                        </Col>
                                        <Col md={4}>
                                            <Form.Label className="small fw-semibold">Số hàng (1-11)</Form.Label>
                                            <Form.Control
                                                type="number"
                                                name="rows"
                                                min={1}
                                                max={11}
                                                value={layoutMeta.rows}
                                                onChange={handleLayoutDimensionChange}
                                            />
                                        </Col>
                                        <Col md={4}>
                                            <Form.Label className="small fw-semibold">Số cột (1-5)</Form.Label>
                                            <Form.Control
                                                type="number"
                                                name="cols"
                                                min={1}
                                                max={5}
                                                value={layoutMeta.cols}
                                                onChange={handleLayoutDimensionChange}
                                            />
                                        </Col>
                                        <Col md={12}>
                                            <div className="bg-light p-3 rounded border">
                                                <span className="fw-medium text-dark">Tổng số ghế thiết lập: </span>
                                                <span className="text-success fs-5 fw-bold mx-1">{currentEditSeatCount}</span>
                                                <span className="fw-medium text-dark">ghế</span>
                                            </div>
                                        </Col>
                                    </Row>
                                )}

                                <div className="d-flex justify-content-center gap-2 overflow-auto bg-light rounded border p-4">
                                    {parsedLayout?.floors?.map((matrix, index) => (
                                        <div key={index} className="text-center">
                                            <p className="mb-2 fw-medium">Tầng {index + 1}</p>
                                            <div className="border border-secondary rounded p-3 bg-white shadow-sm">
                                                <SeatMapBuilder
                                                    mode={editSeatMap ? 'EDIT' : 'VIEW'}
                                                    rows={parsedLayout.rows}
                                                    cols={parsedLayout.cols}
                                                    initialMatrix={matrix}
                                                    onChange={editSeatMap ? (m) => handleMatrixChange(index, m) : undefined}
                                                />
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </Card.Body>
                        </Card>
                    </Tab.Pane>
                </Tab.Content>
            </Tab.Container>

            <CoachTypeDeactivationDialog
                isOpen={deactivationOpen}
                coachTypeId={id}
                checkData={deactivationData}
                onClose={() => setDeactivationOpen(false)}
            />

            <ToastContainer position="top-end" className="p-3" style={{ position: 'fixed', zIndex: 9999 }}>
                <Toast show={!!error} onClose={() => setError(null)} delay={9000} autohide bg="danger" text="white">
                    <Toast.Header closeButton className="bg-danger text-white border-0">
                        <strong className="me-auto d-inline-flex align-items-center gap-2">
                            <BsExclamationTriangleFill /><span>Có lỗi xảy ra</span>
                        </strong>
                    </Toast.Header>
                    <Toast.Body className="bg-white text-dark rounded-bottom">
                        <p className={`fw-semibold mb-2 ${error?.fieldErrors ? 'text-danger' : 'mb-0'}`}>
                            {error?.message}
                        </p>
                        {error?.fieldErrors && (
                            <ul className="mb-0 ps-3 text-danger" style={{ fontSize: '0.85rem' }}>
                                {[...new Set(Object.values(error.fieldErrors))].map((msg, i) => (
                                    <li key={i}>{msg}</li>
                                ))}
                            </ul>
                        )}
                    </Toast.Body>
                </Toast>
            </ToastContainer>
        </Container>
    );
}
