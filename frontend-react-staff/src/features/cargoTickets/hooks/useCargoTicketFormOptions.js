import { useCallback, useEffect, useRef, useState } from 'react';
import { cargoTicketApi } from '../api/cargoTicketApi';

export function useCargoTicketFormOptions(pickupStopId, dropoffStopId) {
    const [options, setOptions] = useState({ trips: [], customers: [], stops: [], sellers: [], handlers: [], drivers: [] });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const latestTripRequest = useRef(0);
    const staticLoaded = useRef(false);

    const loadOptions = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            if (!staticLoaded.current) {
                const data = await cargoTicketApi.getFormOptions();
                setOptions(prev => ({
                    ...prev,
                    customers: data.customers ?? [],
                    stops: data.stops ?? [],
                    sellers: data.sellers ?? [],
                    handlers: data.handlers ?? [],
                    drivers: data.drivers ?? []
                }));
                staticLoaded.current = true;
            }

            const hasStops = pickupStopId && dropoffStopId && String(pickupStopId) !== String(dropoffStopId);
            if (hasStops) {
                const requestNumber = ++latestTripRequest.current;
                const tripsData = await cargoTicketApi.getTripsByStops({ pickupStopId, dropoffStopId });
                if (requestNumber === latestTripRequest.current) {
                    setOptions(prev => ({ ...prev, trips: tripsData }));
                }
            } else {
                setOptions(prev => ({ ...prev, trips: [] }));
            }
        } catch (requestError) {
            setError(requestError.response?.data?.message || 'Không thể tải dữ liệu danh mục.');
        } finally {
            setLoading(false);
        }
    }, [pickupStopId, dropoffStopId]);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadOptions();
    }, [loadOptions]);

    return { ...options, loading, error };
}
