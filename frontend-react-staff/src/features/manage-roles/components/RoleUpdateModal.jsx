import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import roleApi from '../api/roleApi';

export default function RoleUpdateModal({ isOpen, data, onClose, onSuccess }) {
    const [roleName, setRoleName] = useState('');
    const [isActive, setIsActive] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (isOpen && data) {
            setRoleName(data.roleName || '');
            setIsActive(data.active !== false);
            setErrorMsg('');
        }
    }, [isOpen, data]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setErrorMsg('');
        try {
            await roleApi.updateRole(data.roleId, { roleName: roleName.trim(), isActive });
            onSuccess();
            onClose();
        } catch (err) {
            const data = err.response?.data;
            if (data?.fieldErrors) {
                const msgs = Object.entries(data.fieldErrors).map(([field, msg]) => `- ${field}: ${msg}`);
                setErrorMsg(['Dữ liệu đầu vào không hợp lệ:', ...msgs].join('\n'));
            } else {
                setErrorMsg(data?.message || 'Có lỗi xảy ra khi cập nhật vai trò.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} centered>
            <Modal.Header closeButton>
                <Modal.Title>Cập nhật vai trò</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {errorMsg && (
                        <Alert variant="danger" className="d-flex align-items-center gap-2 py-2">
                            <BsExclamationTriangleFill /><span style={{whiteSpace: 'pre-line'}}>{errorMsg}</span>
                        </Alert>
                    )}
                    <Row className="g-2">
                        <Col md={8}>
                            <Form.Label className="small">Tên vai trò <span className="text-danger">*</span></Form.Label>
                            <Form.Control
                                value={roleName}
                                onChange={e => setRoleName(e.target.value)}
                                required
                                maxLength={50}
                                size="sm"
                            />
                        </Col>
                        <Col md={4} className="d-flex align-items-end">
                            <Form.Check
                                type="switch"
                                id="role-active-switch"
                                label="Kích hoạt"
                                checked={isActive}
                                onChange={e => setIsActive(e.target.checked)}
                            />
                        </Col>
                    </Row>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Hủy</Button>
                    <Button variant="primary" type="submit" disabled={isSubmitting}>
                        {isSubmitting ? <><Spinner size="sm" /> Đang lưu...</> : 'Lưu thay đổi'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
