import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import customerApi from '../api/customerApi';

export default function CustomerCreateModal({ isOpen, onClose, onSuccess }) {
    const [form, setForm] = useState({ customerName: '', phone: '', email: '', dob: '' });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (isOpen) {
            setForm({ customerName: '', phone: '', email: '', dob: '' });
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
                customerName: form.customerName.trim(),
                phone: form.phone.trim() || null,
                email: form.email.trim() || null,
                dob: form.dob || null,
            };
            await customerApi.createCustomer(payload);
            onSuccess();
            onClose();
        } catch (err) {
            const data = err.response?.data;
            if (data?.fieldErrors) {
                const msgs = Object.entries(data.fieldErrors).map(([f, m]) => `- ${f}: ${m}`);
                setErrorMsg(['Dữ liệu đầu vào không hợp lệ:', ...msgs].join('\n'));
            } else {
                setErrorMsg(data?.message || 'Có lỗi xảy ra khi tạo khách hàng.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} centered>
            <Modal.Header closeButton>
                <Modal.Title>Thêm khách hàng</Modal.Title>
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
                    <Row className="g-2">
                        <Col md={6}>
                            <Form.Label className="small">Họ tên <span className="text-danger">*</span></Form.Label>
                            <Form.Control name="customerName" value={form.customerName} onChange={handleChange} required maxLength={100} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">SĐT</Form.Label>
                            <Form.Control name="phone" value={form.phone} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Email</Form.Label>
                            <Form.Control type="email" name="email" value={form.email} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Ngày sinh</Form.Label>
                            <Form.Control type="date" name="dob" value={form.dob} onChange={handleChange} size="sm" />
                        </Col>
                    </Row>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Hủy</Button>
                    <Button variant="primary" type="submit" disabled={isSubmitting}>
                        {isSubmitting ? <><Spinner size="sm" /> Đang tạo...</> : 'Tạo khách hàng'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
