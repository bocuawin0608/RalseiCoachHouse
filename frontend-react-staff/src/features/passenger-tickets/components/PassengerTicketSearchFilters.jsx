import { BsArrowClockwise } from 'react-icons/bs';
import { Badge, Button, Card, Col, Dropdown, Form, Row } from 'react-bootstrap';
import { useRouteDropdown } from '../../../hooks/useRouteDropdown';
import { hasVisibleSearchFilter, TICKET_STATUS_LABELS } from '../utils/passengerTicketFormatters';

export default function PassengerTicketSearchFilters({
    filters,
    onFilterChange,
    onStatusCheckboxChange,
    onReset,
    onSearch,
    searching,
}) {
    const { routes, loading: routesLoading } = useRouteDropdown(true);
    const canSearch = hasVisibleSearchFilter(filters);
    const selectedStatuses = filters.statuses || [];

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
                            <Dropdown>
                                <Dropdown.Toggle
                                    variant="outline-secondary"
                                    className="w-100 text-start d-flex justify-content-between align-items-center border"
                                    style={{ backgroundColor: '#fff', color: '#495057' }}
                                >
                                    <span className="text-truncate">
                                        {selectedStatuses.length === 0
                                            ? 'Tất cả trạng thái'
                                            : `Đã chọn (${selectedStatuses.length})`}
                                    </span>
                                </Dropdown.Toggle>
                                <Dropdown.Menu
                                    className="p-3 w-100 shadow-sm"
                                    onClick={(event) => event.stopPropagation()}
                                >
                                    {Object.keys(TICKET_STATUS_LABELS).map((statusKey) => (
                                        <Form.Check
                                            key={statusKey}
                                            type="checkbox"
                                            id={`passenger-ticket-status-${statusKey}`}
                                            label={(
                                                <Badge bg={TICKET_STATUS_LABELS[statusKey].bg} className="ms-1">
                                                    {TICKET_STATUS_LABELS[statusKey].text}
                                                </Badge>
                                            )}
                                            name="statuses"
                                            value={statusKey}
                                            checked={selectedStatuses.includes(statusKey)}
                                            onChange={onStatusCheckboxChange}
                                            className="mb-2"
                                        />
                                    ))}
                                </Dropdown.Menu>
                            </Dropdown>
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
