import { Alert, Button, Form, Modal } from 'react-bootstrap'
import { useState, useEffect } from 'react';
import { routeStopApi } from '../api/routeStopApi';
import { BsExclamationTriangleFill } from 'react-icons/bs';

export default function RouteStopUpdateInfoModal({ isOpen, data, routeTotalKilometers, routeTotalMinutes, onClose, onSuccess }) {
    const [formData, setFormData] = useState({
        kilometersFromStart: '',
        minutesFromStart: ''
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const load = () => {
            if (data && isOpen) {
                setFormData({
                    kilometersFromStart: data.kilometersFromStart || '',
                    minutesFromStart: data.minutesFromStart || ''
                });
                setError(null);
            }
        }
        load();
    }, [data, isOpen]);

    const hasAnyChange = data && (
        Number(data.kilometersFromStart) !== Number(formData.kilometersFromStart) ||
        Number(data.minutesFromStart) !== Number(formData.minutesFromStart)
    );

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        setError(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!hasAnyChange) {
            onClose();
            return;
        }

        if (Number(formData.kilometersFromStart) > Number(routeTotalKilometers)) {
            setError(`Khoảng cách không thể vượt quá tổng khoảng cách của tuyến xe (${routeTotalKilometers} km).`);
            return;
        }

        if (Number(formData.minutesFromStart) > Number(routeTotalMinutes)) {
            setError(`Thời gian không thể vượt quá tổng thời gian của tuyến xe (${routeTotalMinutes} phút).`);
            return;
        }

        const minutes = Number(formData.minutesFromStart);
        const kilometers = Number(formData.kilometersFromStart);
        const isBothZero = minutes === 0 && kilometers === 0;
        const isBothPositive = minutes > 0 && kilometers > 0;
        if (!isBothZero && !isBothPositive) {
            setError('Khoảng cách và thời gian phải cùng bằng 0 hoặc cùng lớn hơn 0.');
            return;
        }

        setIsSubmitting(true);
        setError(null);
        try {
            await routeStopApi.updateRouteStop(data.routeStopId, {
                routeId: data.routeId,
                stopPointId: data.stopPointId,
                stopOrder: data.stopOrder,
                kilometersFromStart: Number(formData.kilometersFromStart),
                minutesFromStart: Number(formData.minutesFromStart)
            });

            onSuccess();
        } catch (error) {
            setError(error.response?.data?.message || "Có lỗi xảy ra khi cập nhật.");
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <Modal show={isOpen} onHide={onClose} centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Cập nhật thông tin điểm dừng
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4 pb-0">
                <Form id="update-route-stop-form" onSubmit={handleSubmit}>

                    <div className="d-flex gap-3 mb-4">
                        <Form.Group className="flex-fill">
                            <Form.Label className="fw-semibold text-secondary">
                                Khoảng cách (km) <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Control
                                type="number"
                                name="kilometersFromStart"
                                step="0.1" min="0"
                                value={formData.kilometersFromStart}
                                onChange={handleInputChange}
                                required
                                className="py-2"
                            />
                        </Form.Group>

                        <Form.Group className="flex-fill">
                            <Form.Label className="fw-semibold text-secondary">
                                Thời gian (phút) <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Control
                                type="number"
                                name="minutesFromStart"
                                min="0"
                                value={formData.minutesFromStart}
                                onChange={handleInputChange}
                                required
                                className="py-2"
                            />
                        </Form.Group>
                    </div>

                    {error && <Alert variant='danger' className="mb-3 py-2 px-3 border-0 d-flex align-items-center gap-2">
                        <BsExclamationTriangleFill />
                        <span>{error}</span>
                    </Alert>}
                </Form>
            </Modal.Body>

            <Modal.Footer className="bg-light border-0 rounded-bottom">
                <Button variant="outline-secondary" onClick={onClose} disabled={isSubmitting} className="px-4">
                    Hủy bỏ
                </Button>
                <Button type="submit" form="update-route-stop-form" disabled={isSubmitting} className="px-4 custom-btn-general">
                    {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
