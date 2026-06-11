import { useEffect, useState } from "react";
import { coachStopApi } from "../api/coachStopApi";
import { Alert, Button, Col, Form, Modal, Row } from "react-bootstrap";
import { BsExclamationTriangleFill } from "react-icons/bs";

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

    useEffect(() => {
        if (isOpen && data) {
            setFormData({
                stopPointName: data.stopPointName || '',
                address: data.address || '',
                city: data.city || '',
                active: data.active !== undefined ? data.active : true
            });
            setError(null);
        } else {
            setFormData(INITIAL_FORM_DATA);
        }
    }, [data, isOpen]);

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
                await coachStopApi.disableCoachStop(data.stopPointId);
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
                    <Button type="submit" variant="primary" className="px-4 fw-medium" disabled={isSubmitting}>
                        {isSubmitting ? 'Đang xử lý...' : 'Lưu Thay Đổi'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
