import { useCallback, useEffect, useRef, useState } from 'react';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';

export function usePassengerTicketSeatQr(ticketCode) {
    const [qrPreview, setQrPreview] = useState(null);
    const qrPreviewRef = useRef(null);

    const revokeAndClear = useCallback((preview) => {
        if (preview?.url) URL.revokeObjectURL(preview.url);
    }, []);

    useEffect(() => {
        qrPreviewRef.current = qrPreview;
    }, [qrPreview]);

    useEffect(() => {
        setQrPreview((prev) => {
            revokeAndClear(prev);
            return null;
        });
    }, [ticketCode, revokeAndClear]);

    useEffect(() => () => {
        revokeAndClear(qrPreviewRef.current);
    }, [revokeAndClear]);

    const closeQr = useCallback(() => {
        setQrPreview((prev) => {
            revokeAndClear(prev);
            return null;
        });
    }, [revokeAndClear]);

    const showQr = useCallback(async (seat) => {
        if (!ticketCode) return;

        if (qrPreviewRef.current?.ticketDetailId === seat.ticketDetailId) {
            closeQr();
            return;
        }

        setQrPreview((prev) => {
            revokeAndClear(prev);
            return {
                ticketDetailId: seat.ticketDetailId,
                seatCode: seat.seatCode,
                fullName: seat.fullName,
                url: null,
                loading: true,
                error: null,
            };
        });

        try {
            const blob = await staffPassengerTicketApi.getSeatQrBlob(
                ticketCode,
                seat.ticketDetailId
            );
            setQrPreview({
                ticketDetailId: seat.ticketDetailId,
                seatCode: seat.seatCode,
                fullName: seat.fullName,
                url: URL.createObjectURL(blob),
                loading: false,
                error: null,
            });
        } catch {
            setQrPreview({
                ticketDetailId: seat.ticketDetailId,
                seatCode: seat.seatCode,
                fullName: seat.fullName,
                url: null,
                loading: false,
                error: 'Không thể tải mã QR của ghế này.',
            });
        }
    }, [ticketCode, closeQr, revokeAndClear]);

    return { qrPreview, showQr, closeQr };
}
