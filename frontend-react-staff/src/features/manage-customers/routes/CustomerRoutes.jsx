import { Route } from 'react-router-dom';
import CustomerListPage from '../../../pages/admin/CustomerListPage';

const customerRoutes = (
    <Route path="manage-customers" element={<CustomerListPage />} />
);

export default customerRoutes;
