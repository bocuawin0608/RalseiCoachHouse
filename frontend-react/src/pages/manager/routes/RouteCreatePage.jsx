import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { routeApi } from '../../../features/routes/api/routeApi';
import { Alert, Button, Card, Col, Container, Form, Row } from 'react-bootstrap';

export default function RouteCreatePage() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        routeName: '',
        totalKilometers: '',
        totalMinutes: '',
        active: true
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        setIsSubmitting(true);

        try {
            const payload = {
                routeName: formData.routeName,
                totalKilometers: Number(formData.totalKilometers),
                totalMinutes: Number(formData.totalMinutes),
                active: formData.active
            };

            await routeApi.createRoute(payload);

            navigate('/manager/routes');
        } catch (error) {
            console.error("Lỗi tạo tuyến xe:", error);
            setErrorMsg(error.response?.data?.message || 'Có lỗi xảy ra khi lưu vào hệ thống.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '800px' }}>

            <Button
                variant="link"
                onClick={() => navigate('/manager/routes')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18} /> Quay lại danh sách
            </Button>

            <h2 className="mb-4 text-dark fw-bold">Thêm mới tuyến xe</h2>

            {errorMsg && (
                <Alert variant="danger" className="shadow-sm border-0 d-flex align-items-center gap-2">
                    <BsExclamationTriangleFill />
                    <span>{errorMsg}</span>
                </Alert>
            )}

            <Form onSubmit={handleSubmit}>
                <Card className="shadow-sm border-0">
                    <Card.Header className="bg-white border-bottom pt-3 pb-2">
                        <h5 className="fw-bold mb-0 text-dark">Thông tin cơ bản</h5>
                    </Card.Header>
                    <Card.Body className="p-4 d-flex flex-column gap-3">

                        <Form.Group>
                            <Form.Label className="fw-semibold text-secondary mb-1">Tên tuyến đường <span className="text-danger">*</span></Form.Label>
                            <Form.Control
                                type="text"
                                name="routeName"
                                required
                                maxLength={100}
                                placeholder="Ví dụ: Hà Nội - Sapa"
                                value={formData.routeName}
                                onChange={handleInputChange}
                                className="py-2"
                            />
                            <small className="text-muted">
                                Định dạng: "Điểm Đầu - Điểm Cuối"
                            </small>
                        </Form.Group>

                        <Row className="g-3">
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Khoảng cách (km) <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="totalKilometers"
                                        step="0.1"
                                        required
                                        min="0"
                                        placeholder="Ví dụ: 300"
                                        value={formData.totalKilometers}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Thời gian di chuyển (phút) <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="totalMinutes"
                                        required
                                        min="0"
                                        placeholder="Ví dụ: 240"
                                        value={formData.totalMinutes}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Form.Group className="mt-3">
                            <Form.Label className="fw-semibold text-secondary">Trạng thái ban đầu</Form.Label>
                            <div className="d-flex align-items-center justify-content-between p-3 bg-light border rounded">
                                <div className="d-flex align-items-center gap-3">
                                    <Form.Check
                                        type="switch"
                                        id="active-switch"
                                        name="active"
                                        checked={formData.active}
                                        onChange={handleInputChange}
                                        className="fs-5 m-0"
                                    />
                                    <span style={{ fontSize: '0.95rem' }} className="fw-medium text-dark">
                                        Kích hoạt ngay
                                    </span>
                                </div>
                                <span className={`badge px-3 py-2 ${formData.active ? 'bg-success' : 'bg-secondary'}`}>
                                    {formData.active ? 'Sẽ hoạt động' : 'Chưa hoạt động'}
                                </span>
                            </div>
                        </Form.Group>

                        <Button
                            type="submit"
                            variant="primary"
                            disabled={isSubmitting}
                            className="w-100 py-3 mt-4 fw-medium d-flex justify-content-center align-items-center gap-2 fs-5"
                        >
                            <BsCheckCircle size={20} />
                            {isSubmitting ? 'Đang lưu hệ thống...' : 'Lưu Tuyến Đường'}
                        </Button>

                    </Card.Body>
                </Card>
            </Form>

        </Container>
    );
}
