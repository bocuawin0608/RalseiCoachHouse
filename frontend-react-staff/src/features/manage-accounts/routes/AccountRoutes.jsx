import { Route } from 'react-router-dom';
import AccountListPage from '../../../pages/admin/AccountListPage';

const accountRoutes = (
    <Route path="manage-accounts" element={<AccountListPage />} />
);

export default accountRoutes;
