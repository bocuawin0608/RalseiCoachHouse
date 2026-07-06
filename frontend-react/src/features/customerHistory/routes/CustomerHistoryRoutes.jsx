import { Route } from 'react-router-dom';
import CustomerHistoryDetailPage from '../components/CustomerHistoryDetailPage';
import CustomerHistoryPage from '../components/CustomerHistoryPage';

/**
 * Keeps customer-history route declarations inside the owning feature.
 */
export const customerHistoryRoutes = (
    <>
        <Route path="/history" element={<CustomerHistoryPage />} />
        <Route path="/history/detail/:ticketCode" element={<CustomerHistoryDetailPage />} />
    </>
);
