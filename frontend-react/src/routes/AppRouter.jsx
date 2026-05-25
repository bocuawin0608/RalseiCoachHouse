import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import SeatLayoutPage from '../pages/SeatLayoutPage';

export default function AppRouter() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />

            <Route path="/login" element={<LoginPage />} />
            <Route path="/seat-layouts" element={<SeatLayoutPage />} />
            <Route path="*" element={<h2>404 - Link ko khả dụng</h2>} />
        </Routes>
    );
}