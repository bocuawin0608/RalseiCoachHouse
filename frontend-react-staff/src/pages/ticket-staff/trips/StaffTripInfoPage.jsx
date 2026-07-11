import { useState } from 'react';
import { Alert, Card, Container } from 'react-bootstrap';
import Pagination from '../../../components/common/Pagination';
import StaffTripInfoFilters from '../../../features/staff-trip-info/components/StaffTripInfoFilters';
import StaffTripInfoTable from '../../../features/staff-trip-info/components/StaffTripInfoTable';
import { useStaffTripInfo } from '../../../features/staff-trip-info/hooks/useStaffTripInfo';
import { TripCrewModal } from '../../../features/trip';
import '../../../features/staff-trip-info/styles/staffTripInfo.css';

/** Formats yyyy-MM-dd values into a compact Vietnamese date label. */
const formatDisplayDate = (dateValue) => {
    if (!dateValue) return '---';
    const [year, month, day] = dateValue.split('-');
    return `${day}/${month}/${year}`;
};

/** Adapts staff-trip ISO departureTime into TripCrewModal's date + time fields. */
const toCrewModalTrip = (trip) => {
    if (!trip?.departureTime || trip.departureDate) return trip;
    const value = new Date(trip.departureTime);
    if (Number.isNaN(value.getTime())) return trip;
    const pad = (n) => String(n).padStart(2, '0');
    return {
        ...trip,
        departureDate: `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())}`,
        departureTime: `${pad(value.getHours())}:${pad(value.getMinutes())}`,
    };
};

/**
 * Ticket-staff page for viewing today-and-future trip information.
 * This screen supports the "view trip info" flow before staff drill into
 * passenger tickets or cargo assignment work.
 */
export default function StaffTripInfoPage() {
    const {
        filters,
        trips,
        loading,
        error,
        pageInfo,
        today,
        setPageInfo,
        handleFilterChange,
        handleCheckboxChange,
        handleReset,
    } = useStaffTripInfo();

    const [crewTrip, setCrewTrip] = useState(null);

    return (
        <Container fluid className="staff-trip-info-page">
            <div className="staff-trip-info-header">
                <div>
                    <h2>Thông tin chuyến xe</h2>
                    <p>Tra cứu các chuyến hôm nay và tương lai theo thành phố, thời gian, loại xe, giá vé, trạng thái và tài xế.</p>
                </div>
                <div className="staff-trip-info-date-summary" aria-label="Thông tin ngày đang xem">
                    <div>
                        <span>Hôm nay</span>
                        <strong>{formatDisplayDate(today)}</strong>
                    </div>
                    <div>
                        <span>Đang xem</span>
                        <strong>{formatDisplayDate(filters.date)}</strong>
                    </div>
                </div>
            </div>

            <StaffTripInfoFilters
                filters={filters}
                minDate={today}
                onFilterChange={handleFilterChange}
                onCheckboxChange={handleCheckboxChange}
                onReset={handleReset}
            />

            {error && <Alert variant="danger" className="staff-trip-info-alert">{error}</Alert>}

            <Card className="staff-trip-info-table-card">
                <Card.Body>
                    <StaffTripInfoTable
                        data={trips}
                        loading={loading}
                        onViewCrew={(trip) => setCrewTrip(toCrewModalTrip(trip))}
                    />
                    <div className="staff-trip-info-pagination">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </div>
                </Card.Body>
            </Card>

            <TripCrewModal
                isOpen={Boolean(crewTrip)}
                trip={crewTrip}
                onClose={() => setCrewTrip(null)}
                ticketsHref={
                    crewTrip?.tripId
                        ? `/staff/passenger-tickets/search?tripId=${encodeURIComponent(crewTrip.tripId)}`
                        : undefined
                }
            />
        </Container>
    );
}
