import { Route } from 'react-router-dom';
import CoachTypePage from '../../../pages/manager/coaches/CoachTypePage';
import CoachTypeCreatePage from '../../../pages/manager/coaches/CoachTypeCreatePage';
import CoachTypeUpdateSeatmapPage from '../../../pages/manager/coaches/CoachTypeUpdateSeatmapPage';
import CoachPage from '../../../pages/manager/coaches/CoachPage';

export const coachRoutes = [
    <Route key="coach-types-group" path="coach-types">
        <Route index element={<CoachTypePage />} />
        <Route path='create' element={<CoachTypeCreatePage />} />
        <Route path=':id/seat-map' element={<CoachTypeUpdateSeatmapPage />} />
    </Route>,

    <Route key="coaches-group" path="coaches">
        <Route index element={<CoachPage />} />
    </Route>
];