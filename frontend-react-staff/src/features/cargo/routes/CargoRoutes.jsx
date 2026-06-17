import { Route } from 'react-router-dom';
import CargoTypePage from '../../../pages/manager/cargo-types/CargoTypePage';
import CargoTypeCreatePage from '../../../pages/manager/cargo-types/CargoTypeCreatePage';
import CargoTypePricePage from '../../../pages/manager/cargo-type-prices/CargoTypePricePage';
import CargoTypePriceCreatePage from '../../../pages/manager/cargo-type-prices/CargoTypePriceCreatePage';

export const cargoRoutes = [
    <Route key="cargo-types" path="cargo-types" element={<CargoTypePage />} />,
    <Route key="cargo-types-create" path="cargo-types/create" element={<CargoTypeCreatePage />} />,
    <Route key="freight-rates" path="freight-rates" element={<CargoTypePricePage />} />,
    <Route key="freight-rates-create" path="freight-rates/create" element={<CargoTypePriceCreatePage />} />
];
