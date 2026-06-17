import { useEffect, useState, useRef } from "react";
import { coachStopApi } from "../api/coachStopApi";
import { Alert, Button, Col, Form, Modal, Row, ListGroup, Spinner } from "react-bootstrap";
import { BsExclamationTriangleFill } from "react-icons/bs";
import axiosClient from "../../../api/axiosClient";
import { useDebounce } from "../../../hooks/useDebounce";

const INITIAL_FORM_DATA = {
    stopPointName: '',
    address: '',
    city: '',
    active: true
}

export default function CoachStopUpdateInfoModal({ isOpen, data, onClose, onSuccess }) {

    const [formData, setFormData] = useState(INITIAL_FORM_DATA);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const [predictions, setPredictions] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);
    const [isSearching, setIsSearching] = useState(false);
    const [selectedFromDropdown, setSelectedFromDropdown] = useState(false);

    const dropdownRef = useRef(null);
    const debouncedAddress = useDebounce(formData.address, 500);

    useEffect(() => {
        if (isOpen && data) {
            setSelectedFromDropdown(true); // Prevent fetching immediately on open
            setFormData({
                stopPointName: data.stopPointName || '',
                address: data.address || '',
                city: data.city || '',
                active: data.active !== undefined ? data.active : true
            });
            setError(null);
        } else {
            setFormData(INITIAL_FORM_DATA);
            setPredictions([]);
            setShowDropdown(false);
        }
    }, [data, isOpen]);

    // Click outside listener
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // Fetch predictions
    useEffect(() => {
        if (debouncedAddress && !selectedFromDropdown && isOpen) {
            fetchPredictions(debouncedAddress);
        } else if (selectedFromDropdown) {
            setSelectedFromDropdown(false);
        } else {
            setPredictions([]);
            setShowDropdown(false);
        }
    }, [debouncedAddress, isOpen]);

    const fetchPredictions = async (input) => {
        setIsSearching(true);
        try {
            const res = await axiosClient.get(`/v2/goong/place/autocomplete?input=${encodeURIComponent(input)}`);
            if (res && res.predictions) {
                setPredictions(res.predictions);
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
        setError(null);
        setIsSubmitting(true);

        try {
            const payload = {
                stopPointName: formData.stopPointName,
                address: formData.address,
                city: formData.city,
            };

            await coachStopApi.updateCoachStop(data.stopPointId, payload);

            if (formData.active && !data.active) {
                await coachStopApi.restoreCoachStop(data.stopPointId);
            } else if (!formData.active && data.active) {
                await coachStopApi.softDeleteCoachStop(data.stopPointId);
            }

            onSuccess();
            onClose();
        } catch (error) {
            console.error("Lỗi cập nhật điểm dừng:", error);
            setError(error.response?.data?.message || 'Có lỗi xảy ra khi lưu vào hệ thống.');
        } finally {
            setIsSubmitting(false);
        }
    }

    if (!isOpen) return null;

    return (
        <Modal show={isOpen} onHide={onClose} size="lg" centered backdrop="static">
            <Form onSubmit={handleSubmit}>
                <Modal.Header closeButton>
                    <Modal.Title className="fs-5 fw-bold text-primary">
                        Cập nhật điểm dừng: #{data?.stopPointId}
                    </Modal.Title>
                </Modal.Header>

                <Modal.Body className="px-4 py-4 d-flex flex-column gap-3">
                    {error && (
                        <Alert variant="danger" className="mb-2 py-3 d-flex align-items-center gap-2">
                            <BsExclamationTriangleFill />
                            <span>{error}</span>
                        </Alert>
                    )}

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
                                <Form.Label className="fw-semibold text-secondary mb-1">Thành Phố / Tỉnh <span className="text-danger">*</span></Form.Label>
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
                        <Form.Label className="fw-semibold text-secondary">Trạng thái hoạt động</Form.Label>
                        <div className="d-flex align-items-center justify-content-between p-3 bg-light border rounded">
                            <div className="d-flex align-items-center gap-3">
                                <Form.Check
                                    type="switch"
                                    id="active-switch-update"
                                    name="active"
                                    checked={formData.active}
                                    onChange={handleInputChange}
                                    className="fs-5 m-0"
                                />
                                <span style={{ fontSize: '0.95rem' }} className="fw-medium text-dark">
                                    Cho phép hoạt động
                                </span>
                            </div>
                            <span className={`badge px-3 py-2 ${formData.active ? 'bg-success' : 'bg-secondary'}`}>
                                {formData.active ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                            </span>
                        </div>
                    </Form.Group>

                </Modal.Body>

                <Modal.Footer className="bg-light border-0 rounded-bottom">
                    <Button variant="outline-secondary" onClick={onClose} className="px-4 fw-medium" disabled={isSubmitting}>
                        Hủy bỏ
                    </Button>
                    <Button type="submit" className="px-4 fw-medium custom-btn-general" disabled={isSubmitting}>
                        {isSubmitting ? 'Đang xử lý...' : 'Lưu Thay Đổi'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
