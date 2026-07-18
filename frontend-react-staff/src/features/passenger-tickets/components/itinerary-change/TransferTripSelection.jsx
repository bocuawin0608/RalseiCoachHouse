import { Alert, Col, Form, Row, Spinner } from 'react-bootstrap';
import { formatCurrency, formatDateTime } from '../../utils/passengerTicketFormatters';

export default function TransferTripSelection({ workflow }) {
    const {
        selectedRouteId,
        departureDate,
        routeOptions,
        loadingRoutes,
        candidates,
        selectedTripId,
        canSearchTransferTrips,
        loadingCandidates,
        interactionDisabled,
        handleRouteChange,
        handleDepartureDateChange,
        handleTripChange,
    } = workflow;

    return (
        <>
            <Row className="g-3 mb-3">
                <Col md={6}>
                    <Form.Group>
                        <Form.Label>Tuyến đường</Form.Label>
                        <Form.Select
                            value={selectedRouteId}
                            onChange={(event) => handleRouteChange(event.target.value)}
                            disabled={loadingRoutes || interactionDisabled}
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
                            disabled={interactionDisabled}
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
                        onChange={(event) => handleTripChange(Number(event.target.value) || null)}
                        disabled={interactionDisabled}
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
    );
}
