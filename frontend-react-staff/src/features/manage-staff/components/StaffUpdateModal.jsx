import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import staffApi from '../api/staffApi';

const POSITIONS = ['DRIVER', 'TICKET_STAFF', 'TRIP_STAFF', 'MANAGER', 'ADMIN'];

export default function StaffUpdateModal({ isOpen, data, onClose, onSuccess, ticketAgencies }) {
    const [form, setForm] = useState({
        staffName: '', phone: '', email: '', dob: '', cccd: '',
        staffPosition: '', hireDate: '', ticketAgencyId: '', isActive: true,
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen && data) {
            setLoading(true);
            staffApi.getDetail(data.staffId)
                .then(detail => {
                    setForm({
                        staffName: detail.staffName || '',
                        phone: detail.phone || '',
                        email: detail.email || '',
                        dob: detail.dob || '',
                        cccd: detail.cccd || '',
                        staffPosition: detail.staffPosition || '',
                        hireDate: detail.hireDate || '',
                        ticketAgencyId: detail.ticketAgencyId ?? '',
                        isActive: detail.active !== false,
                    });
                })
                .catch(() => {})
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
        const cccd = form.cccd.trim();
        if (cccd && !/^[0-9]{9,12}$/.test(cccd)) {
            setErrorMsg('CCCD không hợp lệ (9-12 chữ số).');
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
            await staffApi.update(data.staffId, {
                staffName: form.staffName.trim(),
                phone: form.phone.trim() || null,
                email: form.email.trim() || null,
                dob: form.dob || null,
                cccd: form.cccd.trim() || null,
                staffPosition: form.staffPosition.trim(),
                hireDate: form.hireDate || null,
                ticketAgencyId: form.ticketAgencyId !== '' ? parseInt(form.ticketAgencyId, 10) : null,
                isActive: form.isActive,
            });
            onSuccess();
            onClose();
        } catch (err) {
            const respData = err.response?.data;
            if (respData?.fieldErrors) {
                const msgs = Object.entries(respData.fieldErrors).map(([f, m]) => `- ${f}: ${m}`);
                setErrorMsg(['Dữ liệu không hợp lệ:', ...msgs].join('\n'));
            } else {
                setErrorMsg(respData?.message || 'Có lỗi xảy ra.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} centered size="lg">
            <Modal.Header closeButton><Modal.Title>Cập nhật nhân viên</Modal.Title></Modal.Header>
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
                            <Form.Control name="staffName" value={form.staffName} onChange={handleChange} required maxLength={200} size="sm" />
                        </Col>
                        <Col md={6}>
                            <Form.Label className="small">SĐT</Form.Label>
                            <Form.Control name="phone" value={form.phone} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={6}>
                            <Form.Label className="small">Email</Form.Label>
                            <Form.Control type="email" name="email" value={form.email} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Ngày sinh</Form.Label>
                            <Form.Control type="date" name="dob" value={form.dob} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">CCCD</Form.Label>
                            <Form.Control name="cccd" value={form.cccd} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={4}>
                            <Form.Label className="small">Chức vụ <span className="text-danger">*</span></Form.Label>
                            <Form.Select name="staffPosition" value={form.staffPosition} onChange={handleChange} required size="sm">
                                <option value="">-- Chọn chức vụ --</option>
                                {POSITIONS.map(p => (
                                    <option key={p} value={p}>{p}</option>
                                ))}
                            </Form.Select>
                        </Col>
                        <Col md={4}>
                            <Form.Label className="small">Ngày vào làm</Form.Label>
                            <Form.Control type="date" name="hireDate" value={form.hireDate} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={4}>
                            <Form.Label className="small">Bến xe</Form.Label>
                            <Form.Select name="ticketAgencyId" value={form.ticketAgencyId} onChange={handleChange} size="sm">
                                <option value="">-- Không trực thuộc --</option>
                                {(ticketAgencies || []).map(ta => (
                                    <option key={ta.ticketAgencyId} value={ta.ticketAgencyId}>{ta.ticketAgencyName}</option>
                                ))}
                            </Form.Select>
                        </Col>
                        <Col md={3} className="d-flex align-items-end">
                            <Form.Check type="switch" id="s-active-switch" label="Kích hoạt"
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
