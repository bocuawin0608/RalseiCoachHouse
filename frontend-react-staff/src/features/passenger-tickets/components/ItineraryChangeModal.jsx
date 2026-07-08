import { Alert, Badge, Button, Col, Form, Modal, Row, Spinner } from 'react-bootstrap';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import axiosClient from '../../../api/axiosClient';
import { useRouteDropdown } from '../../../hooks/useRouteDropdown';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';
import { formatCurrency, formatDateTime } from '../utils/passengerTicketFormatters';
import { buildItineraryStopOptions } from '../utils/itineraryStopOptions';
import TripSeatMapGrid, { buildSeatLayout } from './TripSeatMapGrid';
import SeatIcon from '../../../components/common/SeatIcon';

function createHoldToken() {
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
        return crypto.randomUUID().replace(/-/g, '');
    }
    return `${Date.now()}${Math.random().toString(36).slice(2)}`;
}

function toDateInputValue(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

export default function ItineraryChangeModal({
    isOpen,
    ticket,
    onClose,
    onSuccess,
}) {
    const holdTokenRef = useRef('');
    const [keepCurrentTrip, setKeepCurrentTrip] = useState(true);
    const [departureDate, setDepartureDate] = useState('');
    const [selectedRouteId, setSelectedRouteId] = useState('');
    const [candidates, setCandidates] = useState([]);
    const [selectedTripId, setSelectedTripId] = useState(null);
    const [stops, setStops] = useState([]);
    const [pickupStopId, setPickupStopId] = useState('');
    const [dropoffStopId, setDropoffStopId] = useState('');
    const [seatList, setSeatList] = useState([]);
    const [selectedTripSeatIds, setSelectedTripSeatIds] = useState([]);
    const [preview, setPreview] = useState(null);
    const [loadingCandidates, setLoadingCandidates] = useState(false);
    const [loadingStops, setLoadingStops] = useState(false);
    const [loadingSeats, setLoadingSeats] = useState(false);
    const [locking, setLocking] = useState(false);
    const [previewing, setPreviewing] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const confirmedSeatCount = useMemo(
        () => ticket?.seats?.filter((seat) => seat.status === 'CONFIRMED').length ?? 0,
        [ticket]
    );

    const majorChangeUsed = Boolean(ticket?.majorChangeType);

    const layout = useMemo(() => buildSeatLayout(seatList), [seatList]);

    const { routes: routeOptions, loading: loadingRoutes } = useRouteDropdown(isOpen && !keepCurrentTrip);

    const { pickupOptions, dropoffOptions, pickupCity, dropoffCity } = useMemo(
        () => buildItineraryStopOptions(stops, pickupStopId, dropoffStopId),
        [stops, pickupStopId, dropoffStopId]
    );

    const canSearchTransferTrips = Boolean(!keepCurrentTrip && departureDate && selectedRouteId);

    const releaseLockedSeats = useCallback(async (tripId, seatIds) => {
        if (!tripId || !seatIds?.length || !holdTokenRef.current) return;
        try {
            await staffPassengerTicketApi.releaseSeats(tripId, seatIds, holdTokenRef.current);
        } catch {
            // Best-effort release.
        }
    }, []);

    const syncSeatLocks = useCallback(async (tripId, currentLockedIds, desiredSeatIds) => {
        if (!tripId) return;

        setLocking(true);
        try {
            if (currentLockedIds.length) {
                await releaseLockedSeats(tripId, currentLockedIds);
            }
            if (desiredSeatIds.length) {
                await staffPassengerTicketApi.lockSeats(
                    tripId,
                    desiredSeatIds,
                    holdTokenRef.current,
                    'ITINERARY'
                );
            }
            setSelectedTripSeatIds(desiredSeatIds);
        } finally {
            setLocking(false);
        }
    }, [releaseLockedSeats]);

    const loadStops = useCallback(async (tripId, initialPickupStopId = null, initialDropoffStopId = null) => {
        if (!tripId) return;

        setLoadingStops(true);
        setError(null);
        try {
            const response = await axiosClient.get(`/v1/trips/${tripId}/stops`);
            const loadedStops = response || [];
            setStops(loadedStops);

            if (initialPickupStopId) {
                setPickupStopId(String(initialPickupStopId));
            }
            if (initialDropoffStopId) {
                setDropoffStopId(String(initialDropoffStopId));
            }
        } catch (requestError) {
            setStops([]);
            setError(requestError.response?.data?.message || 'Không thể tải điểm dừng.');
        } finally {
            setLoadingStops(false);
        }
    }, []);

    const loadSeatMap = useCallback(async (tripId) => {
        if (!tripId) return;

        setLoadingSeats(true);
        setError(null);
        try {
            const response = await staffPassengerTicketApi.getTripSeatMap(tripId);
            setSeatList(response || []);
        } catch (requestError) {
            setSeatList([]);
            setError(requestError.response?.data?.message || 'Không thể tải sơ đồ ghế.');
        } finally {
            setLoadingSeats(false);
        }
    }, []);

    const loadCandidates = useCallback(async () => {
        if (!ticket?.ticketCode || !departureDate || !selectedRouteId) return;

        setLoadingCandidates(true);
        setError(null);
        try {
            const response = await staffPassengerTicketApi.getTransferCandidates(
                ticket.ticketCode,
                {
                    departureDate,
                    routeId: Number(selectedRouteId),
                    excludeCurrentTrip: true,
                }
            );
            setCandidates(response || []);
        } catch (requestError) {
            setCandidates([]);
            setError(requestError.response?.data?.message || 'Không thể tải danh sách chuyến.');
        } finally {
            setLoadingCandidates(false);
        }
    }, [departureDate, selectedRouteId, ticket?.ticketCode]);

    const runPreview = useCallback(async () => {
        if (!ticket?.ticketCode || !pickupStopId || !dropoffStopId) return;
        if (!keepCurrentTrip && selectedTripSeatIds.length !== confirmedSeatCount) {
            setPreview(null);
            return;
        }

        setPreviewing(true);
        setError(null);
        try {
            const response = await staffPassengerTicketApi.previewItinerary(
                ticket.ticketCode,
                {
                    newTripId: keepCurrentTrip ? null : selectedTripId,
                    pickupStopId: Number(pickupStopId),
                    dropoffStopId: Number(dropoffStopId),
                    newTripSeatIds: keepCurrentTrip ? undefined : selectedTripSeatIds,
                }
            );
            setPreview(response);
        } catch (requestError) {
            setPreview(null);
            setError(requestError.response?.data?.message || 'Không thể xem trước giá vé.');
        } finally {
            setPreviewing(false);
        }
    }, [
        confirmedSeatCount,
        dropoffStopId,
        keepCurrentTrip,
        pickupStopId,
        selectedTripId,
        selectedTripSeatIds,
        ticket?.ticketCode,
    ]);

    useEffect(() => {
        if (!isOpen || !ticket) return;

        holdTokenRef.current = createHoldToken();
        setKeepCurrentTrip(true);
        setDepartureDate(ticket.departureTime ? toDateInputValue(new Date(ticket.departureTime)) : toDateInputValue(new Date()));
        setSelectedRouteId(ticket.routeId ? String(ticket.routeId) : '');
        setCandidates([]);
        setSelectedTripId(null);
        setPickupStopId('');
        setDropoffStopId('');
        setSeatList([]);
        setSelectedTripSeatIds([]);
        setPreview(null);
        setError(null);
        loadStops(ticket.tripId, ticket.pickupStopId, ticket.dropoffStopId);
    }, [isOpen, ticket?.ticketCode, ticket?.tripId, ticket?.departureTime, ticket?.pickupStopId, ticket?.dropoffStopId, loadStops]);

    useEffect(() => {
        if (!isOpen || keepCurrentTrip || !canSearchTransferTrips) {
            if (!keepCurrentTrip && isOpen) {
                setCandidates([]);
                setSelectedTripId(null);
            }
            return;
        }

        setSelectedTripId(null);
        setSelectedTripSeatIds([]);
        setPreview(null);
        loadCandidates();
    }, [canSearchTransferTrips, departureDate, isOpen, keepCurrentTrip, loadCandidates, selectedRouteId]);

    useEffect(() => {
        if (!isOpen || keepCurrentTrip || !selectedTripId) return;
        setPickupStopId('');
        setDropoffStopId('');
        setSelectedTripSeatIds([]);
        setPreview(null);
        loadStops(selectedTripId);
        loadSeatMap(selectedTripId);
    }, [isOpen, keepCurrentTrip, selectedTripId, loadSeatMap, loadStops]);

    useEffect(() => {
        if (!isOpen || !pickupStopId || !dropoffStopId) {
            setPreview(null);
            return;
        }
        runPreview();
    }, [isOpen, pickupStopId, dropoffStopId, selectedTripSeatIds, keepCurrentTrip, selectedTripId, runPreview]);

    const handleKeepTripChange = async (nextKeepCurrentTrip) => {
        if (majorChangeUsed && !nextKeepCurrentTrip) return;
        if (nextKeepCurrentTrip === keepCurrentTrip) return;

        if (!nextKeepCurrentTrip && selectedTripSeatIds.length) {
            await releaseLockedSeats(selectedTripId, selectedTripSeatIds);
        }

        setKeepCurrentTrip(nextKeepCurrentTrip);
        setSelectedTripId(null);
        setSelectedTripSeatIds([]);
        setCandidates([]);
        setPreview(null);
        setPickupStopId('');
        setDropoffStopId('');

        if (nextKeepCurrentTrip) {
            loadStops(ticket.tripId, ticket.pickupStopId, ticket.dropoffStopId);
            setSeatList([]);
        } else {
            setSelectedRouteId(ticket.routeId ? String(ticket.routeId) : '');
        }
    };

    const handleRouteChange = (nextRouteId) => {
        setSelectedRouteId(nextRouteId);
        setSelectedTripId(null);
        setSelectedTripSeatIds([]);
        setCandidates([]);
        setPickupStopId('');
        setDropoffStopId('');
        setStops([]);
        setPreview(null);
    };

    const handleDepartureDateChange = (nextDate) => {
        setDepartureDate(nextDate);
        setSelectedTripId(null);
        setSelectedTripSeatIds([]);
        setCandidates([]);
        setPickupStopId('');
        setDropoffStopId('');
        setStops([]);
        setSeatList([]);
        setPreview(null);
    };

    const handleSeatClick = async (clickedSeat) => {
        if (keepCurrentTrip || locking || clickedSeat.status !== 'AVAILABLE') return;

        const isSelected = selectedTripSeatIds.includes(clickedSeat.tripSeatId);
        let nextSelection;

        if (isSelected) {
            nextSelection = selectedTripSeatIds.filter((id) => id !== clickedSeat.tripSeatId);
        } else if (selectedTripSeatIds.length >= confirmedSeatCount) {
            setError(`Chỉ được chọn tối đa ${confirmedSeatCount} ghế.`);
            return;
        } else {
            nextSelection = [...selectedTripSeatIds, clickedSeat.tripSeatId];
        }

        setError(null);
        try {
            await syncSeatLocks(selectedTripId, selectedTripSeatIds, nextSelection);
        } catch (requestError) {
            setError(requestError.response?.data?.message || 'Không thể giữ ghế. Vui lòng thử lại.');
        }
    };

    const hasNoChanges = useMemo(() => keepCurrentTrip
        && ticket?.pickupStopId
        && ticket?.dropoffStopId
        && Number(pickupStopId) === Number(ticket.pickupStopId)
        && Number(dropoffStopId) === Number(ticket.dropoffStopId),
    [keepCurrentTrip, ticket?.pickupStopId, ticket?.dropoffStopId, pickupStopId, dropoffStopId]);

    const handleClose = async () => {
        if (!keepCurrentTrip && selectedTripSeatIds.length) {
            await releaseLockedSeats(selectedTripId, selectedTripSeatIds);
        }
        setSelectedTripSeatIds([]);
        setPreview(null);
        setError(null);
        onClose();
    };

    const handleSubmit = async () => {
        if (!ticket || !preview?.eligible || hasNoChanges) return;

        setSubmitting(true);
        setError(null);

        try {
            const updatedTicket = await staffPassengerTicketApi.changeItinerary(
                ticket.ticketCode,
                {
                    newTripId: keepCurrentTrip ? null : selectedTripId,
                    pickupStopId: Number(pickupStopId),
                    dropoffStopId: Number(dropoffStopId),
                    newTripSeatIds: keepCurrentTrip ? undefined : selectedTripSeatIds,
                },
                keepCurrentTrip ? undefined : holdTokenRef.current
            );
            setSelectedTripSeatIds([]);
            onSuccess?.(updatedTicket);
            onClose();
        } catch (requestError) {
            setError(requestError.response?.data?.message || 'Không thể đổi hành trình.');
        } finally {
            setSubmitting(false);
        }
    };

    if (!isOpen || !ticket) return null;

    const canSubmit = Boolean(
        preview?.eligible
        && pickupStopId
        && dropoffStopId
        && !hasNoChanges
        && (keepCurrentTrip || (selectedTripId && selectedTripSeatIds.length === confirmedSeatCount))
    );

    return (
        <Modal show={isOpen} onHide={handleClose} size="lg" centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">Đổi hành trình</Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4">
                <Alert variant="info" className="py-2 px-3 border-0 small">
                    Chỉ đổi được sang lựa chọn có giá ≤ giá vé hiện tại. Không hoàn tiền chênh lệch.
                    Mã QR lên xe không đổi.
                </Alert>

                <div className="mb-3 p-3 bg-light border rounded small">
                    <div className="fw-semibold mb-1">Chuyến hiện tại</div>
                    <div>{ticket.routeName} • {formatDateTime(ticket.departureTime)}</div>
                    <div className="text-muted">{ticket.pickupStopName} → {ticket.dropoffStopName}</div>
                </div>

                {majorChangeUsed && (
                    <Alert variant="warning" className="py-2 px-3 border-0 small">
                        Vé đã sử dụng quyền đổi chuyến hoặc hủy vé. Chỉ có thể đổi điểm đón/trả trên chuyến hiện tại.
                    </Alert>
                )}

                <Form.Check
                    type="switch"
                    id="keep-current-trip"
                    className="mb-3"
                    label="Giữ nguyên chuyến hiện tại"
                    checked={keepCurrentTrip}
                    disabled={majorChangeUsed}
                    onChange={(event) => handleKeepTripChange(event.target.checked)}
                />

                {!keepCurrentTrip && (
                    <>
                        <Row className="g-3 mb-3">
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Tuyến đường</Form.Label>
                                    <Form.Select
                                        value={selectedRouteId}
                                        onChange={(event) => handleRouteChange(event.target.value)}
                                        disabled={loadingRoutes}
                                    >
                                        <option value="">-- Chọn tuyến --</option>
                                        {routeOptions.map((route) => (
                                            <option key={route.routeId} value={route.routeId}>
                                                {route.routeName}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Ngày khởi hành</Form.Label>
                                    <Form.Control
                                        type="date"
                                        value={departureDate}
                                        onChange={(event) => handleDepartureDateChange(event.target.value)}
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        {!canSearchTransferTrips && (
                            <Alert variant="secondary" className="py-2 px-3 border-0 small">
                                Chọn tuyến đường và ngày khởi hành để xem danh sách chuyến phù hợp.
                            </Alert>
                        )}

                        {canSearchTransferTrips && loadingCandidates && (
                            <div className="py-2 text-muted small">
                                <Spinner animation="border" size="sm" className="me-2" />
                                Đang tải chuyến khả dụng...
                            </div>
                        )}

                        {canSearchTransferTrips && !loadingCandidates && (
                            <Form.Group className="mb-3">
                                <Form.Label>Chọn chuyến mới</Form.Label>
                                <Form.Select
                                    value={selectedTripId ?? ''}
                                    onChange={(event) => setSelectedTripId(Number(event.target.value) || null)}
                                >
                                    <option value="">-- Chọn chuyến --</option>
                                    {candidates.map((candidate) => (
                                        <option key={candidate.tripId} value={candidate.tripId}>
                                            {formatDateTime(candidate.departureTime)}
                                            {' • '}
                                            {candidate.coachTypeName}
                                            {' • '}
                                            {formatCurrency(candidate.seatPrice)}
                                            {' • '}
                                            Còn {candidate.availableSeats} ghế
                                        </option>
                                    ))}
                                </Form.Select>
                                {!candidates.length && (
                                    <Form.Text className="text-muted">
                                        Không có chuyến phù hợp cho tuyến và ngày đã chọn.
                                    </Form.Text>
                                )}
                            </Form.Group>
                        )}
                    </>
                )}

                {(keepCurrentTrip || selectedTripId) && (
                    <>
                        {(pickupCity || dropoffCity) && (
                            <div className="mb-2 small text-muted">
                                Điểm đón thuộc <strong>{pickupCity || '---'}</strong>
                                {' • '}
                                Điểm trả thuộc <strong>{dropoffCity || '---'}</strong>
                            </div>
                        )}
                        <Row className="g-3 mb-3">
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Điểm đón</Form.Label>
                                    <Form.Select
                                        value={pickupStopId}
                                        onChange={(event) => setPickupStopId(event.target.value)}
                                        disabled={loadingStops}
                                    >
                                        <option value="">-- Chọn điểm đón --</option>
                                        {pickupOptions.map((stop) => (
                                            <option key={stop.stopPointId} value={stop.stopPointId}>
                                                {stop.stopPointName}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                            <Col md={6}>
                                <Form.Group>
                                    <Form.Label>Điểm trả</Form.Label>
                                    <Form.Select
                                        value={dropoffStopId}
                                        onChange={(event) => setDropoffStopId(event.target.value)}
                                        disabled={loadingStops}
                                    >
                                        <option value="">-- Chọn điểm trả --</option>
                                        {dropoffOptions.map((stop) => (
                                            <option key={stop.stopPointId} value={stop.stopPointId}>
                                                {stop.stopPointName}
                                            </option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </Col>
                        </Row>
                    </>
                )}

                {!keepCurrentTrip && selectedTripId && (
                    <>
                        <div className="mb-2 small text-muted">
                            Chọn {confirmedSeatCount} ghế trên chuyến mới ({selectedTripSeatIds.length}/{confirmedSeatCount})
                        </div>

                        <div className="d-flex flex-wrap gap-3 mb-3 small text-muted justify-content-center">
                            <span className="d-inline-flex align-items-center gap-2">
                                <SeatIcon status="SELECTED" code="" /> Đã chọn
                            </span>
                            <span className="d-inline-flex align-items-center gap-2">
                                <SeatIcon status="AVAILABLE" code="" /> Còn trống
                            </span>
                            <span className="d-inline-flex align-items-center gap-2">
                                <SeatIcon status="SOLD" code="" /> Đã đặt
                            </span>
                        </div>

                        {loadingSeats && (
                            <div className="py-3 text-center">
                                <Spinner animation="border" size="sm" className="me-2" />
                                Đang tải sơ đồ ghế...
                            </div>
                        )}

                        {!loadingSeats && (
                            <TripSeatMapGrid
                                layout={layout}
                                selectedTripSeatIds={selectedTripSeatIds}
                                maxSelectable={confirmedSeatCount}
                                onSeatClick={handleSeatClick}
                            />
                        )}
                    </>
                )}

                {previewing && (
                    <div className="mt-3 small text-muted">
                        <Spinner animation="border" size="sm" className="me-2" />
                        Đang kiểm tra điều kiện giá...
                    </div>
                )}

                {preview && !previewing && (
                    <div className="mt-3 p-3 border rounded bg-white">
                        <div className="d-flex flex-wrap justify-content-between gap-2 mb-2">
                            <span>Giá đã thanh toán: <strong>{formatCurrency(preview.originalNetPaid)}</strong></span>
                            <span>Giá mới: <strong>{formatCurrency(preview.newNetPaid)}</strong></span>
                        </div>
                        <Badge bg={preview.eligible ? 'success' : 'danger'}>
                            {preview.eligible ? 'Đủ điều kiện đổi hành trình' : (preview.ineligibleReason || 'Không đủ điều kiện')}
                        </Badge>
                    </div>
                )}

                {hasNoChanges && pickupStopId && dropoffStopId && (
                    <Alert variant="secondary" className="mt-3 mb-0 py-2 px-3 border-0 small">
                        Hành trình không thay đổi so với vé hiện tại. Không cần lưu.
                    </Alert>
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
                    disabled={!canSubmit || submitting || locking || previewing}
                >
                    {submitting ? 'Đang lưu...' : 'Xác nhận đổi hành trình'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
