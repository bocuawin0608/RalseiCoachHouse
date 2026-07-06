import { Route } from 'react-router-dom';
import TripListPage from '../../../pages/tripStaff/TripListPage';
import TripDashboardPage from '../../../pages/tripStaff/TripDashboardPage';
import QrScanPage from '../../../pages/tripStaff/QrScanPage';

export const tripStaffRoutes = [
    <Route key="trip-list" path="trip/list" element={<TripListPage />} />,
    <Route key="trip-dashboard" path="trip/:tripId/dashboard" element={<TripDashboardPage />} />,
    <Route key="trip-scan" path="trip/:tripId/scan" element={<QrScanPage />} />,
    <Route key="trip-index" index element={<TripListPage />} />,
];
