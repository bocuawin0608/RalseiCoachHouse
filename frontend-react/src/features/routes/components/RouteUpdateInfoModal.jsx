import { Alert, Button, Form, Modal } from 'react-bootstrap'
import { useState, useEffect } from 'react';
import { routeApi } from '../api/routeApi';
import { BsExclamationTriangleFill } from 'react-icons/bs';

export default function RouteUpdateInfoModal({ isOpen, data, onClose, onSuccess }) {
    const [formData, setFormData] = useState({
        routeName: '',
        totalKilometers: '',
        totalMinutes: '',
        active: true
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const load = () => {
            if (data && isOpen) {
                setFormData({
                    routeName: data.routeName || '',
                    totalKilometers: data.totalKilometers || '',
                    totalMinutes: data.totalMinutes || '',
                    active: data.active !== undefined ? data.active : data.isActive
                });
                setError(null);
            }
        }
        load();
    }, [data, isOpen]);

    const hasAnyChange = data && (
        data.routeName !== formData.routeName ||
        data.totalKilometers !== formData.totalKilometers ||
        data.totalMinutes !== formData.totalMinutes ||
        (data.active !== undefined ? data.active : data.isActive) !== formData.active
    );

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
        setError(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!hasAnyChange) {
            onClose();
            return;
        }

        setIsSubmitting(true);
        setError(null);
        try {
            // First update basic info
            await routeApi.updateRouteInfo(data.routeId, {
                routeName: formData.routeName,
                totalKilometers: formData.totalKilometers,
                totalMinutes: formData.totalMinutes,
                active: formData.active
            });

            const originalActive = data.active !== undefined ? data.active : data.active;
            if (originalActive !== formData.active) {
                if (formData.active) {
                    await routeApi.restoreRoute(data.routeId);
                } else {
                    await routeApi.disableRoute(data.routeId);
                }
            }

            onSuccess();
            onClose();
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
                    Cập nhật thông tin tuyến đường
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4 pb-0">
                <Form id="update-route-form" onSubmit={handleSubmit}>

                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">
                            Tên tuyến đường <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="text"
                            name="routeName"
                            value={formData.routeName}
                            onChange={handleInputChange}
                            required maxLength={100}
                            className="py-2"
                        />
                    </Form.Group>

                    <div className="d-flex gap-3 mb-4">
                        <Form.Group className="flex-fill">
                            <Form.Label className="fw-semibold text-secondary">
                                Khoảng cách (km) <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Control
                                type="number"
                                name="totalKilometers"
                                step="0.1" min="0"
                                value={formData.totalKilometers}
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
                                name="totalMinutes"
                                min="0"
                                value={formData.totalMinutes}
                                onChange={handleInputChange}
                                required
                                className="py-2"
                            />
                        </Form.Group>
                    </div>

                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">Trạng thái hệ thống</Form.Label>
                        <div className="d-flex align-items-center justify-content-between p-3 bg-light border rounded">
                            <div className="d-flex align-items-center gap-3">
                                <Form.Check
                                    type="switch"
                                    id="status-switch"
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
                <Button variant="primary" type="submit" form="update-route-form" disabled={isSubmitting} className="px-4">
                    {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
