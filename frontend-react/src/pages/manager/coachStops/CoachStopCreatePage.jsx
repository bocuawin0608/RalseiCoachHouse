import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { coachStopApi } from '../../../features/coachStops';
import { Alert, Button, Card, Col, Container, Form, Row, ListGroup, Spinner } from 'react-bootstrap';
import axios from 'axios';
import { useDebounce } from '../../../hooks/useDebounce';

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

    const [predictions, setPredictions] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [isSearching, setIsSearching] = useState(false);
    const [selectedFromDropdown, setSelectedFromDropdown] = useState(false);

    const dropdownRef = useRef(null);
    const debouncedAddress = useDebounce(formData.address, 500);

    useEffect(() => {
        if (debouncedAddress && !selectedFromDropdown) {
            fetchPredictions(debouncedAddress);
        } else if (selectedFromDropdown) {
            setSelectedFromDropdown(false);
        } else {
            setPredictions([]);
            setShowDropdown(false);
        }
    }, [debouncedAddress]);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const fetchPredictions = async (input) => {
        setIsSearching(true);
        try {
            const apiKey = import.meta.env.VITE_GOONG_API_KEY;
            if (!apiKey) return;
            const res = await axios.get(`https://rsapi.goong.io/v2/place/autocomplete?api_key=${apiKey}&input=${encodeURIComponent(input)}`);
            if (res.data && res.data.predictions) {
                setPredictions(res.data.predictions);
                setShowDropdown(true);
            }
        } catch (err) {
            console.error("Goong autocomplete error:", err);
        } finally {
            setIsSearching(false);
        }
    };

    const handleSelectAddress = (prediction) => {
        const fullAddress = prediction.description;
        const parts = fullAddress.split(',');

        let cityPart = '';
        let addressPart = fullAddress;

        if (parts.length > 1) {
            cityPart = parts.pop().trim();
            addressPart = parts.join(',').trim();
        }

        setSelectedFromDropdown(true);
        setFormData(prev => ({
            ...prev,
            address: addressPart,
            city: cityPart
        }));
        setShowDropdown(false);
    };

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

            navigate('/management/coach-stops');
        } catch (error) {
            console.error("Lỗi tạo điểm dừng:", error);
            setErrorMsg(error.response.data.message || 'Có lỗi xảy ra khi lưu vào hệ thống.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '800px' }}>

            <Button
                variant="link"
                onClick={() => navigate('/management/coach-stops')}
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
                                    <div className="position-relative" ref={dropdownRef}>
                                        <Form.Control
                                            type="text"
                                            name="address"
                                            required
                                            maxLength={200}
                                            placeholder="Nhập địa chỉ để tìm kiếm..."
                                            value={formData.address}
                                            onChange={handleInputChange}
                                            onFocus={() => {
                                                if (predictions.length > 0) setShowDropdown(true);
                                            }}
                                            className="py-2"
                                            autoComplete="off"
                                        />

                                        {showDropdown && predictions.length > 0 && (
                                            <ListGroup className="position-absolute w-100 shadow-lg mt-1" style={{ zIndex: 1000, maxHeight: '250px', overflowY: 'auto' }}>
                                                {predictions.map((p) => (
                                                    <ListGroup.Item
                                                        key={p.place_id}
                                                        action
                                                        onClick={() => handleSelectAddress(p)}
                                                        className="py-2 px-3 text-start"
                                                    >
                                                        <div className="fw-medium text-dark">{p.structured_formatting?.main_text || p.description}</div>
                                                        {p.structured_formatting?.secondary_text && (
                                                            <small className="text-muted">{p.structured_formatting.secondary_text}</small>
                                                        )}
                                                    </ListGroup.Item>
                                                ))}
                                            </ListGroup>
                                        )}
                                        {isSearching && (
                                            <div className="position-absolute end-0 top-50 translate-middle-y pe-3">
                                                <Spinner animation="border" size="sm" variant="secondary" />
                                            </div>
                                        )}
                                    </div>
                                    <small className="text-muted mt-1 d-block">
                                        Vui lòng chọn địa chỉ từ danh sách gợi ý để đảm bảo tính chính xác
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
