import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { cargoTypeApi } from '../../../features/cargo';
import { Alert, Button, Card, Col, Container, Form, InputGroup, Row } from 'react-bootstrap';
import { formatCurrency } from '../../../utils/formatters';
import '../../../features/cargo/styles/CargoTypeManagement.css';

/**
 * Creates a cargo type and its surcharge fields in one operation.
 */
export default function CargoTypeCreatePage() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        cargoTypeName: '',
        unit: '',
        pricePerUnit: ''
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    const handleInputChange = (e) => {
        const { name, value } = e.target;

        setFormData(prev => ({
            ...prev,
            [name]: name === 'pricePerUnit' ? (value ? Number(value) : '') : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        if (!formData.cargoTypeName || !formData.unit || formData.pricePerUnit === '') {
            setErrorMsg('Vui lòng nhập đầy đủ tên loại hàng, đơn vị và đơn giá!');
            return;
        }

        setIsSubmitting(true);

        try {
            const payload = {
                cargoTypeName: formData.cargoTypeName,
                unit: formData.unit,
                pricePerUnit: formData.pricePerUnit
            };

            await cargoTypeApi.createCargoType(payload);

            navigate('/management/cargo-types');
        } catch (error) {
            console.error("Lỗi tạo loại hàng:", error);
            setErrorMsg(error.response?.data?.message || 'Có lỗi xảy ra khi lưu vào hệ thống.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Container fluid className="py-4 cargo-type-management-create-page">

            <Button
                variant="link"
                onClick={() => navigate('/management/cargo-types')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18} /> Quay lại danh sách
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

                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Đơn vị <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="unit"
                                        required
                                        maxLength={50}
                                        placeholder="Ví dụ: kg, m3, kiện..."
                                        value={formData.unit}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>

                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Đơn giá <span className="text-danger">*</span></Form.Label>
                                    <InputGroup>
                                        <Form.Control
                                            type="number"
                                            name="pricePerUnit"
                                            required
                                            min="0"
                                            max={100000000}
                                            step="1000"
                                            placeholder="Ví dụ: 50000"
                                            value={formData.pricePerUnit}
                                            onChange={handleInputChange}
                                            className="py-2"
                                        />
                                        <InputGroup.Text className="bg-light fw-medium text-secondary">VNĐ</InputGroup.Text>
                                    </InputGroup>

                                    {formData.pricePerUnit !== '' && !Number.isNaN(formData.pricePerUnit) && (
                                        <Form.Text className="text-success fw-medium mt-2 d-block">
                                            Giá trị: {formatCurrency(formData.pricePerUnit)}
                                        </Form.Text>
                                    )}
                                </Form.Group>

                                <Button
                                    type="submit"
                                    disabled={isSubmitting}
                                    className="w-100 py-2 mt-3 fw-medium d-flex justify-content-center align-items-center gap-2 custom-btn-general"
                                >
                                    <BsCheckCircle size={18} />
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
