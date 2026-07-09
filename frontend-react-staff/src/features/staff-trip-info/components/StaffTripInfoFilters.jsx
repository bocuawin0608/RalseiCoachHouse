import { useMemo } from 'react';
import { BsArrowClockwise, BsSearch } from 'react-icons/bs';
import { Button, Card, Col, Form, Row } from 'react-bootstrap';
import { useRouteDropdown } from '../../../hooks/useRouteDropdown';

const PRICE_OPTIONS = [
    { value: 'LOW', label: 'Dưới 300.000đ' },
    { value: 'MIDDLE', label: '300.000đ - 500.000đ' },
    { value: 'HIGH', label: 'Trên 500.000đ' },
];

const STATUS_OPTIONS = [
    { value: '', label: 'Tất cả' },
    { value: 'SCHEDULED', label: 'Sắp khởi hành' },
    { value: 'IN_PROGRESS', label: 'Đang chạy' },
    { value: 'COMPLETED', label: 'Hoàn thành' },
    { value: 'CANCELLED', label: 'Đã hủy' },
];

const COACH_TYPE_OPTIONS = [
    { value: 'LIMOUSINE', label: 'Limousine' },
    { value: 'LUXURY', label: 'Luxury' },
    { value: 'TRUYEN_THONG', label: 'Truyền thống' },
];

/** Extracts the departure city from route display text used by current data. */
const getDepartureCity = (routeName = '') => {
    if (routeName.includes('→')) return routeName.split('→')[0].trim();
    if (routeName.includes(' - ')) return routeName.split(' - ')[0].trim();
    return routeName.trim();
};

/**
 * Renders all search controls for ticket staff trip lookup.
 * The checkbox groups intentionally map to backend List<String> params.
 */
export default function StaffTripInfoFilters({
    filters,
    minDate,
    onFilterChange,
    onCheckboxChange,
    onReset,
}) {
    const { routes, loading: routesLoading } = useRouteDropdown(true);

    const cities = useMemo(() => {
        const names = routes.map((route) => getDepartureCity(route.routeName)).filter(Boolean);
        return [...new Set(names)].sort((left, right) => left.localeCompare(right, 'vi'));
    }, [routes]);

    return (
        <Card className="staff-trip-info-filter-card">
            <Card.Body>
                <Form>
                    <Row className="g-3 align-items-end">
                        <Col md={6} xl={2}>
                            <Form.Label>Ngày khởi hành</Form.Label>
                            <Form.Control
                                type="date"
                                name="date"
                                value={filters.date}
                                min={minDate}
                                onChange={onFilterChange}
                                required
                            />
                        </Col>

                        <Col md={6} xl={2}>
                            <Form.Label>Thành phố</Form.Label>
                            <Form.Select
                                name="city"
                                value={filters.city}
                                onChange={onFilterChange}
                                disabled={routesLoading}
                            >
                                <option value="">Tất cả thành phố</option>
                                {cities.map((city) => (
                                    <option key={city} value={city}>{city}</option>
                                ))}
                            </Form.Select>
                        </Col>

                        <Col md={6} xl={3}>
                            <Form.Label>Tài xế phụ trách</Form.Label>
                            <div className="staff-trip-info-search-control">
                                <BsSearch />
                                <Form.Control
                                    name="driverName"
                                    value={filters.driverName}
                                    onChange={onFilterChange}
                                    placeholder="Nhập tên tài xế"
                                />
                            </div>
                        </Col>

                        <Col md={6} xl={1}>
                            <Form.Label>Từ giờ</Form.Label>
                            <Form.Control
                                type="time"
                                name="timeFrom"
                                value={filters.timeFrom}
                                onChange={onFilterChange}
                            />
                        </Col>

                        <Col md={6} xl={1}>
                            <Form.Label>Đến giờ</Form.Label>
                            <Form.Control
                                type="time"
                                name="timeTo"
                                value={filters.timeTo}
                                onChange={onFilterChange}
                            />
                        </Col>

                        <Col md={6} xl={3}>
                            <Form.Label>Loại xe</Form.Label>
                            <Form.Select
                                name="coachTypeKeyword"
                                value={filters.coachTypeKeyword}
                                onChange={onFilterChange}
                            >
                                <option value="">Tất cả loại xe</option>
                                {COACH_TYPE_OPTIONS.map((coachType) => (
                                    <option key={coachType.value} value={coachType.value}>
                                        {coachType.label}
                                    </option>
                                ))}
                            </Form.Select>
                        </Col>
                    </Row>

                    <div className="staff-trip-info-checkbox-grid">
                        <fieldset>
                            <legend>Giá vé</legend>
                            {PRICE_OPTIONS.map((option) => (
                                <Form.Check
                                    key={option.value}
                                    type="checkbox"
                                    id={`price-${option.value}`}
                                    label={option.label}
                                    checked={filters.priceRanges.includes(option.value)}
                                    onChange={() => onCheckboxChange('priceRanges', option.value)}
                                />
                            ))}
                        </fieldset>

                        <fieldset>
                            <legend>Trạng thái chuyến</legend>
                            {STATUS_OPTIONS.map((option) => (
                                <Form.Check
                                    key={option.value}
                                    type="radio"
                                    name="tripStatus"
                                    id={`status-${option.value || 'ALL'}`}
                                    label={option.label}
                                    checked={(filters.statuses[0] || '') === option.value}
                                    onChange={() => onCheckboxChange('statuses', option.value)}
                                />
                            ))}
                        </fieldset>

                        <div className="staff-trip-info-filter-actions">
                            <Button
                                type="button"
                                variant="outline-secondary"
                                onClick={onReset}
                                className="d-inline-flex align-items-center gap-2"
                            >
                                <BsArrowClockwise />
                                Làm mới
                            </Button>
                        </div>
                    </div>
                </Form>
            </Card.Body>
        </Card>
    );
}
