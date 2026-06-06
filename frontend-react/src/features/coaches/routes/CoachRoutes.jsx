import { Route } from 'react-router-dom';
import CoachConfigPage from '../../../pages/manager/coaches/CoachConfigPage';
import CoachTypeCreatePage from '../../../pages/manager/coaches/CoachTypeCreatePage';

export const coachRoutes = [
    <Route key="coach-types-group" path="coach-types">
        <Route index element={<CoachConfigPage />} />,
        <Route path='create' element={<CoachTypeCreatePage />} />

    </Route>
];