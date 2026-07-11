import { Button, Col, Form, Row } from 'react-bootstrap';

const STATUS_OPTIONS = [
    { value: 'PENDING', label: 'Chờ xử lý' },
    { value: 'COMPLETED', label: 'Đã hoàn' },
    { value: 'FAILED', label: 'Thất bại' },
    { value: '', label: 'Tất cả' },
];

export default function RefundSearchFilters({
    filters,
    onFilterChange,
    onReset,
    onSearch,
    searching,
    disabled,
}) {
    const handleSubmit = (event) => {
        event.preventDefault();
        onSearch();
    };

    return (
        <Form onSubmit={handleSubmit} className="mb-4">
            <Row className="g-3 align-items-end">
                <Col md={2}>
                    <Form.Group>
                        <Form.Label className="fw-semibold text-secondary">Trạng thái</Form.Label>
                        <Form.Select
                            name="status"
                            value={filters.status}
                            onChange={onFilterChange}
                            disabled={disabled}
                        >
                            {STATUS_OPTIONS.map((option) => (
                                <option key={option.value || 'ALL'} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </Form.Select>
                    </Form.Group>
                </Col>

                <Col md={2}>
                    <Form.Group>
                        <Form.Label className="fw-semibold text-secondary">Mã vé</Form.Label>
                        <Form.Control
                            type="text"
                            name="ticketCode"
                            value={filters.ticketCode}
                            onChange={onFilterChange}
                            placeholder="Nhập mã vé"
                            disabled={disabled}
                        />
                    </Form.Group>
                </Col>

                <Col md={2}>
                    <Form.Group>
                        <Form.Label className="fw-semibold text-secondary">SĐT khách</Form.Label>
                        <Form.Control
                            type="text"
                            name="phone"
                            value={filters.phone}
                            onChange={onFilterChange}
                            placeholder="Nhập SĐT"
                            disabled={disabled}
                        />
                    </Form.Group>
                </Col>

                <Col md={2}>
                    <Form.Group>
                        <Form.Label className="fw-semibold text-secondary">Từ ngày</Form.Label>
                        <Form.Control
                            type="date"
                            name="createdFrom"
                            value={filters.createdFrom}
                            onChange={onFilterChange}
                            disabled={disabled}
                        />
                    </Form.Group>
                </Col>

                <Col md={2}>
                    <Form.Group>
                        <Form.Label className="fw-semibold text-secondary">Đến ngày</Form.Label>
                        <Form.Control
                            type="date"
                            name="createdTo"
                            value={filters.createdTo}
                            onChange={onFilterChange}
                            disabled={disabled}
                        />
                    </Form.Group>
                </Col>

                <Col md={2} className="d-flex gap-2">
                    <Button type="submit" className="custom-btn-general flex-grow-1" disabled={searching || disabled}>
                        {searching ? 'Đang tải…' : 'Tìm kiếm'}
                    </Button>
                    <Button type="button" variant="outline-secondary" onClick={onReset} disabled={searching || disabled}>
                        Xóa lọc
                    </Button>
                </Col>
            </Row>
        </Form>
    );
}
