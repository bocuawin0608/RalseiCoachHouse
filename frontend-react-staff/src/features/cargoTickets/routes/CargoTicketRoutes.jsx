import { Route } from 'react-router-dom';
import CargoTicketPage from '../../../pages/ticket-staff/cargo-tickets/CargoTicketPage';
import CargoTicketCreatePage from '../../../pages/ticket-staff/cargo-tickets/CargoTicketCreatePage';
import CargoSendPage from '../../../pages/ticket-staff/cargo-tickets/CargoSendPage';
import CargoTripAssignPage from '../../../pages/ticket-staff/cargo-tickets/CargoTripAssignPage';
import CargoCheckPage from '../../../pages/ticket-staff/cargo-tickets/CargoCheckPage';

export const cargoTicketRoutes = [
    <Route key="cargo-tickets" path="cargo-tickets" element={<CargoTicketPage />} />,
    <Route key="cargo-tickets-send" path="cargo-tickets/send" element={<CargoSendPage />} />,
    <Route key="cargo-tickets-assign" path="cargo-tickets/send/assign/:tripId" element={<CargoTripAssignPage />} />,
    <Route key="cargo-tickets-check" path="cargo-tickets/check" element={<CargoCheckPage />} />,
    <Route key="cargo-tickets-create" path="cargo-tickets/create" element={<CargoTicketCreatePage />} />
];
