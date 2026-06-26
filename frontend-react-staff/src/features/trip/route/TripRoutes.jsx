import { Route } from 'react-router-dom';
import TripPage from '../../../pages/manager/trip/TripPage';
import TripCreatePage from '../../../pages/manager/trip/TripCreatePage';

/** All manager-side trip routes, consumed by the root router */
export const tripRoutes = [
    <Route key="trips" path="trips" element={<TripPage />} />,
    <Route key="trips-create" path="trips/create" element={<TripCreatePage />} />
];
