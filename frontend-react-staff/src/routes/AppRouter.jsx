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
import StaffProfilePage from '../pages/staff/StaffProfilePage';
// Nested routes
import { routeRoutes } from '../features/routes';
import { coachRoutes } from '../features/coaches';
import { voucherRoutes } from '../features/vouchers';
import { cargoRoutes } from '../features/cargo';
import { tripRoutes } from '../features/trip';
import { cargoTicketRoutes } from '../features/cargoTickets';
import { accountRoutes } from '../features/manage-accounts';
import { roleRoutes } from '../features/manage-roles';
import { customerRoutes } from '../features/manage-customers';
import { ticketAgencyRoutes } from '../features/manage-ticket-agencies';
import { staffRoutes } from '../features/manage-staff';
import { tripStaffRoutes } from '../features/tripStaff';
import { passengerTicketRoutes } from '../features/passenger-tickets';
import { staffTripInfoRoutes } from '../features/staff-trip-info';
import { refundRoutes } from '../features/refunds';

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
                    <Route path="profile" element={<StaffProfilePage />} />
                    {coachRoutes}
                    {routeRoutes}
                    {cargoRoutes}
                    {voucherRoutes}
                    {tripRoutes}
                    {refundRoutes}

                    {/* route dưới thì chỉ admin vào đc, manager thì ko */}
                    <Route element={<RoleGuard allowedRoles={['ADMIN']} />}>
                        {accountRoutes}
                        {customerRoutes}
                        {ticketAgencyRoutes}
                        {roleRoutes}
                        {staffRoutes}
                    </Route>

                </Route>
            </Route>

            <Route path="/staff">
                <Route element={<RoleGuard allowedRoles={['ADMIN', 'MANAGER', 'TICKET_STAFF', 'TRIP_STAFF']} />}>
                    <Route element={<DesktopLayout />}>
                        <Route path="profile" element={<StaffProfilePage />} />
                    </Route>
                </Route>

                <Route element={<RoleGuard allowedRoles={['TICKET_STAFF']} />}>
                    <Route element={<DesktopLayout />}>
                        <Route path="ticket/sell" element={<div>Bán vé</div>} />
                        <Route path="ticket/history" element={<div>Lịch sử bán</div>} />
                        {cargoTicketRoutes}
                        {passengerTicketRoutes}
                        {staffTripInfoRoutes}
                    </Route>
                </Route>

                <Route element={<RoleGuard allowedRoles={['TRIP_STAFF']} />}>
                    <Route element={<MobileLayout />}>
                        {tripStaffRoutes}
                    </Route>
                </Route>
            </Route>

            <Route path="/unauthorized" element={<div>401 - Unauthorized.</div>} />
            <Route path="*" element={<div>404 - Page Not Found.</div>} />
        </Routes>
    );
};

export default AppRouter;
