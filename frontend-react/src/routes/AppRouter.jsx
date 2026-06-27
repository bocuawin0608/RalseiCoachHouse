import { Routes, Route } from 'react-router-dom';
// Layouts
import PublicLayout from '../components/layout/PublicLayout/PublicLayout';
import AuthLayout from '../components/layout/AuthLayout/AuthLayout';
// Guards
import GuestGuard from './GuestGuard';
import RoleGuard from './RoleGuard';
// Pages
import HomePage from '../pages/public/home/HomePage';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
// Nested routes
import { publicTripRoutes } from '../features/trips';
import CheckoutPage from '../pages/public/checkout/CheckoutPage';

const AppRouter = () => {
    return (
        <Routes>
            <Route element={<PublicLayout />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/checkout" element={<CheckoutPage />} />
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
                    <Route path="/booking-history" element={<div>Lịch sử đặt vé</div>} />
                </Route>
            </Route>

        </Routes>
    );
};

export default AppRouter;