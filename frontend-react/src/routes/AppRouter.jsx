import { Routes, Route, Navigate } from 'react-router-dom';
import HomePage from '../pages/HomePage';
import SeatLayoutPage from '../pages/seatlayout/SeatLayoutPage';
import SeatLayoutDetailPage from '../pages/seatlayout/SeatLayoutDetailPage';
import AdminDashboard from '../pages/admin/AdminDashboard';


const AppRouter = () => {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/home" replace />} />
            <Route path="/home" element={<HomePage />} />

            <Route path="/seat-layouts" element={<SeatLayoutPage />} />
            <Route path="/seat-layouts/:id" element={<SeatLayoutDetailPage />} />

            <Route path="/admin" element={<AdminDashboard />} />
        </Routes>
    );
};

export default AppRouter;