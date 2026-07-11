import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import ticketAgencyApi from '../api/ticketAgencyApi';

export default function TicketAgencyUpdateModal({ isOpen, data, onClose, onSuccess }) {
    const [form, setForm] = useState({ ticketAgencyName: '', isActive: true });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (isOpen && data) {
            setForm({
                ticketAgencyName: data.ticketAgencyName || '',
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
        setIsSubmitting(true);
        setErrorMsg('');
        try {
            await ticketAgencyApi.update(data.ticketAgencyId, {
                ticketAgencyName: form.ticketAgencyName.trim(),
                stopPointId: data.stopPointId,
                isActive: form.isActive,
            });
            onSuccess();
            onClose();
        } catch (err) {
            const data = err.response?.data;
            if (data?.fieldErrors) {
                const msgs = Object.entries(data.fieldErrors).map(([f, m]) => `- ${f}: ${m}`);
                setErrorMsg(['Dữ liệu không hợp lệ:', ...msgs].join('\n'));
            } else {
                setErrorMsg(data?.message || 'Có lỗi xảy ra.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} centered enforceFocus restoreFocus>
            <Modal.Header closeButton><Modal.Title>Sửa đại lý</Modal.Title></Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {errorMsg && (
                        <Alert variant="danger" className="d-flex align-items-center gap-2 py-2">
                            <BsExclamationTriangleFill /><span style={{whiteSpace: 'pre-line'}}>{errorMsg}</span>
                        </Alert>
                    )}
                    <Row className="g-2">
                        <Col md={6}>
                            <Form.Label className="small">Điểm dừng</Form.Label>
                            <Form.Control type="text" value={data?.stopPointName || ''} size="sm" disabled />
                            {data?.city && <small className="text-muted d-block mt-1">{data.stopPointName}, {data.city}</small>}
                        </Col>
                        <Col md={6}>
                            <Form.Label className="small">Tên đại lý <span className="text-danger">*</span></Form.Label>
                            <Form.Control name="ticketAgencyName" value={form.ticketAgencyName} onChange={handleChange} required maxLength={200} size="sm" autoFocus />
                        </Col>
                        <Col md={3} className="d-flex align-items-end">
                            <Form.Check type="switch" id="ta-active-switch" label="Kích hoạt"
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