import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { routeStopApi } from '../../../features/routes/api/routeStopApi';
import { routeApi } from '../../../features/routes/api/routeApi';
import { coachStopApi } from '../../../features/coachStops/api/coachStopApi';
import { Alert, Button, Card, Col, Container, Form, Row } from 'react-bootstrap';

export default function RouteStopAddPage() {
    const navigate = useNavigate();
    const { routeId } = useParams();

    const [coachStops, setCoachStops] = useState([]);
    const [existingOrders, setExistingOrders] = useState([]);
    const [routeTotalKilometers, setRouteTotalKilometers] = useState(0);
    const [routeTotalMinutes, setRouteTotalMinutes] = useState(0);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const routeData = await routeApi.getRouteDetail(routeId);
                const existingStopIds = (routeData.routeStops || []).map(rs => rs.stopPointId);
                setExistingOrders((routeData.routeStops || []).map(rs => rs.stopOrder));
                setRouteTotalKilometers(routeData.totalKilometers || 0);
                setRouteTotalMinutes(routeData.totalMinutes || 0);

                // Fetch all coach stops to show in dropdown
                const data = await coachStopApi.getAllCoachStops('', true, 0, 1000);
                const allStops = data.content || [];
                const availableStops = allStops.filter(stop => !existingStopIds.includes(stop.stopPointId));
                setCoachStops(availableStops);
            } catch (err) {
                console.error("Lỗi khi tải dữ liệu:", err);
            }
        };
        fetchData();
    }, [routeId]);

    const [formData, setFormData] = useState({
        stopPointId: '',
        stopOrder: '',
        kilometersFromStart: '',
        minutesFromStart: ''
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        if (!formData.stopPointId || formData.stopOrder === '' || formData.kilometersFromStart === '' || formData.minutesFromStart === '') {
            setErrorMsg('Vui lòng nhập đầy đủ thông tin bắt buộc!');
            return;
        }

        if (existingOrders.includes(Number(formData.stopOrder))) {
            setErrorMsg('Thứ tự dừng này đã tồn tại trong tuyến xe. Vui lòng chọn thứ tự khác!');
            return;
        }

        if (Number(formData.kilometersFromStart) > Number(routeTotalKilometers)) {
            setErrorMsg(`Khoảng cách không thể vượt quá tổng khoảng cách của tuyến xe (${routeTotalKilometers} km).`);
            return;
        }

        if (Number(formData.minutesFromStart) > Number(routeTotalMinutes)) {
            setErrorMsg(`Thời gian không thể vượt quá tổng thời gian của tuyến xe (${routeTotalMinutes} phút).`);
            return;
        }

        setIsSubmitting(true);

        try {
            const payload = {
                routeId: Number(routeId),
                stopPointId: Number(formData.stopPointId),
                stopOrder: Number(formData.stopOrder),
                kilometersFromStart: Number(formData.kilometersFromStart),
                minutesFromStart: Number(formData.minutesFromStart)
            };

            await routeStopApi.createRouteStop(payload);

            navigate('/management/routes');
        } catch (error) {
            console.error("Lỗi thêm điểm dừng vào tuyến:", error);
            setErrorMsg(error.response.data.message || 'Có lỗi xảy ra khi lưu vào hệ thống.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '800px' }}>

            <Button
                variant="link"
                onClick={() => navigate('/management/routes')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18} /> Quay lại danh sách
            </Button>

            <h2 className="mb-4 text-dark fw-bold">Thêm điểm dừng cho tuyến #{routeId}</h2>

            {errorMsg && (
                <Alert variant="danger" className="shadow-sm border-0 d-flex align-items-center gap-2">
                    <BsExclamationTriangleFill />
                    <span>{errorMsg}</span>
                </Alert>
            )}

            <Form onSubmit={handleSubmit}>
                <Card className="shadow-sm border-0 h-100">
                    <Card.Header className="bg-white border-bottom pt-3 pb-2">
                        <h5 className="fw-bold mb-0 text-dark">Thông tin điểm dừng</h5>
                    </Card.Header>
                    <Card.Body className="p-4 d-flex flex-column gap-3">

                        <Row className="g-3">
                            <Col md={12}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Điểm dừng <span className="text-danger">*</span></Form.Label>
                                    <Form.Select
                                        name="stopPointId"
                                        required
                                        value={formData.stopPointId}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    >
                                        <option value="" disabled>-- Chọn điểm dừng --</option>
                                        {coachStops.map(stop => (
                                            <option key={stop.stopPointId} value={stop.stopPointId}>
                                                {stop.stopPointName} - {stop.province}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                        </Row>

                        <Row className="g-3">
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Thứ tự dừng <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="stopOrder"
                                        required
                                        min="1"
                                        placeholder="Ví dụ: 1"
                                        value={formData.stopOrder}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Khoảng cách từ điểm xuất phát (km) <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="kilometersFromStart"
                                        required
                                        step="0.1"
                                        min="0"
                                        placeholder="Ví dụ: 50.5"
                                        value={formData.kilometersFromStart}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Thời gian từ điểm xuất phát (phút) <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="minutesFromStart"
                                        required
                                        min="0"
                                        placeholder="Ví dụ: 60"
                                        value={formData.minutesFromStart}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Button
                            type="submit"
                            disabled={isSubmitting}
                            className="w-100 py-3 mt-4 fw-medium d-flex justify-content-center align-items-center gap-2 fs-5 custom-btn-general"
                        >
                            <BsCheckCircle size={20} />
                            {isSubmitting ? 'Đang lưu hệ thống...' : 'Thêm điểm dừng'}
                        </Button>

                    </Card.Body>
                </Card>
            </Form>

        </Container>
    );
}
