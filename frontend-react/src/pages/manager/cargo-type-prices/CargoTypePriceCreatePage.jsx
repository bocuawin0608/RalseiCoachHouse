import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { cargoTypePriceApi, useCargoTypes } from '../../../features/cargo';
import { Alert, Button, Card, Col, Container, Form, InputGroup, Row } from 'react-bootstrap';
import { formatCurrency } from '../../../utils/formatters';

export default function CargoTypePriceCreatePage() {
    const navigate = useNavigate();
    const { cargoTypes } = useCargoTypes();

    const [formData, setFormData] = useState({
        cargoTypeId: '',
        unit: '',
        pricePerUnit: '',
        startEffectiveDate: '',
        endEffectiveDate: ''
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: name === 'pricePerUnit' || name === 'cargoTypeId' ? (value ? Number(value) : '') : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        if (!formData.cargoTypeId || !formData.unit || !formData.pricePerUnit || !formData.startEffectiveDate || !formData.endEffectiveDate) {
            setErrorMsg('Vui lòng nhập đầy đủ thông tin bắt buộc!');
            return;
        }

        setIsSubmitting(true);

        try {
            const payload = {
                cargoTypeId: formData.cargoTypeId,
                unit: formData.unit,
                pricePerUnit: formData.pricePerUnit,
                startEffectiveDate: formData.startEffectiveDate,
                endEffectiveDate: formData.endEffectiveDate
            };

            await cargoTypePriceApi.createCargoTypePrice(payload);

            navigate('/manager/freight-rates');
        } catch (error) {
            console.error("Lỗi tạo giá cước:", error);
            setErrorMsg(error.response?.data?.message || 'Có lỗi xảy ra khi lưu vào hệ thống.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '800px' }}>

            <Button
                variant="link"
                onClick={() => navigate('/manager/freight-rates')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18} /> Quay lại danh sách
            </Button>

            <h2 className="mb-4 text-dark fw-bold">Thêm mới giá cước</h2>

            {errorMsg && (
                <Alert variant="danger" className="shadow-sm border-0 d-flex align-items-center gap-2">
                    <BsExclamationTriangleFill />
                    <span>{errorMsg}</span>
                </Alert>
            )}

            <Form onSubmit={handleSubmit}>
                <Card className="shadow-sm border-0 h-100">
                    <Card.Header className="bg-white border-bottom pt-3 pb-2">
                        <h5 className="fw-bold mb-0 text-dark">Thông tin cơ bản</h5>
                    </Card.Header>
                    <Card.Body className="p-4 d-flex flex-column gap-3">

                        <Row className="g-3">
                            <Col md={12}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Loại hàng <span className="text-danger">*</span></Form.Label>
                                    <Form.Select
                                        name="cargoTypeId"
                                        required
                                        value={formData.cargoTypeId}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    >
                                        <option value="" disabled>-- Chọn loại hàng --</option>
                                        {cargoTypes && cargoTypes.filter(type => type.isActive === true || type.active === true).map(type => (
                                            <option key={type.cargoTypeId} value={type.cargoTypeId}>
                                                {type.cargoTypeName}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                        </Row>

                        <Row className="g-3">
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Đơn vị tính <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="unit"
                                        required
                                        maxLength={50}
                                        placeholder="Ví dụ: kg, m3..."
                                        value={formData.unit}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group className="mb-4">
                                    <Form.Label className="fw-semibold text-secondary mb-1">Đơn giá <span className="text-danger">*</span></Form.Label>

                                    <InputGroup>
                                        <Form.Control
                                            type="number"
                                            name="pricePerUnit"
                                            required
                                            min="0" max={100000000}
                                            placeholder="Ví dụ: 50000"
                                            value={formData.pricePerUnit}
                                            onChange={handleInputChange}
                                            className="py-2"
                                        />
                                        <InputGroup.Text className="bg-light fw-medium text-secondary">VNĐ</InputGroup.Text>
                                    </InputGroup>

                                    {formData.pricePerUnit !== '' && !isNaN(formData.pricePerUnit) && (
                                        <Form.Text className="text-success fw-medium mt-2 d-block">
                                            Giá trị: {formatCurrency(formData.pricePerUnit)}
                                        </Form.Text>
                                    )}
                                </Form.Group>
                            </Col>
                        </Row>

                        <Row className="g-3">
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Bắt đầu hiệu lực <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="datetime-local"
                                        name="startEffectiveDate"
                                        required
                                        value={formData.startEffectiveDate}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Kết thúc hiệu lực <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="datetime-local"
                                        name="endEffectiveDate"
                                        required
                                        value={formData.endEffectiveDate}
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Button
                            type="submit"
                            variant="primary"
                            disabled={isSubmitting}
                            className="w-100 py-3 mt-4 fw-medium d-flex justify-content-center align-items-center gap-2 fs-5"
                        >
                            <BsCheckCircle size={20} />
                            {isSubmitting ? 'Đang lưu hệ thống...' : 'Tạo giá cước'}
                        </Button>

                    </Card.Body>
                </Card>
            </Form>

        </Container>
    );
}
