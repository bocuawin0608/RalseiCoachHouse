import { Route } from 'react-router-dom';
import TicketAgencyListPage from '../../../pages/admin/TicketAgencyListPage';

const ticketAgencyRoutes = (
    <Route path="manage-ticket-agencies" element={<TicketAgencyListPage />} />
);

export default ticketAgencyRoutes;
