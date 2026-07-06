import { Navigate, Routes, Route } from 'react-router-dom';
// Layouts
import PublicLayout from '../components/layout/PublicLayout/PublicLayout';
import AuthLayout from '../components/layout/AuthLayout/AuthLayout';
// Guards
import GuestGuard from './GuestGuard';
import RoleGuard from './RoleGuard';
// Pages
import HomePage from '../pages/public/home/HomePage';
import CargoTrackingPage from '../pages/public/cargo-tracking/CargoTrackingPage';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import BookingPaymentPage from '../pages/public/booking/BookingPaymentPage';
import CargoHistoryPage from '../pages/customer/CargoHistoryPage';
import BookingTripPage from '../pages/public/booking/BookingTripPage';
// Nested routes
import { publicTripRoutes } from '../features/trips';
import { customerHistoryRoutes } from '../features/customerHistory';
import { cargoLookupRoutes } from '../features/cargoLookup';

const AppRouter = () => {
    return (
        <Routes>
            <Route element={<PublicLayout />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/tra-cuu" element={<CargoTrackingPage />} />
                <Route path="/booking/trip/:tripId" element={<BookingTripPage />} />
                <Route path="/booking/payment/:transactionId" element={<BookingPaymentPage />} />
                <Route path="/unauthorized" element={<div>401 - Unauthorized.</div>} />
                <Route path="*" element={<div>404 - Page Not Found.</div>} />
                {publicTripRoutes}
            </Route>

            <Route element={<GuestGuard />}>
                <Route element={<AuthLayout />}>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                </Route>
            </Route>

            <Route element={<RoleGuard allowedRoles={['CUSTOMER']} />}>
                <Route element={<PublicLayout />}>
                    <Route path="/profile" element={<div>Trang cá nhân của khách</div>} />
                    <Route path="/booking-history" element={<Navigate to="/history" replace />} />
                    <Route path="/cargo-history" element={<CargoHistoryPage />} />
                    {customerHistoryRoutes}
                    {cargoLookupRoutes}
                </Route>
            </Route>

        </Routes>
    );
};

export default AppRouter;
