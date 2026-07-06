import { Route } from 'react-router-dom';
import CargoLookupPage from '../components/CargoLookupPage';

/** Keeps authenticated cargo history routing within its owning feature. */
export const cargoLookupRoutes = <Route path="/cargo-history" element={<CargoLookupPage />} />;
