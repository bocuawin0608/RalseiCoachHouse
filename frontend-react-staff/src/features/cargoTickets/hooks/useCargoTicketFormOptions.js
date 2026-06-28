import { useCallback, useEffect, useRef, useState } from 'react';
import { cargoTicketApi } from '../api/cargoTicketApi';

export function useCargoTicketFormOptions(pickupStopId, dropoffStopId) {
    const [options, setOptions] = useState({ trips: [], customers: [], stops: [], sellers: [], handlers: [], drivers: [] });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const latestRequest = useRef(0);

    const loadOptions = useCallback(async () => {
        const requestNumber = ++latestRequest.current;
        setLoading(true);
        setError('');
        try {
            const hasStops = pickupStopId && dropoffStopId && pickupStopId !== dropoffStopId;
            const data = await cargoTicketApi.getFormOptions(hasStops ? { pickupStopId, dropoffStopId } : undefined);
            if (requestNumber !== latestRequest.current) return;
            setOptions({
                trips: data.trips ?? [],
                customers: data.customers ?? [],
                stops: data.stops ?? [],
                sellers: data.sellers ?? [],
                handlers: data.handlers ?? [],
                drivers: data.drivers ?? []
            });
        } catch (requestError) {
            if (requestNumber !== latestRequest.current) return;
            setError(requestError.response?.data?.message || 'Không thể tải dữ liệu danh mục.');
        } finally {
            if (requestNumber === latestRequest.current) setLoading(false);
        }
    }, [pickupStopId, dropoffStopId]);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadOptions();
    }, [loadOptions]);

    return { ...options, loading, error };
}
