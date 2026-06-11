import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { cargoTypeApi } from '../../../features/cargo';
import { Alert, Button, Card, Col, Container, Form, Row } from 'react-bootstrap';

export default function CargoTypeCreatePage() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        cargoTypeName: ''
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    const handleInputChange = (e) => {
        const { name, value } = e.target;

        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        if (!formData.cargoTypeName) {
            setErrorMsg('Vui lòng nhập tên loại hàng!');
            return;
        }

        setIsSubmitting(true);

        try {
            const payload = {
                cargoTypeName: formData.cargoTypeName
            };

            await cargoTypeApi.createCargoType(payload);
            
            navigate('/manager/cargo-types');
        } catch (error) {
            console.error("Lỗi tạo loại hàng:", error);
            setErrorMsg(error.response?.data?.message || 'Có lỗi xảy ra khi lưu vào hệ thống.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            
            <Button 
                variant="link" 
                onClick={() => navigate('/manager/cargo-types')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18}/> Quay lại danh sách
            </Button>

            <h2 className="mb-4 text-dark fw-bold">Thêm mới loại hàng</h2>

            {errorMsg && (
                <Alert variant="danger" className="shadow-sm border-0 d-flex align-items-center gap-2">
                    <BsExclamationTriangleFill />
                    <span>{errorMsg}</span>
                </Alert>
            )}

            <Form onSubmit={handleSubmit}>
                <Row className="g-4 justify-content-center">
                    
                    <Col lg={6} md={12}>
                        <Card className="shadow-sm border-0 h-100">
                            <Card.Header className="bg-white border-bottom pt-3 pb-2">
                                <h5 className="fw-bold mb-0 text-dark">Thông tin cơ bản</h5>
                            </Card.Header>
                            <Card.Body className="d-flex flex-column gap-3">
                                
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Tên loại hàng <span className="text-danger">*</span></Form.Label>
                                    <Form.Control 
                                        type="text" 
                                        name="cargoTypeName" 
                                        required 
                                        maxLength={100}
                                        placeholder="Ví dụ: Hàng dễ vỡ, Hàng cồng kềnh..."
                                        value={formData.cargoTypeName} 
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>

                                <Button 
                                    type="submit" 
                                    variant="primary" 
                                    disabled={isSubmitting}
                                    className="w-100 py-2 mt-3 fw-medium d-flex justify-content-center align-items-center gap-2"
                                >
                                    <BsCheckCircle size={18}/> 
                                    {isSubmitting ? 'Đang lưu hệ thống...' : 'Lưu & Kích hoạt'}
                                </Button>

                            </Card.Body>
                        </Card>
                    </Col>

                </Row>
            </Form>

        </Container>
    );
}
