import { Route } from 'react-router-dom';
import RouteManager from '../components/RouteManager';
import CoachStopManager from '../components/CoachStopManager';

export const routeRoutes = [
    <Route key="routes" path="routes" element={<RouteManager />} />,
    <Route key="coach-stops" path="coach-stops" element={<CoachStopManager />} />
];
