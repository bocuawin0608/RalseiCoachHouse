import { Alert, Button, Modal, Spinner } from 'react-bootstrap';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';
import TripSeatMapGrid, { buildSeatLayout } from './TripSeatMapGrid';
import SeatIcon from '../../../components/common/SeatIcon';

function createHoldToken() {
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
        return crypto.randomUUID().replace(/-/g, '');
    }
    return `${Date.now()}${Math.random().toString(36).slice(2)}`;
}

export default function ChangeSeatModal({
    isOpen,
    ticket,
    seat,
    onClose,
    onSuccess,
}) {
    const holdTokenRef = useRef('');
    const [seatList, setSeatList] = useState([]);
    const [loading, setLoading] = useState(false);
    const [locking, setLocking] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [selectedTripSeatId, setSelectedTripSeatId] = useState(null);
    const [selectedSeatCode, setSelectedSeatCode] = useState(null);
    const lockedTripSeatIdRef = useRef(null);

    const layout = useMemo(() => buildSeatLayout(seatList), [seatList]);

    const releaseLockedSeat = useCallback(async () => {
        const lockedId = lockedTripSeatIdRef.current;
        const holdToken = holdTokenRef.current;
        if (!lockedId || !holdToken || !ticket?.tripId) return;

        try {
            await staffPassengerTicketApi.releaseSeats(ticket.tripId, [lockedId], holdToken);
        } catch {
            // Best-effort release when closing modal.
        } finally {
            lockedTripSeatIdRef.current = null;
        }
    }, [ticket?.tripId]);

    const loadSeatMap = useCallback(async () => {
        if (!ticket?.tripId) return;

        setLoading(true);
        setError(null);

        try {
            const response = await staffPassengerTicketApi.getTripSeatMap(ticket.tripId);
            setSeatList(response || []);
        } catch (requestError) {
            setSeatList([]);
            setError(requestError.response?.data?.message || 'Không thể tải sơ đồ ghế.');
        } finally {
            setLoading(false);
        }
    }, [ticket?.tripId]);

    useEffect(() => {
        if (!isOpen || !seat || !ticket) return;

        holdTokenRef.current = createHoldToken();
        setSelectedTripSeatId(null);
        setSelectedSeatCode(null);
        lockedTripSeatIdRef.current = null;
        setError(null);
        loadSeatMap();

        return () => {
            releaseLockedSeat();
        };
    }, [isOpen, seat?.ticketDetailId, ticket?.ticketCode, loadSeatMap, releaseLockedSeat]);

    const handleSeatClick = async (clickedSeat) => {
        if (!ticket?.tripId || clickedSeat.tripSeatId === seat.tripSeatId || locking) return;

        const isAlreadySelected =
            lockedTripSeatIdRef.current === clickedSeat.tripSeatId
            || selectedTripSeatId === clickedSeat.tripSeatId;

        setLocking(true);
        setError(null);

        try {
            if (isAlreadySelected) {
                await staffPassengerTicketApi.releaseSeats(
                    ticket.tripId,
                    [clickedSeat.tripSeatId],
                    holdTokenRef.current
                );
                lockedTripSeatIdRef.current = null;
                setSelectedTripSeatId(null);
                setSelectedSeatCode(null);
                return;
            }

            if (lockedTripSeatIdRef.current) {
                await staffPassengerTicketApi.releaseSeats(
                    ticket.tripId,
                    [lockedTripSeatIdRef.current],
                    holdTokenRef.current
                );
                lockedTripSeatIdRef.current = null;
            }

            await staffPassengerTicketApi.lockSeat(
                ticket.tripId,
                clickedSeat.tripSeatId,
                holdTokenRef.current
            );

            lockedTripSeatIdRef.current = clickedSeat.tripSeatId;
            setSelectedTripSeatId(clickedSeat.tripSeatId);
            setSelectedSeatCode(clickedSeat.seatCode);
        } catch (requestError) {
            if (lockedTripSeatIdRef.current) {
                const lockedSeat = seatList.find((s) => s.tripSeatId === lockedTripSeatIdRef.current);
                setSelectedTripSeatId(lockedTripSeatIdRef.current);
                setSelectedSeatCode(lockedSeat?.seatCode ?? null);
            } else {
                setSelectedTripSeatId(null);
                setSelectedSeatCode(null);
            }
            setError(requestError.response?.data?.message || 'Không thể giữ ghế. Vui lòng thử lại.');
        } finally {
            setLocking(false);
        }
    };

    const handleClose = async () => {
        await releaseLockedSeat();
        setSelectedTripSeatId(null);
        setSelectedSeatCode(null);
        setError(null);
        onClose();
    };

    const handleSubmit = async () => {
        if (!selectedTripSeatId || !ticket || !seat) return;

        setSubmitting(true);
        setError(null);

        try {
            const updatedTicket = await staffPassengerTicketApi.changeSeat(
                ticket.ticketCode,
                seat.ticketDetailId,
                selectedTripSeatId,
                holdTokenRef.current
            );
            lockedTripSeatIdRef.current = null;
            onSuccess?.(updatedTicket);
            onClose();
        } catch (requestError) {
            setError(requestError.response?.data?.message || 'Không thể đổi ghế.');
        } finally {
            setSubmitting(false);
        }
    };

    if (!isOpen || !seat || !ticket) return null;

    return (
        <Modal show={isOpen} onHide={handleClose} size="lg" centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Đổi ghế — {seat.fullName} ({seat.seatCode})
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4">
                <Alert variant="info" className="py-2 px-3 border-0 small">
                    Mã QR lên xe không đổi. Hệ thống chỉ cập nhật ghế trên vé.
                </Alert>

                <div className="d-flex flex-wrap gap-3 mb-3 small text-muted justify-content-center">
                    <span className="d-inline-flex align-items-center gap-2">
                        <SeatIcon status="CURRENT" code="" /> Ghế hiện tại
                    </span>
                    <span className="d-inline-flex align-items-center gap-2">
                        <SeatIcon status="SELECTED" code="" /> Ghế mới
                    </span>
                    <span className="d-inline-flex align-items-center gap-2">
                        <SeatIcon status="AVAILABLE" code="" /> Còn trống
                    </span>
                    <span className="d-inline-flex align-items-center gap-2">
                        <SeatIcon status="SOLD" code="" /> Đã đặt
                    </span>
                </div>

                {loading && (
                    <div className="py-4 text-center">
                        <Spinner animation="border" size="sm" className="me-2" />
                        Đang tải sơ đồ ghế...
                    </div>
                )}

                {!loading && (
                    <TripSeatMapGrid
                        layout={layout}
                        currentTripSeatId={seat.tripSeatId}
                        selectedTripSeatId={selectedTripSeatId}
                        onSeatClick={handleSeatClick}
                    />
                )}

                {selectedSeatCode && (
                    <div className="mt-3 p-3 bg-light border rounded small">
                        Đổi từ <strong>{seat.seatCode}</strong> → <strong>{selectedSeatCode}</strong>
                    </div>
                )}

                {error && (
                    <Alert variant="danger" className="mt-3 mb-0 py-2 px-3 border-0 d-flex align-items-center gap-2">
                        <BsExclamationTriangleFill />
                        <span>{error}</span>
                    </Alert>
                )}
            </Modal.Body>

            <Modal.Footer className="bg-light border-0 rounded-bottom">
                <Button variant="outline-secondary" onClick={handleClose} disabled={submitting || locking}>
                    Hủy bỏ
                </Button>
                <Button
                    className="custom-btn-general px-4"
                    onClick={handleSubmit}
                    disabled={!selectedTripSeatId || submitting || locking}
                >
                    {submitting ? 'Đang lưu...' : 'Xác nhận đổi ghế'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
