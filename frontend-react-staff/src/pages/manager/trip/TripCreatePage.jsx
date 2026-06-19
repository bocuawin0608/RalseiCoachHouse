import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Col, Container, Form, Row } from 'react-bootstrap';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { tripApi } from '../../../features/trip';
import './TripCreatePage.css';

export default function TripCreatePage() {
    const navigate = useNavigate();

    /** Form state - fields align with the BE TripUpdateRequest / Trip model */
    const [formData, setFormData] = useState({
        coachId: '',
        departureDate: '',
        departureTime: '',
        routeId: '',
        tripStatus: 'SCHEDULED'
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    /** Generic handler for all controlled inputs */
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    /** Validate required fields before submitting */
    const validate = () => {
        if (!formData.coachId) return 'Vui lòng nhập ID xe khách!';
        if (!formData.routeId) return 'Vui lòng nhập ID tuyến đường!';
        if (!formData.departureDate) return 'Vui lòng chọn ngày khởi hành!';
        if (!formData.departureTime) return 'Vui lòng chọn giờ khởi hành!';
        return null;
    };

    /** Build payload and POST to the create endpoint */
    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        const validationError = validate();
        if (validationError) {
            setErrorMsg(validationError);
            return;
        }

        setIsSubmitting(true);
        try {
            /**
             * Spring LocalTime deserializer requires strict HH:mm:ss format.
             * The <input type="time"> returns "HH:mm"; append ":00" for seconds.
             * Guard against empty string to avoid a parse error at index 0.
             */
            const timeValue = formData.departureTime
                ? `${formData.departureTime}:00`
                : null;

            const payload = {
                coachId: parseInt(formData.coachId),
                routeId: parseInt(formData.routeId),
                departureDate: formData.departureDate,
                departureTime: timeValue,
                tripStatus: formData.tripStatus
            };

            await tripApi.createTrip(payload);
            navigate('/management/trips');
        } catch (err) {
            console.error('Lỗi tạo chuyến xe:', err);
            setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi lưu vào hệ thống.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>

            {/* Back navigation */}
            <Button
                variant="link"
                onClick={() => navigate('/management/trips')}
                className="trip-create-back-btn"
            >
                <BsArrowLeft size={18} /> Quay lại danh sách
            </Button>

            <h2 className="trip-create-title">Thêm mới chuyến xe</h2>

            {/* Error banner */}
            {errorMsg && (
                <Alert variant="danger" className="shadow-sm border-0 d-flex align-items-center gap-2">
                    <BsExclamationTriangleFill />
                    <span>{errorMsg}</span>
                </Alert>
            )}

            <Form onSubmit={handleSubmit}>
                <Row className="g-4 justify-content-center">
                    <Col lg={6} md={12}>
                        <Card className="trip-create-card">
                            <Card.Header className="trip-create-card-header">
                                <h5 className="fw-bold mb-0 text-dark">Thông tin cơ bản</h5>
                            </Card.Header>
                            <Card.Body className="d-flex flex-column gap-3">

                                {/* Coach ID */}
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">
                                        ID Xe khách <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="coachId"
                                        required
                                        min="1"
                                        placeholder="Ví dụ: 5"
                                        value={formData.coachId}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>

                                {/* Route ID */}
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">
                                        ID Tuyến đường <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control
                                        type="number"
                                        name="routeId"
                                        required
                                        min="1"
                                        placeholder="Ví dụ: 3"
                                        value={formData.routeId}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>

                                {/* Departure Date */}
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">
                                        Ngày khởi hành <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control
                                        type="date"
                                        name="departureDate"
                                        required
                                        value={formData.departureDate}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>

                                {/* Departure Time */}
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">
                                        Giờ khởi hành <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Control
                                        type="time"
                                        name="departureTime"
                                        required
                                        value={formData.departureTime}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>

                                {/* Trip Status */}
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">
                                        Trạng thái chuyến
                                    </Form.Label>
                                    <Form.Select
                                        name="tripStatus"
                                        value={formData.tripStatus}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    >
                                        <option value="SCHEDULED">Đã lên lịch</option>
                                        <option value="ACTIVE">Đang hoạt động</option>
                                        <option value="COMPLETED">Hoàn thành</option>
                                        <option value="CANCELLED">Đã hủy</option>
                                    </Form.Select>
                                </Form.Group>

                                {/* Submit */}
                                <Button
                                    type="submit"
                                    disabled={isSubmitting}
                                    className="trip-create-submit-btn custom-btn-general"
                                >
                                    <BsCheckCircle size={18} />
                                    {isSubmitting ? 'Đang lưu hệ thống...' : 'Lưu & Kích hoạt'}
                                </Button>

                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </Form>

        </Container>
    );
}
