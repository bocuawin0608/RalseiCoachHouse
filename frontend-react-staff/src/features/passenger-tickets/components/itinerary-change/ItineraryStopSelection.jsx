import { Col, Form, Row } from 'react-bootstrap';

export default function ItineraryStopSelection({ workflow }) {
    const {
        pickupCity,
        dropoffCity,
        pickupStopId,
        dropoffStopId,
        pickupOptions,
        dropoffOptions,
        loadingStops,
        interactionDisabled,
        handlePickupChange,
        handleDropoffChange,
    } = workflow;

    return (
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
                            onChange={(event) => handlePickupChange(event.target.value)}
                            disabled={loadingStops || interactionDisabled}
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
                            onChange={(event) => handleDropoffChange(event.target.value)}
                            disabled={loadingStops || interactionDisabled}
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
    );
}
