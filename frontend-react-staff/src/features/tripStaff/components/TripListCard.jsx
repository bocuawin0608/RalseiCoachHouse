import { Badge } from 'react-bootstrap';
import { formatDateTime } from '../../../utils/formatters';
import './TripStaff.css';

const STATUS_VARIANT = {
    SCHEDULED: 'success',
    IN_PROGRESS: 'primary',
    CANCELLED: 'danger',
    COMPLETED: 'secondary',
};

const ROLE_LABEL = {
    DRIVER: 'Tài xế',
    ATTENDANT: 'Phụ xe',
};

export default function TripListCard({ trip, onClick }) {
    return (
        <div className="trip-list-card" onClick={onClick} role="button" tabIndex={0}>
            <div className="d-flex justify-content-between align-items-start gap-2 mb-2">
                <div className="trip-list-card-title">{trip.routeName}</div>
                <Badge bg={STATUS_VARIANT[trip.tripStatus] || 'warning'}>{trip.tripStatus}</Badge>
            </div>
            <div className="trip-list-card-meta mb-1">
                {formatDateTime(trip.departureTime)} · {trip.licensePlate}
            </div>
            <div className="trip-list-card-meta mb-1">{trip.coachTypeName}</div>
            <div className="d-flex justify-content-between align-items-center mt-2">
                <span className="trip-list-card-meta">
                    Check-in: {trip.checkedInCount}/{trip.totalPassengers}
                </span>
                <Badge bg="dark">{ROLE_LABEL[trip.assignedRole] || trip.assignedRole}</Badge>
            </div>
        </div>
    );
}
