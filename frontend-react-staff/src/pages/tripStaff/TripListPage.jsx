import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Spinner } from 'react-bootstrap';
import TripListCard from '../../features/tripStaff/components/TripListCard';
import { useAssignedTrips } from '../../features/tripStaff/hooks/useAssignedTrips';
import '../../features/tripStaff/components/TripStaff.css';

function toDateString(offsetDays = 0) {
    const d = new Date();
    d.setDate(d.getDate() + offsetDays);
    return d.toISOString().slice(0, 10);
}

export default function TripListPage() {
    const navigate = useNavigate();
    const [dayOffset, setDayOffset] = useState(0);
    const date = useMemo(() => toDateString(dayOffset), [dayOffset]);
    const { trips, loading, error } = useAssignedTrips(date);

    return (
        <div className="trip-staff-page">
            <h5 className="fw-bold mb-3" style={{ color: 'var(--ralsei-black)' }}>
                Chuyến được phân công
            </h5>

            <div className="trip-staff-date-toggle">
                <button
                    type="button"
                    className={`trip-staff-date-btn ${dayOffset === 0 ? 'active' : ''}`}
                    onClick={() => setDayOffset(0)}
                >
                    Hôm nay
                </button>
                <button
                    type="button"
                    className={`trip-staff-date-btn ${dayOffset === 1 ? 'active' : ''}`}
                    onClick={() => setDayOffset(1)}
                >
                    Ngày mai
                </button>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            {loading ? (
                <div className="text-center py-5">
                    <Spinner animation="border" />
                </div>
            ) : trips.length === 0 ? (
                <p className="text-muted text-center py-4">Không có chuyến nào {dayOffset === 1 ? 'trong ngày mai' : 'trong ngày này'}.</p>
            ) : (
                trips.map((trip) => (
                    <TripListCard
                        key={trip.tripId}
                        trip={trip}
                        onClick={() => navigate(`/staff/trip/${trip.tripId}/dashboard`)}
                    />
                ))
            )}
        </div>
    );
}
