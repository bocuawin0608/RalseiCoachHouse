import { useState, useEffect } from 'react';
import { Modal, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import roleApi from '../api/roleApi';

export default function RoleCreateModal({ isOpen, onClose, onSuccess }) {
    const [roleName, setRoleName] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (isOpen) {
            setRoleName('');
            setErrorMsg('');
        }
    }, [isOpen]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');
        setIsSubmitting(true);
        try {
            await roleApi.createRole({ roleName: roleName.trim() });
            onSuccess();
            onClose();
        } catch (err) {
            const data = err.response?.data;
            if (data?.fieldErrors) {
                const msgs = Object.entries(data.fieldErrors).map(([field, msg]) => `- ${field}: ${msg}`);
                setErrorMsg(['Dữ liệu đầu vào không hợp lệ:', ...msgs].join('\n'));
            } else {
                setErrorMsg(data?.message || 'Có lỗi xảy ra khi tạo vai trò.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} centered>
            <Modal.Header closeButton>
                <Modal.Title>Thêm vai trò</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {errorMsg && (
                        <Alert variant="danger" className="py-2 mb-3">
                            <div className="d-flex align-items-start gap-2">
                                <BsExclamationTriangleFill className="mt-1 flex-shrink-0" />
                                <span style={{whiteSpace: 'pre-line'}}>{errorMsg}</span>
                            </div>
                        </Alert>
                    )}
                    <Form.Group>
                        <Form.Label className="small">Tên vai trò <span className="text-danger">*</span></Form.Label>
                        <Form.Control
                            value={roleName}
                            onChange={e => setRoleName(e.target.value)}
                            required
                            maxLength={50}
                            size="sm"
                            placeholder="VD: TRIP_STAFF"
                        />
                        <Form.Text className="text-muted">Tên vai trò phải là duy nhất, tối đa 50 ký tự.</Form.Text>
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Hủy</Button>
                    <Button variant="primary" type="submit" disabled={isSubmitting}>
                        {isSubmitting ? <><Spinner size="sm" /> Đang tạo...</> : 'Tạo vai trò'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
