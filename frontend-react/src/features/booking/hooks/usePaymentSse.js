import { useEffect, useRef } from 'react';
import axiosClient from '../../../api/axiosClient';

export const usePaymentSse = (transactionId, { onStatusChange, enabled = true }) => {
    const onStatusChangeRef = useRef(onStatusChange);

    useEffect(() => {
        onStatusChangeRef.current = onStatusChange;
    }, [onStatusChange]);

    useEffect(() => {
        if (!transactionId || !enabled) return undefined;

        const baseUrl = axiosClient.defaults.baseURL?.replace(/\/$/, '');
        const streamUrl = `${baseUrl}/v1/bookings/payments/${encodeURIComponent(transactionId)}/stream`;
        const eventSource = new EventSource(streamUrl, { withCredentials: true });

        const handlePaymentStatus = (event) => {
            const nextStatus = event.data?.trim();
            if (nextStatus) {
                onStatusChangeRef.current(nextStatus);
            }
        };

        eventSource.addEventListener('PAYMENT_STATUS', handlePaymentStatus);

        eventSource.onerror = () => {
            // EventSource tự reconnect khi connection bị ngắt tạm thời
        };

        return () => {
            eventSource.removeEventListener('PAYMENT_STATUS', handlePaymentStatus);
            eventSource.close();
        };
    }, [transactionId, enabled]);
};
