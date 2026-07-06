import { BsArrowClockwise } from 'react-icons/bs';
import { Button, Card, Col, Form, Row } from 'react-bootstrap';
import { useRouteDropdown } from '../../../hooks/useRouteDropdown';
import { hasVisibleSearchFilter } from '../utils/passengerTicketFormatters';

export default function PassengerTicketSearchFilters({
    filters,
    onFilterChange,
    onReset,
    onSearch,
    searching,
}) {
    const { routes, loading: routesLoading } = useRouteDropdown(true);
    const canSearch = hasVisibleSearchFilter(filters);

    const handleSubmit = (event) => {
        event.preventDefault();
        if (canSearch) onSearch();
    };

    return (
        <Card className="mb-4 shadow-sm border-0">
            <Card.Body className="p-4">
                <Form onSubmit={handleSubmit}>
                    <Row className="g-3 align-items-end">
                        <Col md={6} lg={3}>
                            <Form.Label className="small fw-semibold text-secondary">Số điện thoại</Form.Label>
                            <Form.Control
                                name="phone"
                                placeholder="Nhập SĐT khách"
                                value={filters.phone}
                                onChange={onFilterChange}
                                maxLength={15}
                            />
                        </Col>

                        <Col md={6} lg={3}>
                            <Form.Label className="small fw-semibold text-secondary">Mã vé</Form.Label>
                            <Form.Control
                                name="ticketCode"
                                placeholder="VD: PA2607060001"
                                value={filters.ticketCode}
                                onChange={onFilterChange}
                                maxLength={50}
                            />
                        </Col>

                        <Col md={6} lg={2}>
                            <Form.Label className="small fw-semibold text-secondary">Trạng thái</Form.Label>
                            <Form.Select name="status" value={filters.status} onChange={onFilterChange}>
                                <option value="">Tất cả</option>
                                <option value="PENDING">Đang xử lý</option>
                                <option value="CONFIRMED">Đã xác nhận</option>
                                <option value="CHANGED">Có thay đổi</option>
                                <option value="CANCELLED">Đã hủy</option>
                            </Form.Select>
                        </Col>

                        <Col md={6} lg={2}>
                            <Form.Label className="small fw-semibold text-secondary">Tuyến xe</Form.Label>
                            <Form.Select
                                name="routeId"
                                value={filters.routeId}
                                onChange={onFilterChange}
                                disabled={routesLoading}
                            >
                                <option value="">Tất cả tuyến</option>
                                {routes.map((route) => (
                                    <option key={route.routeId} value={route.routeId}>
                                        {route.routeName}
                                    </option>
                                ))}
                            </Form.Select>
                        </Col>

                        <Col md={6} lg={2}>
                            <Form.Label className="small fw-semibold text-secondary">Ngày khởi hành</Form.Label>
                            <Form.Control
                                type="date"
                                name="departureDate"
                                value={filters.departureDate}
                                onChange={onFilterChange}
                            />
                        </Col>

                        <Col xs={12} className="d-flex flex-wrap gap-2 justify-content-end">
                            <Button
                                type="button"
                                variant="outline-secondary"
                                onClick={onReset}
                                className="d-flex align-items-center gap-2"
                            >
                                <BsArrowClockwise />
                                Làm mới
                            </Button>
                            <Button
                                type="submit"
                                className="custom-btn-general px-4"
                                disabled={!canSearch || searching}
                            >
                                {searching ? 'Đang tìm...' : 'Tìm kiếm'}
                            </Button>
                        </Col>
                    </Row>
                </Form>
            </Card.Body>
        </Card>
    );
}
