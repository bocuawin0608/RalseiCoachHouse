import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import ticketAgencyApi from '../api/ticketAgencyApi';

export default function TicketAgencyCreateModal({ isOpen, onClose, onSuccess }) {
    const [form, setForm] = useState({ ticketAgencyName: '', stopPointId: '' });
    const [availableStops, setAvailableStops] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (isOpen) {
            setForm({ ticketAgencyName: '', stopPointId: '' });
            setErrorMsg('');
            ticketAgencyApi.getAvailableStops()
                .then(res => setAvailableStops(res || []))
                .catch(() => setAvailableStops([]));
        }
    }, [isOpen]);

    const selectedStop = availableStops.find(cs => cs.stopPointId == form.stopPointId);

    const handleStopChange = (e) => {
        const stopId = e.target.value;
        const stop = availableStops.find(cs => cs.stopPointId == stopId);
        setForm(prev => ({
            ...prev,
            stopPointId: stopId,
            ticketAgencyName: stop ? stop.stopPointName : prev.ticketAgencyName,
        }));
    };

    const handleNameChange = (e) => {
        setForm(prev => ({ ...prev, ticketAgencyName: e.target.value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');
        setIsSubmitting(true);
        try {
            await ticketAgencyApi.create({
                ticketAgencyName: form.ticketAgencyName.trim(),
                stopPointId: parseInt(form.stopPointId, 10),
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
        <Modal show={isOpen} onHide={onClose} centered>
            <Modal.Header closeButton><Modal.Title>Thêm bến xe</Modal.Title></Modal.Header>
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
                            <Form.Label className="small">Điểm dừng <span className="text-danger">*</span></Form.Label>
                            <Form.Select name="stopPointId" value={form.stopPointId} onChange={handleStopChange} required size="sm">
                                <option value="">-- Chọn điểm dừng --</option>
                                {availableStops.map(cs => (
                                    <option key={cs.stopPointId} value={cs.stopPointId}>{cs.stopPointName}</option>
                                ))}
                            </Form.Select>
                            {selectedStop && (
                                <small className="text-muted d-block mt-1">
                                    {selectedStop.address}{selectedStop.city ? `, ${selectedStop.city}` : ''}
                                </small>
                            )}
                        </Col>
                        <Col md={6}>
                            <Form.Label className="small">Tên đại lý <span className="text-danger">*</span></Form.Label>
                            <Form.Control name="ticketAgencyName" value={form.ticketAgencyName} onChange={handleNameChange} required maxLength={200} size="sm" />
                        </Col>
                    </Row>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Hủy</Button>
                    <Button variant="primary" type="submit" disabled={isSubmitting}>
                        {isSubmitting ? <><Spinner size="sm" /> Đang tạo...</> : 'Tạo bến xe'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}