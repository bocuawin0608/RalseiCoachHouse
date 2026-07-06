import { Route } from 'react-router-dom';
import CoachTypePage from '../../../pages/manager/coaches/CoachTypePage';
import CoachTypeCreatePage from '../../../pages/manager/coaches/CoachTypeCreatePage';
import CoachTypeDetailPage from '../../../pages/manager/coaches/CoachTypeDetailPage';
import CoachPage from '../../../pages/manager/coaches/CoachPage';
import CoachDetailPage from '../../../pages/manager/coaches/CoachDetailPage';

export const coachRoutes = [
    <Route key="coach-types-group" path="coach-types">
        <Route index element={<CoachTypePage />} />
        <Route path='create' element={<CoachTypeCreatePage />} />
        <Route path=':id' element={<CoachTypeDetailPage />} />
    </Route>,

    <Route key="coaches-group" path="coaches">
        <Route index element={<CoachPage />} />
        <Route path=':id' element={<CoachDetailPage />} />
    </Route>
];