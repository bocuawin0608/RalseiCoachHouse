import { Button, Card, Col, Form, Row } from 'react-bootstrap';
import { useCargoTypes } from '../../cargo/hooks/useCargoTypes';
import { useEffect } from 'react';

function Field({ label, ...props }) {
    return (
        <Form.Group>
            <Form.Label className="fw-semibold">
                {label}{props.required ? ' *' : ''}
            </Form.Label>
            <Form.Control {...props} />
        </Form.Group>
    );
}

export default function CargoTicketDetailSection({ draftDetails, onAdd, onChange, onRemove }) {
    const { cargoTypes, setPageInfo } = useCargoTypes();

    useEffect(() => {
        setPageInfo(prev => ({ ...prev, size: 100 }));
    }, [setPageInfo]);

    return (
        <Card className="shadow-sm border-0 mb-4">
            <Card.Header className="bg-white py-3 d-flex justify-content-between align-items-center">
                <h5 className="fw-bold mb-0">Chi tiết hàng hóa</h5>
                <Button className='custom-btn-general fw-medium' size="sm" onClick={onAdd}>Thêm hàng hóa</Button>
            </Card.Header>
            <Card.Body className="p-4">
                {draftDetails.length === 0 ? (
                    <p className="text-muted mb-0">Chưa có chi tiết hàng hóa.</p>
                ) : (
                    draftDetails.map((detail, index) => (
                        <Row key={index} className="g-3 mb-3 pb-3 border-bottom">
                            <Col md={4}>
                                <Form.Group>
                                    <Form.Label className="fw-semibold">Loại hàng *</Form.Label>
                                    <Form.Select
                                        value={detail.cargoTypePriceId || ''}
                                        onChange={(e) => onChange(index, 'cargoTypePriceId', e.target.value)}
                                        required
                                    >
                                        <option value="">-- Chọn loại hàng --</option>
                                        {cargoTypes.map(ct => ct.cargoTypePriceId ? (
                                            <option key={ct.cargoTypePriceId} value={ct.cargoTypePriceId}>
                                                {ct.cargoTypeName} - {Number(ct.pricePerUnit).toLocaleString('vi-VN')} đ/{ct.unit}
                                            </option>
                                        ) : null)}
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                            <Col md={2}>
                                <Field label="Số lượng" type="number" required min="1" onChange={(e) => onChange(index, 'quantity', e.target.value)} />
                            </Col>
                            <Col md={2}>
                                <Field label="Trọng lượng (kg)" type="number" required min="1" onChange={(e) => onChange(index, 'weightKg', e.target.value)} />
                            </Col>
                            <Col md={3}>
                                <Field label="Thể tích (m3)" type="number" required min="1" onChange={(e) => onChange(index, 'dimensionVol', e.target.value)} />
                            </Col>
                            <Col md={1} className="d-flex align-items-end mb-3">
                                <Button variant="outline-danger" onClick={() => onRemove(index)}>Xóa</Button>
                            </Col>
                            <Col md={12}>
                                <Field label="Mô tả" as="textarea" rows={1} value={detail.description || ''} onChange={(e) => onChange(index, 'description', e.target.value)} />
                            </Col>
                        </Row>
                    ))
                )}
            </Card.Body>
        </Card>
    );
}
