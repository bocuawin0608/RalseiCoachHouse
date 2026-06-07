import { Route } from "react-router-dom";
import HomePage from "../../../pages/public/home/HomePage";
import SelectSeatPage from "../components/SelectSeatPage";

export const publicTripRoutes = (
    <>
        <Route index element={<HomePage />} />
        <Route path="select-seat/:tripId" element={<SelectSeatPage />} />
    </>
);