import { Route } from 'react-router-dom';
import CargoTicketPage from '../../../pages/ticket-staff/cargo-tickets/CargoTicketPage';
import CargoTicketCreatePage from '../../../pages/ticket-staff/cargo-tickets/CargoTicketCreatePage';

export const cargoTicketRoutes = [
    <Route key="cargo-tickets" path="cargo-tickets" element={<CargoTicketPage />} />,
    <Route key="cargo-tickets-create" path="cargo-tickets/create" element={<CargoTicketCreatePage />} />
];
