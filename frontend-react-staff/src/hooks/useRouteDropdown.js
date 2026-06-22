import { useEffect, useState } from "react";
import { routeApi } from "../features/routes";

export function useRouteDropdown(enable = false) {
    const [routes, setRoutes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if(!enable) return;

        const fetchDropdownData = async () => {
            setLoading(true);
            setError(null);

            try {
                const data = await routeApi.getRoutesForDropdown();
                setRoutes(data || []);
            } catch (error) {
                setError(error.response?.data?.message || "Có lỗi xảy ra khi tải dữ liệu.");
            } finally {
                setLoading(false);
            }
        };

        fetchDropdownData();
    }, [enable]);

    return { routes, loading, error };
}