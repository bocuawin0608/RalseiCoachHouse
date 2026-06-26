import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Col, Container, Form, Row } from 'react-bootstrap';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { tripApi } from '../../../features/trip';
import './TripCreatePage.css';

export default function TripCreatePage() {
    const navigate = useNavigate();

    /** Form state - lưu trữ thông tin chuyến xe */
    const [formData, setFormData] = useState({
        routeId: '',
        coachId: '',
        departureDate: '',
        departureTime: '',
        status: 'SCHEDULED',
        driverId: '',
        attendantId: ''
    });

    // --- STATE ĐỂ LƯU DANH SÁCH DROPDOWN ĐỘNG ---
    const [availableCoaches, setAvailableCoaches] = useState([]);
    const [availableDrivers, setAvailableDrivers] = useState([]);
    const [availableAttendants, setAvailableAttendants] = useState([]);
    
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isLoadingResources, setIsLoadingResources] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    /** Handler cập nhật state khi người dùng nhập liệu */
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    /** EFFECT: Tự động chạy ngầm truy vấn danh sách trống khi nhập xong Tuyến + Ngày + Giờ */
    useEffect(() => {
        const { routeId, departureDate, departureTime } = formData;

        // Chỉ kích hoạt lệnh gọi API khi cả 3 trường cốt lõi đã được điền hợp lệ
        if (routeId && departureDate && departureTime) {
            const fetchAvailableResources = async () => {
                setIsLoadingResources(true);
                setErrorMsg('');
                try {
                    // Ghép chuỗi chuẩn định dạng ISO-8601 gửi xuống Backend bóc tách
                    const combinedTime = `${departureDate}T${departureTime}:00`;
                    
                    // Kích hoạt đồng thời 3 API ngầm
                    const [coachesRes, driversRes, attendantsRes] = await Promise.all([
                        tripApi.getAvailableCoaches({ routeId: parseInt(routeId, 10), departureTime: combinedTime }),
                        tripApi.getAvailableDrivers({ departureTime: combinedTime }),
                        tripApi.getAvailableAttendants({ departureTime: combinedTime })
                    ]);

                    // Đổ dữ liệu trả về từ ResponseEntity vào các Dropdown tương ứng
                    setAvailableCoaches(coachesRes.data || []);
                    setAvailableDrivers(driversRes.data || []);
                    setAvailableAttendants(attendantsRes.data || []);
                } catch (err) {
                    console.error('Lỗi tự động tải tài nguyên:', err);
                    setErrorMsg('Không thể tự động cập nhật danh sách xe và nhân sự phù hợp cho lịch trình này.');
                } finally {
                    setIsLoadingResources(false);
                }
            };

            fetchAvailableResources();
        } else {
            // Nếu người dùng xóa bớt 1 trong 3 trường cốt lõi, xóa trắng dropdown phía sau
            setAvailableCoaches([]);
            setAvailableDrivers([]);
            setAvailableAttendants([]);
        }
    }, [formData.routeId, formData.departureDate, formData.departureTime]); // Đã sửa lỗi cú pháp thừa chữ ở đây!

    /** Kiểm tra điều kiện trước khi submit form chính thức */
    const validate = () => {
        if (!formData.routeId) return 'Vui lòng chọn tuyến đường!';
        if (!formData.departureDate) return 'Vui lòng chọn ngày khởi hành!';
        if (!formData.departureTime) return 'Vui lòng chọn giờ khởi hành!';
        if (!formData.coachId) return 'Vui lòng chọn xe khách từ danh sách!';
        if (!formData.driverId) return 'Vui lòng chọn tài xế từ danh sách!';
        if (!formData.attendantId) return 'Vui lòng chọn phụ xe từ danh sách!';
        return null;
    };

    /** Gửi payload cuối cùng lên endpoint tạo mới chuyến xe */
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
            const combinedDepartureTime = `${formData.departureDate}T${formData.departureTime}:00`;

            const payload = {
                coachId: parseInt(formData.coachId, 10),
                routeId: parseInt(formData.routeId, 10),
                departureTime: combinedDepartureTime, 
                status: formData.status,
                driverId: parseInt(formData.driverId, 10),
                attendantId: parseInt(formData.attendantId, 10)
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
                                <h5 className="fw-bold mb-0 text-dark">Thông tin lịch trình & Nhân sự</h5>
                            </Card.Header>
                            <Card.Body className="d-flex flex-column gap-3">

                                {/* 1. Tuyến đường (Dữ liệu cố định hoặc động) */}
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">
                                        Tuyến đường <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Select
                                        name="routeId"
                                        required
                                        value={formData.routeId}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    >
                                        <option value="">Chọn tuyến đường</option>
                                        <option value="1">Hà Nội - Quảng Bình</option>
                                        <option value="2">Quảng Bình - Hà Nội</option>
                                    </Form.Select>
                                </Form.Group>

                                {/* 2. Ngày khởi hành */}
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

                                {/* 3. Giờ khởi hành */}
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

                                <hr className="my-2" />
                                {isLoadingResources && <small className="text-primary fw-medium">Đang tìm xe và nhân sự trống lịch ngầm...</small>}

                                {/* 4. Xe khách (Hiện danh sách động từ API trả về) */}
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">
                                        Xe khách <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Select
                                        name="coachId"
                                        required
                                        value={formData.coachId}
                                        onChange={handleInputChange}
                                        disabled={!formData.routeId || !formData.departureDate || !formData.departureTime}
                                    >
                                        <option value="">-- Chọn xe trống lịch --</option>
                                        {availableCoaches.map(coach => (
                                            <option key={coach.id} value={coach.id}>
                                                {coach.licensePlate} ({coach.coachType})
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>

                                {/* 5. Tài xế (Hiện danh sách động từ API trả về) */}
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">
                                        Tài xế <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Select
                                        name="driverId"
                                        required
                                        value={formData.driverId}
                                        onChange={handleInputChange}
                                        disabled={!formData.routeId || !formData.departureDate || !formData.departureTime}
                                    >
                                        <option value="">-- Chọn tài xế sẵn sàng --</option>
                                        {availableDrivers.map(driver => (
                                            <option key={driver.id} value={driver.id}>
                                                {driver.fullName}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>

                                {/* 6. Phụ xe (Hiện danh sách động từ API trả về) */}
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">
                                        Phụ xe <span className="text-danger">*</span>
                                    </Form.Label>
                                    <Form.Select
                                        name="attendantId"
                                        required
                                        value={formData.attendantId}
                                        onChange={handleInputChange}
                                        disabled={!formData.routeId || !formData.departureDate || !formData.departureTime}
                                    >
                                        <option value="">-- Chọn phụ xe sẵn sàng --</option>
                                        {availableAttendants.map(attendant => (
                                            <option key={attendant.id} value={attendant.id}>
                                                {attendant.fullName}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>

                                {/* Trạng thái chuyến */}
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">
                                        Trạng thái chuyến
                                    </Form.Label>
                                    <Form.Select name="status" value={formData.status} onChange={handleInputChange}>
                                        <option value="SCHEDULED">Đã lên lịch</option>
                                        <option value="ACTIVE">Đang hoạt động</option>
                                        <option value="COMPLETED">Hoàn thành</option>
                                        <option value="CANCELLED">Đã hủy</option>
                                    </Form.Select>
                                </Form.Group>

                                {/* Submit Button */}
                                <Button
                                    type="submit"
                                    disabled={isSubmitting || isLoadingResources}
                                    className="trip-create-submit-btn custom-btn-general mt-2"
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