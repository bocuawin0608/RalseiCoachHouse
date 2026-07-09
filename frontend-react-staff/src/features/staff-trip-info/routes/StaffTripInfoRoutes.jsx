import { Route } from 'react-router-dom';
import StaffTripInfoPage from '../../../pages/ticket-staff/trips/StaffTripInfoPage';

/** Ticket-staff routes for operational trip lookup. */
export const staffTripInfoRoutes = (
    <Route path="trips/info" element={<StaffTripInfoPage />} />
);
