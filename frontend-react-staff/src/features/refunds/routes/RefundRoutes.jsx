import { Route } from 'react-router-dom';
import RefundListPage from '../../../pages/manager/refunds/RefundListPage';

export const refundRoutes = (
    <Route key="refunds-group" path="refunds">
        <Route index element={<RefundListPage />} />
    </Route>
);
