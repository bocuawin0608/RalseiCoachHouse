import { Route } from 'react-router-dom';
import RoleListPage from '../../../pages/admin/RoleListPage';

const roleRoutes = (
    <Route path="manage-roles" element={<RoleListPage />} />
);

export default roleRoutes;
