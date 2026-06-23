import { useState } from 'react';
import { Form, Button, Row, Col, Alert } from 'react-bootstrap';

export default function Step2PassengerInfo({ tripId, seatIds, initialInfo, onNext, onBack }) {
    const [info, setInfo] = useState({
        fullName: initialInfo.fullName || '',
        phone: initialInfo.phone || '',
        email: initialInfo.email || ''
    });

    const handleChange = (e) => {
        setInfo({ ...info, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onNext(info);
    };

    return (
        <div>
            <Alert variant="info" className="mb-4 text-center fw-bold">
                Ghế của bạn đang được giữ trong 10 phút. Vui lòng điền thông tin!
            </Alert>
            
            <Row className="justify-content-center">
                <Col md={8}>
                    <Form onSubmit={handleSubmit} className="p-4 border rounded-3 bg-light">
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Họ và tên <span className="text-danger">*</span></Form.Label>
                            <Form.Control 
                                type="text" 
                                name="fullName"
                                value={info.fullName} 
                                onChange={handleChange} 
                                required 
                                placeholder="VD: Nguyen Van A"
                            />
                        </Form.Group>

                        <Row>
                            <Col md={6}>
                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-bold">Số điện thoại <span className="text-danger">*</span></Form.Label>
                                    <Form.Control 
                                        type="tel" 
                                        name="phone"
                                        value={info.phone} 
                                        onChange={handleChange} 
                                        required 
                                        placeholder="VD: 0912345678"
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-4">
                                    <Form.Label className="fw-bold">Email</Form.Label>
                                    <Form.Control 
                                        type="email" 
                                        name="email"
                                        value={info.email} 
                                        onChange={handleChange} 
                                        placeholder="Để nhận vé điện tử"
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <div className="d-flex justify-content-between mt-4">
                            <Button 
                                variant="outline-dark" 
                                className="fw-bold px-4"
                                onClick={onBack}
                            >
                                Quay lại
                            </Button>
                            <Button 
                                type="submit" 
                                className="fw-bold border-0 px-4"
                                style={{ backgroundColor: 'var(--ralsei-black)', color: 'white' }}
                            >
                                Thanh toán
                            </Button>
                        </div>
                    </Form>
                </Col>
            </Row>
        </div>
    );
}