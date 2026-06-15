import { Route } from 'react-router-dom';
import RoutePage from '../../../pages/manager/routes/RoutePage';
import RouteCreatePage from '../../../pages/manager/routes/RouteCreatePage';
import RouteStopAddPage from '../../../pages/manager/routes/RouteStopAddPage';
import CoachStopPage from '../../../pages/manager/coachStops/CoachStopPage';
import CoachStopCreatePage from '../../../pages/manager/coachStops/CoachStopCreatePage';

export const routeRoutes = [
    <Route key="routes" path="routes" element={<RoutePage />} />,
    <Route key="routes-create" path="routes/create" element={<RouteCreatePage />} />,
    <Route key="routes-add-stops" path="routes/:routeId/add-route-stops" element={<RouteStopAddPage />} />,
    <Route key="coach-stops" path="coach-stops" element={<CoachStopPage />} />,
    <Route key="coach-stops-create" path="coach-stops/create" element={<CoachStopCreatePage />} />
];
