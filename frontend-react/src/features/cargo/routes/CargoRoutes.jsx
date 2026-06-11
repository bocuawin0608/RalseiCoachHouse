import { Route } from 'react-router-dom';
import CargoTypeManager from '../components/CargoTypeManager';
import CargoTypePriceManager from '../components/CargoTypePriceManager';

export const cargoRoutes = [
    <Route key="cargo-types" path="cargo-types" element={<CargoTypeManager />} />,
    <Route key="freight-rates" path="freight-rates" element={<CargoTypePriceManager />} />
];
