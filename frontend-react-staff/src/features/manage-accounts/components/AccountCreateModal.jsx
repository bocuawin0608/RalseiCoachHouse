import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import accountApi from '../api/accountApi';



export default function AccountCreateModal({ isOpen, onClose, onSuccess }) {
    const [form, setForm] = useState({
        username: '', password: '',
        customerName: '', email: '',
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (isOpen) {
            setForm({
                username: '', password: '',
                customerName: '', email: '',
            });
            setErrorMsg('');
        }
    }, [isOpen]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        setIsSubmitting(true);
        try {
            const payload = {
                username: form.username,
                password: form.password,
                staffName: form.customerName,
                phone: form.username,
                email: form.email || null,
                staffPosition: 'CUSTOMER',
            };
            await accountApi.createAccount(payload);
            onSuccess();
            onClose();
        } catch (err) {
            const data = err.response?.data;
            if (data?.fieldErrors) {
                const msgs = Object.entries(data.fieldErrors).map(([field, msg]) => `- ${field}: ${msg}`);
                setErrorMsg(['Dữ liệu đầu vào không hợp lệ:', ...msgs].join('\n'));
            } else {
                setErrorMsg(data?.message || 'Có lỗi xảy ra khi tạo tài khoản.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>Thêm tài khoản</Modal.Title>
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
                    <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin tài khoản</h6>
                    <Row className="g-2 mb-3">
                        <Col md={6}>
                            <Form.Label className="small">SĐT (Tên đăng nhập) <span className="text-danger">*</span></Form.Label>
                            <Form.Control name="username" value={form.username} onChange={handleChange} required size="sm" />
                        </Col>
                        <Col md={6}>
                            <Form.Label className="small">Mật khẩu <span className="text-danger">*</span></Form.Label>
                            <Form.Control type="password" name="password" value={form.password} onChange={handleChange} required size="sm" minLength={6} />
                        </Col>
                    </Row>

                    <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin khách hàng</h6>
                    <Row className="g-2 mb-3">
                        <Col md={6}>
                            <Form.Label className="small">Họ tên <span className="text-danger">*</span></Form.Label>
                            <Form.Control name="customerName" value={form.customerName} onChange={handleChange} required size="sm" />
                        </Col>
                        <Col md={6}>
                            <Form.Label className="small">Email</Form.Label>
                            <Form.Control type="email" name="email" value={form.email} onChange={handleChange} size="sm" />
                        </Col>
                    </Row>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Hủy</Button>
                    <Button variant="primary" type="submit" disabled={isSubmitting}>
                        {isSubmitting ? <><Spinner size="sm" /> Đang tạo...</> : 'Tạo tài khoản'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
