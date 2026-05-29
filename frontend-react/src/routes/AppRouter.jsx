import { Routes, Route, Navigate } from 'react-router-dom'; 
import HomePage from '../pages/HomePage';
import SeatLayoutPage from '../pages/SeatLayoutPage';

const AppRouter = () => {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/home" replace />} />
            
            <Route path="/home" element={<HomePage />} />
            <Route path="/seat-layout" element={<SeatLayoutPage />} />
        </Routes>
    );
};

export default AppRouter;