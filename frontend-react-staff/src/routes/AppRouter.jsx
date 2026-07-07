import { Routes, Route, Navigate } from 'react-router-dom';
// Layouts
import StaffAuthLayout from '../components/layout/AuthLayout/StaffAuthLayout';
import DesktopLayout from '../components/layout/DesktopStaffLayout/DesktopLayout';
import MobileLayout from '../components/layout/MobileStaffLayout/MobileLayout';
// Guards
import GuestGuard from './GuestGuard';
import RoleGuard from './RoleGuard';
// Pages
import StaffLogin from '../pages/auth/StaffLogin';
// Nested routes
import { routeRoutes } from '../features/routes';
import { coachRoutes } from '../features/coaches';
import { voucherRoutes } from '../features/vouchers';
import { cargoRoutes } from '../features/cargo';
import { tripRoutes } from '../features/trip';
import { passengerTicketRoutes } from '../features/passenger-tickets';
import { staffTripInfoRoutes } from '../features/staff-trip-info';

const AppRouter = () => {
    return (
        <Routes>

            <Route element={<GuestGuard />}>
                <Route element={<StaffAuthLayout />}>
                    <Route path="/" element={<Navigate to="/staff/login" replace />} />
                    <Route path="/staff/login" element={<StaffLogin />} />
                </Route>
            </Route>

            <Route element={<RoleGuard allowedRoles={['MANAGER', 'ADMIN']} />}>
                <Route path="/management" element={<DesktopLayout />}>
                    <Route path="dashboard" element={<div>Bảng điều khiển quản trị</div>} />
                    {coachRoutes}
                    {routeRoutes}
                    {cargoRoutes}
                    {voucherRoutes}
                    {tripRoutes}
                    
                    {/* route dưới thì chỉ admin vào đc, manager thì ko */}
                    <Route element={<RoleGuard allowedRoles={['ADMIN']} />}>
                        <Route path="system-config" element={<div>Cấu hình hệ thống</div>} />
                        <Route path="manage-accounts" element={<div>Phân quyền tài khoản</div>} />
                    </Route>

                </Route>
            </Route>

            <Route path="/staff">
                <Route element={<RoleGuard allowedRoles={['TICKET_STAFF']} />}>
                    <Route element={<DesktopLayout />}>
                        {passengerTicketRoutes}
                        {staffTripInfoRoutes}
                    </Route>
                </Route>

                <Route element={<RoleGuard allowedRoles={['TRIP_STAFF']} />}>
                    <Route element={<MobileLayout />}> 
                        <Route path="trip/scan" element={<div>Quét vé</div>} />
                        <Route path="trip/status" element={<div>Cập nhật trạng thái xe</div>} />
                    </Route>
                </Route>
            </Route>

            <Route path="/unauthorized" element={<div>401 - Unauthorized.</div>} />
            <Route path="*" element={<div>404 - Page Not Found.</div>} />
        </Routes>
    );
};

export default AppRouter;
