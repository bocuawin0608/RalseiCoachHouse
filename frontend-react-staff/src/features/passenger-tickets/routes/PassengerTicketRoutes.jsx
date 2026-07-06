import { Route, Navigate } from 'react-router-dom';
import PassengerTicketSearchPage from '../../../pages/ticket-staff/passenger-tickets/PassengerTicketSearchPage';

export const passengerTicketRoutes = (
    <>
        <Route path="passenger-tickets">
            <Route index element={<Navigate to="search" replace />} />
            <Route path="search" element={<PassengerTicketSearchPage />} />
        </Route>
    </>
);
