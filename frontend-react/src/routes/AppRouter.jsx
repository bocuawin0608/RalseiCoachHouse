import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom'; 
import HomePage from '../pages/HomePage';
import SelectSeatPage from '../pages/SelectSeatPage';
import SeatLayoutPage from '../pages/SeatLayoutPage';

const AppRouter = () => {
    return (
        <Routes>
            {/* Chuyển hướng bắt buộc từ root (/) sang /home */}
            <Route path="/" element={<Navigate to="/home" replace />} />
            <Route path="/home" element={<HomePage />} />
            <Route path="/seat-layout/:tripId" element={<SeatLayoutPage />} />
        </Routes>
    );
};

export default AppRouter;