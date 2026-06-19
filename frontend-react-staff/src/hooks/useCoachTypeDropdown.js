import { useEffect, useState } from "react";
import { coachTypeApi } from "../features/coaches";

export function useCoachTypeDropdown(enable = false) {
    const [coachTypes, setCoachTypes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if(!enable) return;

        const fetchDropdownData = async () => {
            setLoading(true);
            setError(null);

            try {
                const data = await coachTypeApi.getCoachTypesDropdown();
                setCoachTypes(data || []);
            } catch (error) {
                setError(error.response?.data?.message || "Có lỗi xảy ra khi tải dữ liệu.");
            } finally {
                setLoading(false);
            }
        };

        fetchDropdownData();
    }, [enable]);

    return { coachTypes, loading, error };
}