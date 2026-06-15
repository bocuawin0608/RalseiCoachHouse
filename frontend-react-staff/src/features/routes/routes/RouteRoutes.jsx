import { Route } from 'react-router-dom'
import AdminDashboard from '../../../pages/manager/routes/AdminDashboard'
export const routeRoutes = [
    <Route index path="routes" element={<AdminDashboard/>}>
        {/* hiện thành nhúng 2 cái component kia dạng modal, not page nên ko có link để access nested ở đây */}
    </Route>
]