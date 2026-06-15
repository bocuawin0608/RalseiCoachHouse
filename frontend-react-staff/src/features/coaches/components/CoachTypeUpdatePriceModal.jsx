import { Alert, Button, Form, Modal, InputGroup } from 'react-bootstrap';
import { useState, useEffect } from 'react';
import { coachTypeApi } from '../api/coachTypeApi';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import { formatCurrency } from '../../../utils/formatters';

export default function CoachTypeUpdatePriceModal({ isOpen, data, onClose, onSuccess }) {
    const [price, setPrice] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const load = () => {
            if (data && isOpen) {
                setPrice(data.currentPrice); 
                setError(null);
            }    
        }
        load();
    }, [data, isOpen]);

    const hasAnyChange = data && (Number(data.currentPrice) !== Number(price));

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!hasAnyChange) {
            onClose();
            return;
        }

        setIsSubmitting(true);
        setError(null);
        try {
            await coachTypeApi.updateCoachTypePrice(data.coachTypeId, { seatPrice: Number(price) });
            onSuccess(); 
            onClose();   
        } catch (error) {
            setError(error.response?.data?.message || "Có lỗi xảy ra khi cập nhật giá vé." );
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <Modal show={isOpen} onHide={onClose} centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Cập nhật giá vé xe
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4 pb-0">
                <Form id="update-price-form" onSubmit={handleSubmit}>
                    
                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold text-secondary">
                            Mức giá áp dụng mới <span className="text-danger">*</span>
                        </Form.Label>
                        
                        <InputGroup>
                            <Form.Control 
                                type="number"
                                name="seatPrice"
                                min="0"
                                max='100000000'
                                value={price} 
                                onChange={(e) => {
                                    setPrice(e.target.value);
                                    setError(null);
                                }} 
                                required 
                                className="py-2"
                                placeholder="Nhập giá vé..."
                            />
                            <InputGroup.Text className="bg-light fw-medium text-secondary">VNĐ</InputGroup.Text>
                        </InputGroup>
                        
                        {price !== '' && !isNaN(price) && (
                            <Form.Text className="text-success fw-medium mt-2 d-block">
                                Giá trị: {formatCurrency(price)}
                            </Form.Text>
                        )}
                    </Form.Group>

                    {error && (
                        <Alert variant='danger' className="mb-3 py-2 px-3 border-0 d-flex align-items-center gap-2">
                            <BsExclamationTriangleFill />
                            <span>{error}</span>
                        </Alert>
                    )}
                </Form>
            </Modal.Body>

            <Modal.Footer className="bg-light border-0 rounded-bottom">
                <Button variant="outline-secondary" onClick={onClose} disabled={isSubmitting} className="px-4">
                    Hủy bỏ
                </Button>
                <Button variant="primary" type="submit" form="update-price-form" disabled={isSubmitting} className="px-4">
                    {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}