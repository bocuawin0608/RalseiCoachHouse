import { Alert, Button, Form, Modal } from 'react-bootstrap'
import { useState, useEffect } from 'react';
import { cargoTypeApi } from '../api/cargoTypeApi';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import {
    MAX_CARGO_SURCHARGE_PRICE,
    normalizeCargoPriceInput,
    validateCargoSurchargePrice
} from '../utils/cargoPriceValidation';
import '../styles/CargoTypeManagement.css';

/**
 * Edits every field shown in the cargo type management table.
 */
export default function CargoTypeUpdateInfoModal({ isOpen, data, onClose, onSuccess }) {
    const [formData, setFormData] = useState({
        cargoTypeName: '',
        unit: '',
        pricePerUnit: ''
    });
    const [isActive, setIsActive] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const load = () => {
            if (data && isOpen) {
                setFormData({
                    cargoTypeName: data.cargoTypeName || '',
                    unit: data.unit || '',
                    pricePerUnit: data.pricePerUnit ?? ''
                });
                setIsActive(data.active);
                setError(null);
            }    
        }
        load();
    }, [data, isOpen]);

    const hasAnyChange = data && (
        data.cargoTypeName !== formData.cargoTypeName ||
        (data.unit || '') !== formData.unit ||
        String(data.pricePerUnit ?? '') !== String(formData.pricePerUnit) ||
        data.active !== isActive
    );

    const handleInputChange = (e) => {
        const { name, value } = e.target;

        setFormData(prev => ({
            ...prev,
            [name]: name === 'pricePerUnit' ? normalizeCargoPriceInput(value) : value
        }));
        setError(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if(!hasAnyChange) {
            onClose();
            return;
        }

        setIsSubmitting(true);
        setError(null);
        try {
            const priceError = validateCargoSurchargePrice(formData.pricePerUnit);
            if (priceError) {
                setError(priceError);
                return;
            }

            await cargoTypeApi.updateCargoTypeInfo(data.cargoTypeId, {
                cargoTypeName: formData.cargoTypeName,
                unit: formData.unit,
                pricePerUnit: formData.pricePerUnit
            });
            
            if (data.active !== isActive) {
                await cargoTypeApi.toggleCargoTypeStatus(data.cargoTypeId, isActive);
            }

            onSuccess(); 
            onClose();   
        } catch (error) {
            setError(error.response?.data?.message || "Có lỗi xảy ra khi cập nhật." );
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <Modal show={isOpen} onHide={onClose} centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Cập nhật thông tin loại hàng
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4 pb-0">
                <Form id="update-cargo-form" onSubmit={handleSubmit}>
                    
                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold text-secondary">
                            Tên loại hàng <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control 
                            type="text"
                            name="cargoTypeName"
                            value={formData.cargoTypeName}
                            onChange={handleInputChange}
                            required
                            maxLength={100}
                            className="py-2"
                        />
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold text-secondary">
                            Đơn vị <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="text"
                            name="unit"
                            value={formData.unit}
                            onChange={handleInputChange}
                            required
                            maxLength={50}
                            placeholder="Ví dụ: kg, m3, kiện..."
                            className="py-2"
                        />
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold text-secondary">
                            Đơn giá (VNĐ) <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="number"
                            name="pricePerUnit"
                            value={formData.pricePerUnit}
                            onChange={handleInputChange}
                            required
                            min="0"
                            max={MAX_CARGO_SURCHARGE_PRICE}
                            step="1"
                            className="py-2"
                        />
                    </Form.Group>

                    <Form.Group className="mb-2">
                        <Form.Label className="fw-semibold text-secondary">Trạng thái hệ thống</Form.Label>
                        <div className="cargo-type-management-status">
                            <div className="d-flex align-items-center gap-3">
                                <Form.Check 
                                    type="switch" 
                                    id="status-switch"
                                    checked={isActive} 
                                    onChange={(e) => {
                                        setIsActive(e.target.checked);
                                        setError(null);
                                    }} 
                                    className="fs-5 m-0"
                                />
                                <span className="fw-medium text-dark cargo-type-management-status__label">
                                    Cho phép hoạt động
                                </span>
                            </div>
                            <span className={`badge px-3 py-2 ${isActive ? 'bg-success' : 'bg-secondary'}`}>
                                {isActive ? 'Đang hoạt động' : 'Ngừng hoạt động'}
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
                <Button type="submit" form="update-cargo-form" disabled={isSubmitting} className="px-4 custom-btn-general">
                    {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
