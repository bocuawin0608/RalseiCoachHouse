import { BsArrowClockwise } from 'react-icons/bs';
import { Button, Card, Form } from 'react-bootstrap';
import './TripFilter.css';

/**
 * Filter bar for the Trip list page.
 * Renders a date picker and a reset button.
 */
export default function TripFilter({ filters, onFilterChange, onReset }) {
    return (
        <Card className="trip-filter-card">
            <Card.Body className="trip-filter-body">
                <Form className="trip-filter-form">

                    {/* Date filter - maps to ?date= query param */}
                    <Form.Control
                        type="date"
                        name="date"
                        value={filters.date || ''}
                        onChange={onFilterChange}
                        className="trip-filter-input"
                    />

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
