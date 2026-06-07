import { Alert, Button, Form, Modal } from 'react-bootstrap'
import { useState, useEffect } from 'react';
import { coachTypeApi } from '../api/coachTypeApi';
import { BsExclamationTriangleFill } from 'react-icons/bs';

export default function CoachTypeUpdateInfoModal({ isOpen, data, onClose, onSuccess }) {
    const [name, setName] = useState('');
    const [isActive, setIsActive] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const load = () => {
            if (data && isOpen) {
                setName(data.coachTypeName);
                setIsActive(data.isActive);
                setError(null);
            }    
        }
        load();
    }, [data, isOpen]);

    const hasAnyChange = data && (data.coachTypeName !== name || data.isActive !== isActive);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if(!hasAnyChange) {
            onClose();
            return;
        }

        setIsSubmitting(true);
        setError(null);
        try {
            await coachTypeApi.updateCoachTypeInfo(data.coachTypeId, { coachTypeName: name, isActive });
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
                    Cập nhật thông tin loại xe
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4 pb-0">
                <Form id="update-coach-form" onSubmit={handleSubmit}>
                    
                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold text-secondary">
                            Tên loại xe <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control 
                            type="text"
                            value={name} 
                            onChange={(e) => {
                                setName(e.target.value)
                                setError(null);
                            }} 
                            required maxLength={100}
                            className="py-2"
                        />
                    </Form.Group>

                    <Form.Group className="mb-2">
                        <Form.Label className="fw-semibold text-secondary">Trạng thái hệ thống</Form.Label>
                        <div className="d-flex align-items-center justify-content-between p-3 bg-light border rounded">
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
                                <span style={{ fontSize: '0.95rem' }} className="fw-medium text-dark">
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
                <Button variant="primary" type="submit" form="update-coach-form" disabled={isSubmitting} className="px-4">
                    {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}