import { Route } from 'react-router-dom';
import CargoTypeManager from '../components/CargoTypeManager';

export const cargoRoutes = [
    <Route key="cargo-types" path="cargo-types" element={<CargoTypeManager />} />
];
