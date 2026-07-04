import { Route } from "react-router-dom";
import HomePage from "../../../pages/public/home/HomePage";
import SearchTripPage from "../components/SearchTripPage";

export const publicTripRoutes = (
    <>
        <Route index element={<HomePage />} />
        <Route path="search-trip-demo/:tripId" element={<SearchTripPage/>} />
    </>
);