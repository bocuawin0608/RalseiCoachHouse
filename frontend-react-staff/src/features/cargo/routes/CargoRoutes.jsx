import { Route } from 'react-router-dom';
import CargoTypePage from '../../../pages/manager/cargo-types/CargoTypePage';
import CargoTypeCreatePage from '../../../pages/manager/cargo-types/CargoTypeCreatePage';

export const cargoRoutes = [
    <Route key="cargo-types" path="cargo-types" element={<CargoTypePage />} />,
    <Route key="cargo-types-create" path="cargo-types/create" element={<CargoTypeCreatePage />} />
];
