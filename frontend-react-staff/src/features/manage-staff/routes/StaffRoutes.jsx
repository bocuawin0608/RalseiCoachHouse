import { Route } from 'react-router-dom';
import StaffListPage from '../../../pages/admin/StaffListPage';

const staffRoutes = (
    <Route path="manage-staff" element={<StaffListPage />} />
);

export default staffRoutes;
