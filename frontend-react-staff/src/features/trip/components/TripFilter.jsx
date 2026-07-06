import { BsArrowClockwise } from 'react-icons/bs';
import { Button, Card, Form } from 'react-bootstrap';
import './TripFilter.css';

/**
 * Filter bar for the Trip list page.
 * Renders clear, user-facing date, direction and time-of-day controls.
 */
export default function TripFilter({ filters, routes, onFilterChange, onReset }) {
    const today = new Date().toLocaleDateString('en-CA');

    /** Prevent manually typed past years from ever reaching the trips endpoint. */
    const handleDateChange = (event) => {
        const value = event.target.value;
        onFilterChange({
            target: { name: 'date', value: value && value < today ? today : value }
        });
    };

    return (
        <Card className="trip-filter-card">
            <Card.Body className="trip-filter-body">
                <Form className="trip-filter-form">

                    <div className="trip-filter-today">
                        <span>Ngày:</span>
                        <strong>{filters.date ? new Date(`${filters.date}T00:00:00`).toLocaleDateString('vi-VN') : ''}</strong>
                    </div>

                    {/* Date filter - maps to ?date= query param */}
                    <Form.Control
                        type="date"
                        name="date"
                        value={filters.date || ''}
                        onChange={handleDateChange}
                        min={today}
                        className="trip-filter-input"
                    />

                    <div className="trip-filter-choice-group" aria-label="Chiều tuyến">
                        {routes.map((route) => (
                            <Button key={route.routeId} name="routeId" value={route.routeId} onClick={onFilterChange}
                                variant={filters.routeId === String(route.routeId) ? 'success' : 'outline-success'}>
                                {route.routeName.replace(/\s*-\s*/, ' → ')}
                            </Button>
                        ))}
                    </div>

                    <div className="trip-filter-choice-group" aria-label="Buổi khởi hành">
                        <Button name="period" value="MORNING" onClick={onFilterChange}
                            variant={filters.period === 'MORNING' ? 'success' : 'outline-success'}>
                            Chuyến Sáng
                        </Button>
                        <Button name="period" value="EVENING" onClick={onFilterChange}
                            variant={filters.period === 'EVENING' ? 'success' : 'outline-success'}>
                            Chuyến tối
                        </Button>
                    </div>

                    {/* Reset all filters */}
                    <Button
                        variant="outline-secondary"
                        onClick={onReset}
                        size="lg"
                        className="trip-filter-reset-btn"
                        title="Làm mới bộ lọc"
                    >
                        <BsArrowClockwise size={18} />
                    </Button>

                </Form>
            </Card.Body>
        </Card>
    );
}
