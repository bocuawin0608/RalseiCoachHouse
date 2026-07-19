import { Alert, Button, Form, Modal, Spinner } from 'react-bootstrap';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
    BsArrowLeft,
    BsArrowLeftRight,
    BsExclamationTriangleFill,
    BsPencilSquare,
} from 'react-icons/bs';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';
import { useItineraryChangeWorkflow } from '../hooks/useItineraryChangeWorkflow';
import { useSeatHoldSession } from '../hooks/useSeatHoldSession';
import {
    buildPassengerInitialForm,
    buildPassengerPayload,
    passengerFormHasChanges,
    validatePassengerForm,
} from '../utils/passengerChangeForm';
import { formatDateTime } from '../utils/passengerTicketFormatters';
import ItineraryChangePreview from './itinerary-change/ItineraryChangePreview';
import ItineraryStopSelection from './itinerary-change/ItineraryStopSelection';
import TransferSeatSelection from './itinerary-change/TransferSeatSelection';
import TransferTripSelection from './itinerary-change/TransferTripSelection';
import PassengerInfoFields from './PassengerInfoFields';
import CustomerPhoneOtpModal from './CustomerPhoneOtpModal';
import TripSeatMapGrid, { buildSeatLayout } from './TripSeatMapGrid';
import SeatIcon from '../../../components/common/SeatIcon';
import { resolveTicketContactPhone } from '../utils/ticketContactPhone';

/** Choose path → amend current trip (info/seat/stops) OR transfer (major). */
const VIEW_CHOOSE = 'choose';
const VIEW_AMEND = 'amend';
const VIEW_TRANSFER = 'transfer';

function getErrorMessage(error, fallback) {
    return error?.response?.data?.message || fallback;
}

