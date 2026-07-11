import { Alert, Button, Form, Modal } from 'react-bootstrap';
import { useState, useEffect } from 'react';
import { cargoTypePriceApi } from '../api/cargoTypePriceApi';
import { BsExclamationTriangleFill } from 'react-icons/bs';
export default function CargoTypePriceUpdateInfoModal({ isOpen, data, cargoTypes, onClose, onSuccess }) {
    const [formData, setFormData] = useState({
        cargoTypeId: '',
        unit: '',
        pricePerUnit: '',
        startEffectiveDate: '',
        endEffectiveDate: ''
    });
    
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (data && isOpen) {
            setFormData({
                cargoTypeId: data.cargoTypeId || '',
                unit: data.unit || '',
                pricePerUnit: data.pricePerUnit || '',
                startEffectiveDate: data.startEffectiveDate ? data.startEffectiveDate.substring(0, 16) : '',
                endEffectiveDate: data.endEffectiveDate ? data.endEffectiveDate.substring(0, 16) : ''
            });
            setError(null);
        }    
    }, [data, isOpen]);

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

        setIsSubmitting(true);
        setError(null);
        try {
            const payload = {
                ...formData,
                cargoTypeId: parseInt(formData.cargoTypeId),
                pricePerUnit: parseFloat(formData.pricePerUnit)
            };

            await cargoTypePriceApi.updateCargoTypePrice(data.cargoTypePriceId, payload);
            
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
        <Modal show={isOpen} onHide={onClose} centered backdrop="static" size="lg">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Cập nhật thông tin giá cước
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4 pb-0">
                <Form id="update-cargo-price-form" onSubmit={handleSubmit}>
                    
                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">
                            Loại hàng <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Select 
                            name="cargoTypeId"
                            value={formData.cargoTypeId} 
                            onChange={handleInputChange} 
                            required
                            className="py-2"
                        >
                            <option value="" disabled>-- Chọn Loại Hàng --</option>
                            {cargoTypes && cargoTypes.filter(type => type.isActive === true || type.active === true || type.cargoTypeId == formData.cargoTypeId).map(type => (
                                <option key={type.cargoTypeId} value={type.cargoTypeId}>
                                    {type.cargoTypeName}
                                </option>
                            ))}
                        </Form.Select>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">
                            Đơn Vị (VD: kg, m3) <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control 
                            type="text"
                            name="unit"
                            value={formData.unit} 
                            onChange={handleInputChange} 
                            required maxLength={50}
                            placeholder="VD: kg"
                            className="py-2"
                        />
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">
                            Đơn Giá (VNĐ) <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control 
                            type="number"
                            name="pricePerUnit"
                            value={formData.pricePerUnit} 
                            onChange={handleInputChange} 
                            required min="0" step="1"
                            placeholder="VD: 50000"
                            className="py-2"
                        />
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">
                            Ngày Bắt Đầu Hiệu Lực <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control 
                            type="datetime-local"
                            name="startEffectiveDate"
                            value={formData.startEffectiveDate} 
                            onChange={handleInputChange} 
                            required
                            className="py-2"
                        />
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold text-secondary">
                            Ngày Kết Thúc Hiệu Lực <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control 
                            type="datetime-local"
                            name="endEffectiveDate"
                            value={formData.endEffectiveDate} 
                            onChange={handleInputChange} 
                            required
                            className="py-2"
                        />
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
                <Button type="submit" form="update-cargo-price-form" disabled={isSubmitting} className="px-4 custom-btn-general">
                    {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
