import { useCallback, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Scanner } from '@yudiel/react-qr-scanner';
import CheckInResultModal from '../../features/tripStaff/components/CheckInResultModal';
import { tripStaffApi } from '../../features/tripStaff/api/tripStaffApi';
import '../../features/tripStaff/components/TripStaff.css';

export default function QrScanPage() {
    const { tripId } = useParams();
    const navigate = useNavigate();
    const [modal, setModal] = useState({ show: false, variant: 'success', message: '', result: null });
    const scanningRef = useRef(true);

    const handleScan = useCallback(async (detectedCodes) => {
        if (!scanningRef.current || !detectedCodes?.length) return;

        const rawValue = detectedCodes[0]?.rawValue?.trim();
        if (!rawValue) return;

        scanningRef.current = false;

        try {
            const result = await tripStaffApi.checkInByQr(tripId, rawValue);
            setModal({ show: true, variant: 'success', message: '', result });
        } catch (err) {
            setModal({
                show: true,
                variant: 'error',
                message: err.response?.data?.message || 'Mã QR không hợp lệ',
                result: null,
            });
        }
    }, [tripId]);

    const handleCloseSuccess = () => {
        setModal((m) => ({ ...m, show: false }));
        scanningRef.current = true;
    };

    const handleRescan = () => {
        setModal((m) => ({ ...m, show: false }));
        scanningRef.current = true;
    };

    return (
        <>
            <div className="qr-scan-page">
                <div className="qr-scan-viewport">
                    <Scanner
                        onScan={handleScan}
                        onError={(error) => console.error('Camera error:', error)}
                        constraints={{ facingMode: 'environment' }}
                        styles={{ container: { width: '100%', height: '100%' } }}
                    />
                </div>
            </div>

            <CheckInResultModal
                show={modal.show}
                variant={modal.variant}
                message={modal.message}
                result={modal.result}
                autoCloseMs={modal.variant === 'success' ? 10000 : null}
                onClose={handleCloseSuccess}
                onRescan={handleRescan}
            />

            {!modal.show && (
                <button
                    type="button"
                    className="btn btn-light btn-sm"
                    style={{ position: 'fixed', top: 70, left: 16, zIndex: 60 }}
                    onClick={() => navigate(`/staff/trip/${tripId}/dashboard`)}
                >
                    ← Dashboard
                </button>
            )}
        </>
    );
}