export default function ChangeTicketSessionModal({
    isOpen,
    ticket,
    onClose,
    onSuccess,
}) {
    const canChangePassengerInfo = ticket?.allowedActions?.includes('CHANGE_PASSENGER_INFO');
    const canChangeSeat = ticket?.allowedActions?.includes('CHANGE_SEAT');
    const canChangeItinerary = ticket?.allowedActions?.includes('CHANGE_ITINERARY');
    const canTransferTrip = ticket?.allowedActions?.includes('TRANSFER_TRIP')
        && !ticket?.majorChangeType;
    const canAmend = canChangePassengerInfo || canChangeSeat || canChangeItinerary;

    const confirmedSeats = useMemo(
        () => (ticket?.seats || []).filter((seat) => seat.status === 'CONFIRMED'),
        [ticket]
    );

    const [view, setView] = useState(VIEW_CHOOSE);
    const [passengerForms, setPassengerForms] = useState({});
    const [passengerErrors, setPassengerErrors] = useState({});
    const [seatDrafts, setSeatDrafts] = useState({});
    const [activeSeatDetailId, setActiveSeatDetailId] = useState(null);
    const [seatList, setSeatList] = useState([]);
    const [loadingSeats, setLoadingSeats] = useState(false);
    const [lockingSeat, setLockingSeat] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [sessionKey, setSessionKey] = useState(0);
    const [otpOpen, setOtpOpen] = useState(false);
    const pendingConfirmRef = useRef(null);
    const seatDraftsRef = useRef({});

    const contactPhone = useMemo(() => resolveTicketContactPhone(ticket), [ticket]);

    const seatHold = useSeatHoldSession();
    const seatLayout = useMemo(() => buildSeatLayout(seatList), [seatList]);

    useEffect(() => {
        seatDraftsRef.current = seatDrafts;
    }, [seatDrafts]);

    const stopsWorkflow = useItineraryChangeWorkflow({
        isOpen: isOpen && canChangeItinerary && view === VIEW_AMEND,
        mode: 'same-trip',
        ticket,
    });

    const transferWorkflow = useItineraryChangeWorkflow({
        isOpen: isOpen && canTransferTrip && view === VIEW_TRANSFER,
        mode: 'transfer',
        ticket,
    });

    const resetAmendDrafts = useCallback(() => {
        const forms = {};
        confirmedSeats.forEach((seat) => {
            forms[seat.ticketDetailId] = buildPassengerInitialForm(seat);
        });
        setPassengerForms(forms);
        setPassengerErrors({});
        setSeatDrafts({});
        setActiveSeatDetailId(confirmedSeats[0]?.ticketDetailId || null);
        setSeatList([]);
    }, [confirmedSeats]);

    useEffect(() => {
        if (!isOpen || !ticket) return undefined;

        if (canAmend && !canTransferTrip) {
            setView(VIEW_AMEND);
        } else if (!canAmend && canTransferTrip) {
            setView(VIEW_TRANSFER);
        } else {
            setView(VIEW_CHOOSE);
        }

        resetAmendDrafts();
        setError(null);
        setSubmitting(false);
        seatHold.beginSession();
        setSessionKey((value) => value + 1);
        return undefined;
        // eslint-disable-next-line react-hooks/exhaustive-deps -- open/close lifecycle only
    }, [isOpen, ticket?.ticketCode]);

    useEffect(() => {
        if (!isOpen) return undefined;
        const tripId = ticket?.tripId;
        return () => {
            const seatIds = Object.values(seatDraftsRef.current).map((draft) => draft.newTripSeatId);
            const token = seatHold.holdTokenRef.current;
            if (tripId && seatIds.length > 0 && token) {
                void staffPassengerTicketApi.releaseSeats(tripId, seatIds, token);
            }
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps -- cleanup on unmount/close only
    }, [isOpen, ticket?.tripId]);

    const loadSeatMap = useCallback(async () => {
        if (!ticket?.tripId) return;
        setLoadingSeats(true);
        setError(null);
        try {
            const response = await staffPassengerTicketApi.getTripSeatMap(ticket.tripId);
            setSeatList(response || []);
        } catch (requestError) {
            setSeatList([]);
            setError(getErrorMessage(requestError, 'Không thể tải sơ đồ ghế.'));
        } finally {
            setLoadingSeats(false);
        }
    }, [ticket?.tripId]);

    useEffect(() => {
        if (isOpen && view === VIEW_AMEND && canChangeSeat) {
            void loadSeatMap();
        }
    }, [canChangeSeat, isOpen, loadSeatMap, sessionKey, view]);

    const stopsDraft = canChangeItinerary && view === VIEW_AMEND
        ? stopsWorkflow.getDraft?.()
        : null;
    const transferDraft = canTransferTrip && view === VIEW_TRANSFER
        ? transferWorkflow.getDraft?.()
        : null;

    /** Confirm only when there is a complete, eligible change (price OK / seats enough). */
    const canConfirm = useMemo(() => {
        if (view === VIEW_AMEND) {
            const hasValidPassengerChange = confirmedSeats.some((seat) => {
                const form = passengerForms[seat.ticketDetailId];
                return Boolean(
                    form
                    && passengerFormHasChanges(form, seat)
                    && Object.keys(validatePassengerForm(form)).length === 0
                );
            });
            const hasSeatChange = Object.keys(seatDrafts).length > 0;
            const hasStopChange = Boolean(stopsWorkflow.canSubmit);
            return hasValidPassengerChange || hasSeatChange || hasStopChange;
        }
        if (view === VIEW_TRANSFER) {
            return Boolean(transferWorkflow.canSubmit);
        }
        return false;
    }, [
        confirmedSeats,
        passengerForms,
        seatDrafts,
        stopsWorkflow.canSubmit,
        transferWorkflow.canSubmit,
        view,
    ]);

    const activeDraft = activeSeatDetailId ? seatDrafts[activeSeatDetailId] : null;

    /** Original seats vacated by any draft in this session (selectable again). */
    const sessionAvailableTripSeatIds = useMemo(
        () => confirmedSeats
            .filter((seat) => seatDrafts[seat.ticketDetailId]?.newTripSeatId)
            .map((seat) => seat.tripSeatId)
            .filter(Boolean),
        [confirmedSeats, seatDrafts]
    );

    /** Seats held by other passengers (draft target, or current if they have not moved). */
    const blockedTripSeatIds = useMemo(() => {
        const blocked = [];
        confirmedSeats.forEach((seat) => {
            if (seat.ticketDetailId === activeSeatDetailId) return;
            const draft = seatDrafts[seat.ticketDetailId];
            if (draft?.newTripSeatId) {
                blocked.push(draft.newTripSeatId);
            } else if (seat.tripSeatId) {
                blocked.push(seat.tripSeatId);
            }
        });
        return blocked;
    }, [activeSeatDetailId, confirmedSeats, seatDrafts]);

    const handlePassengerFieldChange = (ticketDetailId, field, value) => {
        setPassengerForms((prev) => ({
            ...prev,
            [ticketDetailId]: { ...prev[ticketDetailId], [field]: value },
        }));
        setPassengerErrors((prev) => ({
            ...prev,
            [ticketDetailId]: { ...prev[ticketDetailId], [field]: undefined },
        }));
        setError(null);
    };

    const clearActiveSeatDraft = async (currentSeat, existingDraft) => {
        await staffPassengerTicketApi.releaseSeats(
            ticket.tripId,
            [existingDraft.newTripSeatId],
            seatHold.holdTokenRef.current,
            currentSeat.tripSeatId ? [currentSeat.tripSeatId] : null
        );
        setSeatDrafts((prev) => {
            const next = { ...prev };
            delete next[activeSeatDetailId];
            return next;
        });
    };

    const handleSameTripSeatClick = async (clickedSeat) => {
        if (!ticket?.tripId || !activeSeatDetailId || lockingSeat || submitting) return;

        const currentSeat = confirmedSeats.find((seat) => seat.ticketDetailId === activeSeatDetailId);
        if (!currentSeat) return;

        const existingDraft = seatDrafts[activeSeatDetailId];

        // Click vacated original seat → cancel draft (do not keep old selection).
        if (existingDraft && clickedSeat.tripSeatId === currentSeat.tripSeatId) {
            setLockingSeat(true);
            setError(null);
            try {
                await clearActiveSeatDraft(currentSeat, existingDraft);
            } catch (requestError) {
                setError(getErrorMessage(requestError, 'Không thể bỏ chọn ghế. Vui lòng thử lại.'));
            } finally {
                setLockingSeat(false);
            }
            return;
        }

        if (clickedSeat.tripSeatId === currentSeat.tripSeatId) return;

        const isDeselect = existingDraft?.newTripSeatId === clickedSeat.tripSeatId;
        if (!isDeselect && blockedTripSeatIds.includes(clickedSeat.tripSeatId)) return;

        setLockingSeat(true);
        setError(null);
        try {
            if (isDeselect) {
                await clearActiveSeatDraft(currentSeat, existingDraft);
                return;
            }

            if (existingDraft?.newTripSeatId) {
                await staffPassengerTicketApi.releaseSeats(
                    ticket.tripId,
                    [existingDraft.newTripSeatId],
                    seatHold.holdTokenRef.current
                );
            }

            await staffPassengerTicketApi.lockSeat(
                ticket.tripId,
                clickedSeat.tripSeatId,
                seatHold.holdTokenRef.current,
                currentSeat.tripSeatId
            );

            setSeatDrafts((prev) => ({
                ...prev,
                [activeSeatDetailId]: {
                    newTripSeatId: clickedSeat.tripSeatId,
                    seatCode: clickedSeat.seatCode,
                },
            }));
        } catch (requestError) {
            setError(getErrorMessage(requestError, 'Không thể giữ ghế. Vui lòng thử lại.'));
        } finally {
            setLockingSeat(false);
        }
    };

    const buildPassengerUpdates = () => {
        const updates = [];
        const errorsBySeat = {};

        for (const seat of confirmedSeats) {
            const form = passengerForms[seat.ticketDetailId];
            if (!form || !passengerFormHasChanges(form, seat)) continue;

            const fieldErrors = validatePassengerForm(form);
            if (Object.keys(fieldErrors).length > 0) {
                errorsBySeat[seat.ticketDetailId] = fieldErrors;
                continue;
            }

            updates.push({
                ticketDetailId: seat.ticketDetailId,
                ...buildPassengerPayload(form, seat),
            });
        }

        return { updates, errorsBySeat };
    };

    const changeSummary = useMemo(() => {
        if (!canConfirm) return [];

        const lines = [];
        if (view === VIEW_AMEND) {
            confirmedSeats.forEach((seat) => {
                const form = passengerForms[seat.ticketDetailId];
                if (
                    form
                    && passengerFormHasChanges(form, seat)
                    && Object.keys(validatePassengerForm(form)).length === 0
                ) {
                    lines.push(`Ghế ${seat.seatCode}: cập nhật thông tin hành khách`);
                }
                const seatDraft = seatDrafts[seat.ticketDetailId];
                if (seatDraft) {
                    lines.push(`Ghế ${seat.seatCode} → ${seatDraft.seatCode}`);
                }
            });
            if (stopsWorkflow.canSubmit) {
                lines.push('Đổi điểm đón/trả trên chuyến hiện tại');
            }
        }
        if (view === VIEW_TRANSFER && transferWorkflow.canSubmit) {
            lines.push('Đổi chuyến');
        }
        return lines;
    }, [
        canConfirm,
        confirmedSeats,
        passengerForms,
        seatDrafts,
        stopsWorkflow.canSubmit,
        transferWorkflow.canSubmit,
        view,
    ]);

    const releaseSeatDraftHolds = useCallback(async () => {
        const seatIds = Object.values(seatDrafts).map((draft) => draft.newTripSeatId);
        if (!ticket?.tripId || seatIds.length === 0 || !seatHold.holdTokenRef.current) return;
        try {
            await staffPassengerTicketApi.releaseSeats(
                ticket.tripId,
                seatIds,
                seatHold.holdTokenRef.current
            );
        } catch {
            // Best-effort release when leaving the session.
        }
    }, [seatDrafts, seatHold.holdTokenRef, ticket?.tripId]);

    const handleClose = async () => {
        if (submitting) return;
        await releaseSeatDraftHolds();
        seatHold.forgetSession();
        stopsWorkflow.forgetSession?.();
        transferWorkflow.forgetSession?.();
        onClose?.();
    };

    const goToChoose = async () => {
        if (submitting) return;
        await releaseSeatDraftHolds();
        setSeatDrafts({});
        seatHold.beginSession();
        transferWorkflow.forgetSession?.();
        resetAmendDrafts();
        setError(null);
        setView(VIEW_CHOOSE);
        setSessionKey((value) => value + 1);
    };

    const submitConfirmWithOtp = async (firebaseIdToken) => {
        const pending = pendingConfirmRef.current;
        if (!ticket || !pending) return;

        setSubmitting(true);
        setError(null);
        try {
            const updatedTicket = await staffPassengerTicketApi.confirmChanges(
                ticket.ticketCode,
                {
                    firebaseIdToken,
                    ...pending.payload,
                },
                pending.holdToken
            );
            seatHold.forgetSession();
            stopsWorkflow.forgetSession?.();
            transferWorkflow.forgetSession?.();
            pendingConfirmRef.current = null;
            setOtpOpen(false);
            onSuccess?.(updatedTicket);
            onClose?.();
        } catch (requestError) {
            setError(getErrorMessage(requestError, 'Không thể lưu thay đổi vé.'));
            setOtpOpen(false);
        } finally {
            setSubmitting(false);
        }
    };

    const handleConfirm = () => {
        if (!ticket || submitting || !canConfirm) return;

        if (!contactPhone) {
            setError('Vé không có số điện thoại hành khách để xác thực OTP.');
            return;
        }

        if (view === VIEW_AMEND) {
            const { updates, errorsBySeat } = buildPassengerUpdates();
            if (Object.keys(errorsBySeat).length > 0) {
                setPassengerErrors(errorsBySeat);
                return;
            }

            const seatChanges = Object.entries(seatDrafts).map(([ticketDetailId, draft]) => ({
                ticketDetailId: Number(ticketDetailId),
                newTripSeatId: draft.newTripSeatId,
            }));
            const itineraryChange = stopsDraft?.ready ? stopsDraft.payload : undefined;
            if (updates.length === 0 && seatChanges.length === 0 && !itineraryChange) return;

            pendingConfirmRef.current = {
                payload: {
                    passengerUpdates: updates.length ? updates : undefined,
                    seatChanges: seatChanges.length ? seatChanges : undefined,
                    itineraryChange,
                },
                holdToken: seatChanges.length ? seatHold.holdTokenRef.current : undefined,
            };
            setError(null);
            setOtpOpen(true);
            return;
        }

        if (view === VIEW_TRANSFER) {
            if (!transferDraft?.ready || !transferDraft?.payload) return;
            pendingConfirmRef.current = {
                payload: { itineraryChange: transferDraft.payload },
                holdToken: transferDraft.holdToken,
            };
            setError(null);
            setOtpOpen(true);
        }
    };

    if (!isOpen || !ticket) return null;

    if (!canAmend && !canTransferTrip) {
        return (
            <Modal show={isOpen} onHide={onClose} centered>
                <Modal.Header closeButton>
                    <Modal.Title className="fs-5 fw-bold">Đổi vé</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Alert variant="warning" className="mb-0">
                        Vé này hiện không còn thao tác đổi nào được phép.
                    </Alert>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="outline-secondary" onClick={onClose}>Đóng</Button>
                </Modal.Footer>
            </Modal>
        );
    }

    const activeSeat = confirmedSeats.find((seat) => seat.ticketDetailId === activeSeatDetailId);
    const operationBusy = submitting
        || lockingSeat
        || seatHold.busy
        || stopsWorkflow.locking
        || transferWorkflow.locking;
    const showBackToChoose = canAmend && canTransferTrip && view !== VIEW_CHOOSE;

    return (
        <>
        <Modal
            show={isOpen}
            onHide={handleClose}
            size="lg"
            centered
            backdrop="static"
            keyboard={!operationBusy}
        >
            <Modal.Header closeButton={!operationBusy}>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Đổi vé — {ticket.ticketCode}
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4">
                <div className="mb-3 p-3 bg-light border rounded small">
                    <div className="fw-semibold mb-1">Chuyến hiện tại</div>
                    <div>{ticket.routeName} • {formatDateTime(ticket.departureTime)}</div>
                    <div className="text-muted">
                        {ticket.pickupStopName} → {ticket.dropoffStopName}
                    </div>
                </div>

                {showBackToChoose && (
                    <Button
                        variant="link"
                        className="text-decoration-none px-0 mb-3 d-inline-flex align-items-center gap-2"
                        onClick={goToChoose}
                        disabled={operationBusy}
                    >
                        <BsArrowLeft /> Quay lại chọn loại thao tác
                    </Button>
                )}

                {view === VIEW_CHOOSE && (
                    <div className="row g-3">
                        {canAmend && (
                            <div className={canTransferTrip ? 'col-md-6' : 'col-12'}>
                                <button
                                    type="button"
                                    className="w-100 h-100 border-0 rounded-3 p-4 text-start"
                                    style={{
                                        cursor: 'pointer',
                                        background: 'linear-gradient(145deg, #f0f7ff 0%, #ffffff 70%)',
                                        boxShadow: '0 0 0 1px #dbe7f5 inset',
                                    }}
                                    onClick={() => {
                                        setError(null);
                                        setView(VIEW_AMEND);
                                    }}
                                >
                                    <div
                                        className="rounded-circle d-inline-flex align-items-center justify-content-center mb-3"
                                        style={{ width: 48, height: 48, background: '#0d6efd22', color: '#0d6efd' }}
                                    >
                                        <BsPencilSquare size={22} />
                                    </div>
                                    <div className="fw-semibold fs-5 mb-1">Chỉnh sửa vé</div>
                                    <div className="text-muted small">Thông tin · Ghế · Điểm đón/trả</div>
                                </button>
                            </div>
                        )}

                        {canTransferTrip && (
                            <div className={canAmend ? 'col-md-6' : 'col-12'}>
                                <button
                                    type="button"
                                    className="w-100 h-100 border-0 rounded-3 p-4 text-start"
                                    style={{
                                        cursor: 'pointer',
                                        background: 'linear-gradient(145deg, #fff8f0 0%, #ffffff 70%)',
                                        boxShadow: '0 0 0 1px #f0e0cc inset',
                                    }}
                                    onClick={() => {
                                        setError(null);
                                        setView(VIEW_TRANSFER);
                                    }}
                                >
                                    <div
                                        className="rounded-circle d-inline-flex align-items-center justify-content-center mb-3"
                                        style={{ width: 48, height: 48, background: '#e0440022', color: '#e04400' }}
                                    >
                                        <BsArrowLeftRight size={22} />
                                    </div>
                                    <div className="fw-semibold fs-5 mb-1">Đổi chuyến</div>
                                    <div className="text-muted small">Chuyến mới · Dùng 1 lần quyền</div>
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {view === VIEW_AMEND && (
                    <div className="d-flex flex-column gap-4">
                        {canChangePassengerInfo && (
                            <section>
                                <h6 className="fw-semibold mb-3">1. Thông tin hành khách</h6>
                                <div className="d-flex flex-column gap-3">
                                    {confirmedSeats.map((seat) => (
                                        <div key={seat.ticketDetailId} className="border rounded p-3">
                                            <div className="fw-semibold mb-2">Ghế {seat.seatCode}</div>
                                            <PassengerInfoFields
                                                form={passengerForms[seat.ticketDetailId]
                                                    || buildPassengerInitialForm(seat)}
                                                fieldErrors={passengerErrors[seat.ticketDetailId] || {}}
                                                fieldIdPrefix={`amend-${seat.ticketDetailId}`}
                                                onChange={(field, value) => handlePassengerFieldChange(
                                                    seat.ticketDetailId,
                                                    field,
                                                    value
                                                )}
                                            />
                                        </div>
                                    ))}
                                </div>
                            </section>
                        )}

                        {canChangeSeat && (
                            <section>
                                <h6 className="fw-semibold mb-3">
                                    {canChangePassengerInfo ? '2' : '1'}. Đổi ghế
                                </h6>
                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Chọn hành khách</Form.Label>
                                    <Form.Select
                                        value={activeSeatDetailId || ''}
                                        onChange={(e) => setActiveSeatDetailId(Number(e.target.value))}
                                        disabled={operationBusy}
                                    >
                                        {confirmedSeats.map((seat) => (
                                            <option key={seat.ticketDetailId} value={seat.ticketDetailId}>
                                                {seat.seatCode} — {seat.fullName}
                                                {seatDrafts[seat.ticketDetailId]
                                                    ? ` → ${seatDrafts[seat.ticketDetailId].seatCode}`
                                                    : ''}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>

                                <div className="d-flex flex-wrap gap-3 mb-3 small text-muted justify-content-center">
                                    <span className="d-inline-flex align-items-center gap-2">
                                        <SeatIcon status="CURRENT" code="" /> Hiện tại
                                    </span>
                                    <span className="d-inline-flex align-items-center gap-2">
                                        <SeatIcon status="SELECTED" code="" /> Đang chọn
                                    </span>
                                    <span className="d-inline-flex align-items-center gap-2">
                                        <SeatIcon status="LOCKED" code="" /> Đang giữ
                                    </span>
                                    <span className="d-inline-flex align-items-center gap-2">
                                        <SeatIcon status="AVAILABLE" code="" /> Trống
                                    </span>
                                </div>

                                {loadingSeats ? (
                                    <div className="text-center py-4 text-muted">
                                        <Spinner size="sm" className="me-2" />
                                        Đang tải sơ đồ ghế...
                                    </div>
                                ) : (
                                    <TripSeatMapGrid
                                        layout={seatLayout}
                                        currentTripSeatId={activeDraft ? null : activeSeat?.tripSeatId}
                                        selectedTripSeatIds={
                                            activeDraft ? [activeDraft.newTripSeatId] : []
                                        }
                                        blockedTripSeatIds={blockedTripSeatIds}
                                        sessionAvailableTripSeatIds={sessionAvailableTripSeatIds}
                                        onSeatClick={handleSameTripSeatClick}
                                    />
                                )}
                            </section>
                        )}

                        {canChangeItinerary && (
                            <section>
                                <h6 className="fw-semibold mb-3">
                                    {(canChangePassengerInfo ? 1 : 0) + (canChangeSeat ? 1 : 0) + 1}. Điểm đón / trả
                                </h6>
                                <Alert variant="light" className="border small py-2">
                                    Giữ nguyên chuyến. Không hoàn tiền chênh lệch.
                                </Alert>
                                <ItineraryStopSelection workflow={stopsWorkflow} />
                                <ItineraryChangePreview workflow={stopsWorkflow} />
                                {stopsWorkflow.error && (
                                    <Alert variant="danger" className="mt-3 mb-0 py-2 px-3 border-0">
                                        {stopsWorkflow.error}
                                    </Alert>
                                )}
                            </section>
                        )}
                    </div>
                )}

                {view === VIEW_TRANSFER && (
                    <div>
                        <TransferTripSelection workflow={transferWorkflow} />
                        {transferWorkflow.selectedTripId && (
                            <>
                                <ItineraryStopSelection workflow={transferWorkflow} />
                                <TransferSeatSelection workflow={transferWorkflow} />
                            </>
                        )}
                        <ItineraryChangePreview workflow={transferWorkflow} />
                        {transferWorkflow.error && (
                            <Alert variant="danger" className="mt-3 mb-0 py-2 px-3 border-0">
                                {transferWorkflow.error}
                            </Alert>
                        )}
                    </div>
                )}

                {view !== VIEW_CHOOSE && canConfirm && changeSummary.length > 0 && (
                    <div className="mt-3 p-3 border rounded bg-light small">
                        <div className="fw-semibold mb-2">Tóm tắt thay đổi</div>
                        <ul className="mb-0 ps-3">
                            {changeSummary.map((line) => (
                                <li key={line}>{line}</li>
                            ))}
                        </ul>
                    </div>
                )}

                {error && (
                    <Alert
                        variant="danger"
                        className="mt-3 mb-0 py-2 px-3 border-0 d-flex align-items-center gap-2"
                    >
                        <BsExclamationTriangleFill />
                        <span>{error}</span>
                    </Alert>
                )}
            </Modal.Body>

            <Modal.Footer className="bg-light border-0 rounded-bottom">
                <Button
                    variant="outline-secondary"
                    onClick={handleClose}
                    disabled={operationBusy}
                >
                    Hủy bỏ
                </Button>
                {view !== VIEW_CHOOSE && (
                    <Button
                        className="custom-btn-general px-4"
                        onClick={handleConfirm}
                        disabled={operationBusy || !canConfirm}
                    >
                        {submitting ? 'Đang lưu...' : 'Xác nhận OTP & gửi mail'}
                    </Button>
                )}
            </Modal.Footer>

        </Modal>

        <CustomerPhoneOtpModal
            show={otpOpen}
            phone={contactPhone}
            title="Xác nhận OTP trước khi đổi vé"
            description={(
                <>
                    Nhờ khách hàng cung cấp OTP gửi tới số <strong>{contactPhone}</strong> để đồng ý
                    thay đổi vé.
                </>
            )}
            onClose={() => {
                if (!submitting) {
                    setOtpOpen(false);
                    pendingConfirmRef.current = null;
                }
            }}
            onVerified={({ idToken }) => submitConfirmWithOtp(idToken)}
        />
        </>
    );
}
