import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import customerApi from '../api/customerApi';

export default function CustomerUpdateModal({ isOpen, data, onClose, onSuccess }) {
    const [form, setForm] = useState({ customerName: '', phone: '', email: '', dob: '', isActive: true });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (isOpen && data) {
            setForm({
                customerName: data.customerName || '',
                phone: data.phone || '',
                email: data.email || '',
                dob: data.dob ? data.dob.substring(0, 10) : '',
                isActive: data.active !== false,
            });
            setErrorMsg('');
        }
    }, [isOpen, data]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const phone = form.phone.trim();
        if (phone && !/^[0-9]{10,11}$/.test(phone.replace(/[^0-9]/g, ''))) {
            setErrorMsg('Số điện thoại không hợp lệ (10-11 số).');
            return;
        }
        if (form.dob) {
            const birth = new Date(form.dob);
            const cutoff = new Date();
            cutoff.setFullYear(cutoff.getFullYear() - 16);
            if (birth > cutoff) {
                setErrorMsg('Khách hàng phải từ đủ 16 tuổi trở lên.');
                return;
            }
        }

        setIsSubmitting(true);
        setErrorMsg('');
        try {
            const payload = {
                customerName: form.customerName.trim(),
                phone: form.phone.trim() || null,
                email: form.email.trim() || null,
                dob: form.dob || null,
                isActive: form.isActive,
            };
            await customerApi.updateCustomer(data.customerId, payload);
            onSuccess();
            onClose();
        } catch (err) {
            const data = err.response?.data;
            if (data?.fieldErrors) {
                const msgs = Object.entries(data.fieldErrors).map(([f, m]) => `- ${f}: ${m}`);
                setErrorMsg(['Dữ liệu đầu vào không hợp lệ:', ...msgs].join('\n'));
            } else {
                setErrorMsg(data?.message || 'Có lỗi xảy ra khi cập nhật.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} centered>
            <Modal.Header closeButton>
                <Modal.Title>Cập nhật khách hàng</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {errorMsg && (
                        <Alert variant="danger" className="d-flex align-items-center gap-2 py-2">
                            <BsExclamationTriangleFill /><span style={{whiteSpace: 'pre-line'}}>{errorMsg}</span>
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
                        <Col md={3} className="d-flex align-items-end">
                            <Form.Check type="switch" id="customer-active-switch" label="Kích hoạt"
                                name="isActive" checked={form.isActive} onChange={handleChange} />
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
