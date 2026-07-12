import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import accountApi from '../api/accountApi';

const STAFF_POSITIONS = ['DRIVER', 'ATTENDANT', 'TICKET_STAFF', 'MANAGER'];

export default function AccountUpdateModal({ isOpen, data, onClose, onSuccess }) {
    const [form, setForm] = useState({
        staffName: '', phone: '', email: '', cccd: '', dob: '',
        staffPosition: 'TICKET_STAFF', ticketAgencyId: '', hireDate: '', isActive: true,
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen && data) {
            setLoading(true);
            accountApi.getAccountDetail(data.accountId)
                .then(detail => {
                    const s = detail.staff || {};
                    setForm({
                        staffName: s.staffName || data.staffName || '',
                        phone: s.phone || data.phone || '',
                        email: s.email || data.email || '',
                        cccd: s.cccd || '',
                        dob: s.dob ? s.dob.substring(0, 10) : '',
                        staffPosition: s.staffPosition || data.staffPosition || 'TICKET_STAFF',
                        ticketAgencyId: s.ticketAgencyId ?? data.ticketAgencyId ?? '',
                        hireDate: s.hireDate ? s.hireDate.substring(0, 10) : '',
                        isActive: detail.active !== false,
                    });
                })
                .catch(() => {
                    setForm({
                        staffName: data.staffName || '',
                        phone: data.phone || '',
                        email: data.email || '',
                        cccd: '',
                        dob: '',
                        staffPosition: data.staffPosition || 'TICKET_STAFF',
                        ticketAgencyId: data.ticketAgencyId || '',
                        hireDate: '',
                        isActive: data.active !== false,
                    });
                })
                .finally(() => setLoading(false));
            setErrorMsg('');
        }
    }, [isOpen, data]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (form.hireDate && new Date(form.hireDate) < new Date('1900-01-01')) {
            setErrorMsg('Ngày vào làm không hợp lệ.');
            return;
        }
        if (form.dob) {
            const birth = new Date(form.dob);
            const cutoff = new Date();
            cutoff.setFullYear(cutoff.getFullYear() - 16);
            if (birth > cutoff) {
                setErrorMsg('Nhân viên phải từ đủ 16 tuổi trở lên.');
                return;
            }
        }

        setIsSubmitting(true);
        setErrorMsg('');
        try {
            const payload = {
                staffName: form.staffName,
                phone: form.phone,
                email: form.email || null,
                cccd: form.cccd || null,
                dob: form.dob || null,
                staffPosition: form.staffPosition,
                ticketAgencyId: form.ticketAgencyId ? parseInt(form.ticketAgencyId) : null,
                hireDate: form.hireDate || null,
                isActive: form.isActive,
            };
            await accountApi.updateAccount(data.accountId, payload);
            onSuccess();
            onClose();
        } catch (err) {
            setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi cập nhật.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>Cập nhật thông tin</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {errorMsg && (
                        <Alert variant="danger" className="d-flex align-items-center gap-2 py-2">
                            <BsExclamationTriangleFill /><span>{errorMsg}</span>
                        </Alert>
                    )}
                    <Row className="g-2">
                        <Col md={4}>
                            <Form.Label className="small">Họ tên <span className="text-danger">*</span></Form.Label>
                            <Form.Control name="staffName" value={form.staffName} onChange={handleChange} required size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">SĐT <span className="text-danger">*</span></Form.Label>
                            <Form.Control name="phone" value={form.phone} onChange={handleChange} required size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Email</Form.Label>
                            <Form.Control type="email" name="email" value={form.email} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={2}>
                            <Form.Label className="small">CCCD</Form.Label>
                            <Form.Control name="cccd" value={form.cccd} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Ngày sinh</Form.Label>
                            <Form.Control type="date" name="dob" value={form.dob} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Chức vụ <span className="text-danger">*</span></Form.Label>
                            <Form.Select name="staffPosition" value={form.staffPosition} onChange={handleChange} size="sm">
                                {STAFF_POSITIONS.map(pos => (
                                    <option key={pos} value={pos}>{pos}</option>
                                ))}
                            </Form.Select>
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">ID bến xe</Form.Label>
                            <Form.Control type="number" name="ticketAgencyId" value={form.ticketAgencyId} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Ngày vào làm</Form.Label>
                            <Form.Control type="date" name="hireDate" value={form.hireDate} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">&nbsp;</Form.Label>
                            <Form.Check
                                type="switch"
                                name="isActive"
                                label="Kích hoạt"
                                checked={form.isActive}
                                onChange={handleChange}
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
