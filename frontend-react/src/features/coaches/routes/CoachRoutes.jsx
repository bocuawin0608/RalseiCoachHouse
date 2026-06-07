import { Route } from 'react-router-dom';
import CoachConfigPage from '../../../pages/manager/coaches/CoachConfigPage';
import CoachTypeCreatePage from '../../../pages/manager/coaches/CoachTypeCreatePage';
import CoachTypeUpdateSeatmapPage from '../../../pages/manager/coaches/CoachTypeUpdateSeatmapPage';

export const coachRoutes = [
    <Route key="coach-types-group" path="coach-types">
        <Route index element={<CoachConfigPage />} />
        <Route path='create' element={<CoachTypeCreatePage />} />
        <Route path=':id/seat-map' element={<CoachTypeUpdateSeatmapPage />} />
    </Route>
];