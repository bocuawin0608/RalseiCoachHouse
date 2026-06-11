import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { coachStopApi } from '../../../features/routes/api/coachStopApi';
import { Alert, Button, Card, Col, Container, Form, Row } from 'react-bootstrap';

export default function CoachStopCreatePage() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        stopPointName: '',
        address: '',
        city: '',
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
                stopPointName: formData.stopPointName,
                address: formData.address,
                city: formData.city,
                active: formData.active
            };

            await coachStopApi.createCoachStop(payload);

            navigate('/manager/coach-stops');
        } catch (error) {
            console.error("Lỗi tạo điểm dừng:", error);
            setErrorMsg(error.response?.data?.message || 'Có lỗi xảy ra khi lưu vào hệ thống.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '800px' }}>

            <Button
                variant="link"
                onClick={() => navigate('/manager/coach-stops')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18} /> Quay lại danh sách
            </Button>

            <h2 className="mb-4 text-dark fw-bold">Thêm mới điểm dừng</h2>

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
                            <Form.Label className="fw-semibold text-secondary mb-1">Tên điểm dừng <span className="text-danger">*</span></Form.Label>
                            <Form.Control
                                type="text"
                                name="stopPointName"
                                required
                                maxLength={100}
                                placeholder="Ví dụ: Bến xe Mỹ Đình"
                                value={formData.stopPointName}
                                onChange={handleInputChange}
                                className="py-2"
                            />
                        </Form.Group>

                        <Row className="g-3">
                            <Col md={12}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Địa chỉ chi tiết <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="address"
                                        required
                                        maxLength={200}
                                        placeholder="Ví dụ: 20 Phạm Hùng, Mỹ Đình, Nam Từ Liêm"
                                        value={formData.address}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                    <small className="text-muted">
                                        Định dạng: "Số nhà tên đường phố, huyện, tỉnh"
                                    </small>
                                </Form.Group>
                            </Col>
                        </Row>

                        <Row className="g-3 mt-1">
                            <Col md={12}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Thành phố / Tỉnh <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="city"
                                        required
                                        maxLength={50}
                                        placeholder="Ví dụ: Hà Nội"
                                        value={formData.city}
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
                            {isSubmitting ? 'Đang lưu hệ thống...' : 'Lưu Điểm Dừng'}
                        </Button>

                    </Card.Body>
                </Card>
            </Form>

        </Container>
    );
}
