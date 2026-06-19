import { Alert, Button, Form, Modal } from 'react-bootstrap';
import { useState, useEffect } from 'react';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import { tripApi } from '../api/tripApi';
import './TripUpdateInfoModal.css';

/**
 * Modal for updating an existing trip's editable fields.
 * Controlled via isOpen/onClose props; calls onSuccess after a successful PUT.
 */
export default function TripUpdateInfoModal({ isOpen, data, onClose, onSuccess }) {
    const [formData, setFormData] = useState({
        departureDate: '',
        departureTime: '',
        tripStatus: ''
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    /**
     * Normalize a LocalTime value from the API into "HH:mm" for <input type="time">.
     * Jackson may deserialize LocalTime as an array [H, m, s] or a string "HH:mm:ss".
     */
    const normalizeTime = (value) => {
        if (!value) return '';
        // Array form: [8, 30, 0] → "08:30"
        if (Array.isArray(value)) {
            const [h, m] = value;
            return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
        }
        // String form: "08:30:00" or "08:30" → take first 5 chars
        return String(value).substring(0, 5);
    };

    /** Populate form fields whenever the target row or open state changes */
    useEffect(() => {
        if (data && isOpen) {
            setFormData({
                departureDate: data.departureDate ? String(data.departureDate).substring(0, 10) : '',
                departureTime: normalizeTime(data.departureTime),
                tripStatus: data.tripStatus || ''
            });
            setError(null);
        }
    }, [data, isOpen]);

    /** Detect whether the user has changed at least one field */
    const hasAnyChange = data && (
        String(data.departureDate).substring(0, 10) !== formData.departureDate ||
        String(data.departureTime).substring(0, 5) !== formData.departureTime ||
        data.tripStatus !== formData.tripStatus
    );

    /** Generic handler for all controlled form inputs */
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        setError(null);
    };

    /** Submit updated trip data to the API */
    const handleSubmit = async (e) => {
        e.preventDefault();

        // Skip API call when nothing changed
        if (!hasAnyChange) {
            onClose();
            return;
        }

        setIsSubmitting(true);
        setError(null);
        try {
            await tripApi.updateTrip(data.tripId, {
                departureDate: formData.departureDate,
                /**
                 * Spring LocalTime requires HH:mm:ss.
                 * Send null (omitted) rather than an empty string to avoid
                 * "could not be parsed at index 0" deserialization failure.
                 */
                departureTime: formData.departureTime
                    ? `${formData.departureTime}:00`
                    : null,
                tripStatus: formData.tripStatus
            });

            onSuccess();
            onClose();
        } catch (err) {
            setError(err.response?.data?.message || 'Có lỗi xảy ra khi cập nhật.');
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <Modal show={isOpen} onHide={onClose} centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Cập nhật thông tin chuyến xe
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4 pb-0">
                <Form id="update-trip-form" onSubmit={handleSubmit}>

                    {/* Departure Date */}
                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">
                            Ngày khởi hành <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="date"
                            name="departureDate"
                            value={formData.departureDate}
                            onChange={handleInputChange}
                            required
                            className="py-2"
                        />
                    </Form.Group>

                    {/* Departure Time */}
                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">
                            Giờ khởi hành <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="time"
                            name="departureTime"
                            value={formData.departureTime}
                            onChange={handleInputChange}
                            required
                            className="py-2"
                        />
                    </Form.Group>

                    {/* Trip Status */}
                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold text-secondary">
                            Trạng thái chuyến <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Select
                            name="tripStatus"
                            value={formData.tripStatus}
                            onChange={handleInputChange}
                            required
                            className="py-2"
                        >
                            <option value="" disabled>-- Chọn trạng thái --</option>
                            <option value="SCHEDULED">Đã lên lịch</option>
                            <option value="ACTIVE">Đang hoạt động</option>
                            <option value="COMPLETED">Hoàn thành</option>
                            <option value="CANCELLED">Đã hủy</option>
                        </Form.Select>
                    </Form.Group>

                    {/* Inline error alert */}
                    {error && (
                        <Alert variant="danger" className="mb-3 py-2 px-3 border-0 d-flex align-items-center gap-2">
                            <BsExclamationTriangleFill />
                            <span>{error}</span>
                        </Alert>
                    )}
                </Form>
            </Modal.Body>

            <Modal.Footer className="trip-modal-footer">
                <Button
                    variant="outline-secondary"
                    onClick={onClose}
                    disabled={isSubmitting}
                    className="trip-modal-cancel-btn"
                >
                    Hủy bỏ
                </Button>
                <Button
                    type="submit"
                    form="update-trip-form"
                    disabled={isSubmitting}
                    className="trip-modal-submit-btn custom-btn-general"
                >
                    {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
