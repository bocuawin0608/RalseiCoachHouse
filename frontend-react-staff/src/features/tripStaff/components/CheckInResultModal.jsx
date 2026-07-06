import { useEffect } from 'react';
import { Button } from 'react-bootstrap';
import './TripStaff.css';

export default function CheckInResultModal({
    show,
    variant,
    message,
    result,
    autoCloseMs,
    onClose,
    onRescan,
}) {
    useEffect(() => {
        if (!show || variant !== 'success' || !autoCloseMs) return undefined;

        const timer = setTimeout(() => {
            onClose();
        }, autoCloseMs);

        return () => clearTimeout(timer);
    }, [show, variant, autoCloseMs, onClose]);

    useEffect(() => {
        if (show && variant === 'success') {
            try {
                const ctx = new (window.AudioContext || window.webkitAudioContext)();
                const oscillator = ctx.createOscillator();
                const gain = ctx.createGain();
                oscillator.connect(gain);
                gain.connect(ctx.destination);
                oscillator.frequency.value = 880;
                gain.gain.value = 0.08;
                oscillator.start();
                oscillator.stop(ctx.currentTime + 0.15);
            } catch {
                // ignore audio errors on unsupported browsers
            }
        }
    }, [show, variant]);

    if (!show) return null;

    return (
        <div className="checkin-result-overlay">
            <div className={`checkin-result-modal ${variant}`}>
                {variant === 'success' && result ? (
                    <>
                        <h5 className="text-success fw-bold mb-1">Check-in thành công</h5>
                        <p className="mb-0 fw-semibold">{result.fullName}</p>
                        <div className="checkin-result-seat">Ghế {result.seatCode}</div>
                        <p className="mb-1" style={{ fontSize: '14px' }}>
                            Đón: {result.pickupStopName}
                        </p>
                        <p className="mb-3" style={{ fontSize: '14px' }}>
                            Trả: {result.dropoffStopName}
                        </p>
                        {result.accompaniedChild && (
                            <p className="mb-3 text-muted" style={{ fontSize: '13px' }}>
                                Trẻ em: {result.accompaniedChild.fullname} (sinh {result.accompaniedChild.birthYear})
                            </p>
                        )}
                        <Button variant="success" onClick={onClose}>
                            {autoCloseMs ? 'Quét tiếp' : 'Đã hướng dẫn'}
                        </Button>
                    </>
                ) : (
                    <>
                        <h5 className="text-danger fw-bold mb-2">Không hợp lệ</h5>
                        <p className="mb-4">{message}</p>
                        <Button variant="danger" onClick={onRescan || onClose}>
                            Quét lại
                        </Button>
                    </>
                )}
            </div>
        </div>
    );
}
