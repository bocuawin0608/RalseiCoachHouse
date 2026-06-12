import React from 'react';
import { Card, Form, Row, Col, Button } from 'react-bootstrap';

const VoucherFilter = ({ filters, onFilterChange, onReset }) => {
    return (
        <Card className="mb-3">
            <Card.Body>
                <Form>
                    <Row className="g-3 align-items-end">
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label>Tìm kiếm</Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="Mã voucher..."
                                    value={filters.search || ''}
                                    onChange={(e) => onFilterChange('search', e.target.value)}
                                />
                            </Form.Group>
                        </Col>
                        <Col md={2}>
                            <Form.Group>
                                <Form.Label>Loại giảm</Form.Label>
                                <Form.Select
                                    value={filters.discountType || ''}
                                    onChange={(e) => onFilterChange('discountType', e.target.value)}
                                >
                                    <option value="">Tất cả</option>
                                    <option value="PERCENT">Phần trăm</option>
                                    <option value="FIXED">Cố định</option>
                                </Form.Select>
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label>Từ ngày</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    value={filters.fromDate || ''}
                                    onChange={(e) => onFilterChange('fromDate', e.target.value)}
                                />
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label>Đến ngày</Form.Label>
                                <Form.Control
                                    type="datetime-local"
                                    value={filters.toDate || ''}
                                    onChange={(e) => onFilterChange('toDate', e.target.value)}
                                />
                            </Form.Group>
                        </Col>
                        <Col md={1}>
                            <Button variant="secondary" onClick={onReset} className="w-100">
                                Reset
                            </Button>
                        </Col>
                    </Row>
                </Form>
            </Card.Body>
        </Card>
    );
};

export default VoucherFilter;
