import { Alert, Button, Card, Col, Form, Row } from 'react-bootstrap';
import { BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { useMemo, useState, useEffect } from 'react';
import { useCargoTicketFormOptions } from '../hooks/useCargoTicketFormOptions';
import { useAuth } from '../../auth/context/AuthContext';
import { routeApi } from '../../routes/api/routeApi';
import PhoneAutocomplete from './PhoneAutocomplete';
import CargoTicketDetailSection from './CargoTicketDetailSection';
import '../styles/CargoTicketForm.css';

const EMPTY_FORM = {
    tripId: '', customerId: '', senderName: '', senderPhone: '',
    receiverName: '', receiverPhone: '',
    totalPrice: '', description: '', feePayer: 'SENDER', codAmount: 0,
    pickupStopId: '', dropoffStopId: '', status: 'RECEIVED', soldBy: '',
    paymentMethod: 'CASH'
};

const MAX_CARGO_VOLUME_M3 = 2.5;

/** One create/update form that always displays the current order information. */
export default function CargoTicketForm({ initialData, lockedTrip, onSubmit, submitLabel = 'Lưu đơn gửi hàng' }) {
    const [formData, setFormData] = useState(() => ({
        ...EMPTY_FORM,
        ...initialData,
        soldBy: initialData?.soldBy?.staffId ?? initialData?.soldBy ?? EMPTY_FORM.soldBy
    }));
    const [draftDetails, setDraftDetails] = useState(() =>
        initialData?.details ? structuredClone(initialData.details) : []
    );
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');
    const { user } = useAuth();
    const { trips, stops, sellers, routes, loading: optionsLoading, error: optionsError } = useCargoTicketFormOptions(
        formData.pickupStopId,
        formData.dropoffStopId
    );
    const [selectedRouteId, setSelectedRouteId] = useState(() => lockedTrip?.routeId ? String(lockedTrip.routeId) : '');
    const [routeDetail, setRouteDetail] = useState(null);
    const isCreateFlow = Boolean(lockedTrip);

    useEffect(() => {
        if (!selectedRouteId) return;
        routeApi.getRouteDetail(selectedRouteId).then(r => setRouteDetail(r)).catch(() => setRouteDetail(null));
    }, [selectedRouteId]);

    useEffect(() => {
        if (sellers?.length > 0 && user?.username && !formData.soldBy && !initialData?.cargoTicketId) {
            const match = sellers.find(s => s.username === user.username);
            if (match) {
                // eslint-disable-next-line react-hooks/set-state-in-effect
                setFormData(prev => ({ ...prev, soldBy: match.staffId }));
            }
        }
    }, [sellers, user, formData.soldBy, initialData]);

    const eligibleTrips = useMemo(
        () => filterTripsForSelectedStops(trips, stops, formData.pickupStopId, formData.dropoffStopId),
        [trips, stops, formData.pickupStopId, formData.dropoffStopId]
    );

    const stopsForRoute = useMemo(() => {
        if (!selectedRouteId || !routeDetail?.routeStops?.length) return [];
        return routeDetail.routeStops.map(rs => ({
            stopPointId: rs.stopPointId,
            stopPointName: rs.stopPointName,
            city: rs.city,
            stopOrder: rs.stopOrder,
        })).sort((a, b) => a.stopOrder - b.stopOrder);
    }, [selectedRouteId, routeDetail]);

    // Existing update forms retain their endpoint-city behaviour. New orders are
    // locked to the authenticated staff member's exact agency stop.
    const { originCity, destinationCity } = useMemo(() => {
        const uniqueCities = [];
        for (const s of stopsForRoute) {
            if (s.city && !uniqueCities.includes(s.city)) uniqueCities.push(s.city);
        }
        if (uniqueCities.length < 2) return { originCity: null, destinationCity: null };
        return { originCity: uniqueCities[0], destinationCity: uniqueCities[uniqueCities.length - 1] };
    }, [stopsForRoute]);

    const pickupStopOptions = useMemo(() => {
        if (lockedTrip?.pickupStopId) {
            return stopsForRoute.filter(s => String(s.stopPointId) === String(lockedTrip.pickupStopId));
        }
        const base = originCity ? stopsForRoute.filter(s => s.city === originCity) : stopsForRoute;
        return excludeSelectedStop(base, formData.dropoffStopId);
    }, [stopsForRoute, originCity, formData.dropoffStopId, lockedTrip]);

    const dropoffStopOptions = useMemo(() => {
        if (lockedTrip?.pickupStopId) {
            const pickup = stopsForRoute.find(s => String(s.stopPointId) === String(lockedTrip.pickupStopId));
            return pickup ? stopsForRoute.filter(s => s.stopOrder > pickup.stopOrder) : [];
        }
        const base = destinationCity ? stopsForRoute.filter(s => s.city === destinationCity) : stopsForRoute;
        return excludeSelectedStop(base, formData.pickupStopId);
    }, [stopsForRoute, destinationCity, formData.pickupStopId, lockedTrip]);

    /** Keeps the primary action unavailable until every required business input is valid. */
    const isFormComplete = useMemo(
        () => isCargoTicketFormComplete(formData, draftDetails, isCreateFlow),
        [formData, draftDetails, isCreateFlow]
    );
    const occupiedVolume = useMemo(() => calculateOccupiedVolume(draftDetails), [draftDetails]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((previous) => ({
            ...previous,
            [name]: value,
            ...((name === 'pickupStopId' || name === 'dropoffStopId') && !lockedTrip ? { tripId: '' } : {})
        }));
        setError('');
    };

    const handleRouteChange = (event) => {
        setSelectedRouteId(event.target.value);
        setRouteDetail(null);
        setFormData((previous) => ({ ...previous, pickupStopId: '', dropoffStopId: '', tripId: '' }));
        setError('');
    };

    const handleAddDetail = () => {
        setDraftDetails(prev => {
            const newDetails = structuredClone(prev);
            newDetails.push({ cargoTypePriceId: '', description: '', quantity: 1, weightKg: '', length: '', width: '', height: '', dimensionVol: '' });
            return newDetails;
        });
    };

    const handleDetailChange = (index, field, value) => {
        setDraftDetails(prev => {
            const newDetails = structuredClone(prev);
            newDetails[index][field] = value;
            return newDetails;
        });
    };

    const handleRemoveDetail = (index) => {
        setDraftDetails(prev => {
            const newDetails = structuredClone(prev);
            newDetails.splice(index, 1);
            return newDetails;
        });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (occupiedVolume > MAX_CARGO_VOLUME_M3) {
            setError('Tổng thể tích hàng hóa (thể tích × số lượng) không được vượt quá 2,5 m³.');
            return;
        }
        if (!isFormComplete) {
            setError('Vui lòng điền đầy đủ và đúng tất cả thông tin bắt buộc.');
            return;
        }
        if (String(formData.pickupStopId) === String(formData.dropoffStopId)) {
            setError('Điểm nhận và điểm trả hàng phải khác nhau.');
            return;
        }

        if (!initialData?.cargoTicketId && draftDetails.length === 0) {
            setError('Vui lòng thêm ít nhất một chi tiết hàng hóa.');
            return;
        }

        const payload = buildCargoTicketRequest(formData, draftDetails);
        setSubmitting(true);
        try {
            await onSubmit(payload);
        } catch (err) {
            const response = err.response?.data;
            const validationDetails = response?.fieldErrors
                ? Object.values(response.fieldErrors).join(' ')
                : '';
            setError(validationDetails || response?.message || 'Không thể lưu đơn gửi hàng.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Form id="cargo-ticket-form" onSubmit={handleSubmit}>
            {(error || optionsError) && <Alert variant="danger" className="d-flex align-items-center gap-2"><BsExclamationTriangleFill />{error || optionsError}</Alert>}

            <Row className="g-4 mb-4">
                <Col lg={6}><PartyCard title="Người gửi" prefix="sender" data={formData} onChange={handleChange} setFormData={setFormData} /></Col>
                <Col lg={6}><PartyCard title="Người nhận" prefix="receiver" data={formData} onChange={handleChange} setFormData={setFormData} /></Col>
            </Row>

            <Card className="shadow-sm border-0 mb-4">
                <Card.Header className="bg-white py-3"><h5 className="fw-bold mb-0">Thông tin vé và hành trình</h5></Card.Header>
                <Card.Body className="p-4">
                    <Row className="g-3">
                        {!isCreateFlow && <>
                            <Col md={4}><Dropdown label="Tuyến đường" name="routeId" value={selectedRouteId} onChange={handleRouteChange} loading={optionsLoading} options={routes} optionValue="routeId" renderOption={(item) => item.routeName} emptyLabel="-- Chọn tuyến --" /></Col>
                            <Col md={4}><Dropdown label="Điểm nhận" name="pickupStopId" value={formData.pickupStopId} onChange={handleChange} loading={optionsLoading} options={pickupStopOptions} optionValue="stopPointId" renderOption={(item) => `${item.stopPointName} (${item.city})`} required disabled={!selectedRouteId} emptyLabel="-- Chọn tuyến trước --" /></Col>
                        </>}
                        <Col md={isCreateFlow ? 6 : 4}><Dropdown label="Điểm trả" name="dropoffStopId" value={formData.dropoffStopId} onChange={handleChange} loading={optionsLoading} options={dropoffStopOptions} optionValue="stopPointId" renderOption={(item) => `${item.stopPointName} (${item.city})`} required disabled={!selectedRouteId} emptyLabel="-- Chọn điểm trả --" /></Col>
                        <Col md={isCreateFlow ? 6 : 8}>{isCreateFlow
                            ? <CoachSummary lockedTrip={lockedTrip} />
                            : <Dropdown label="Chuyến đi phù hợp" name="tripId" value={formData.tripId ?? ''} onChange={handleChange} loading={optionsLoading} options={eligibleTrips} optionValue="tripId" renderOption={tripLabel} disabled={!formData.pickupStopId || !formData.dropoffStopId} />}
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            <Card className="shadow-sm border-0 mb-4">
                <Card.Header className="bg-white py-3"><h5 className="fw-bold mb-0">Thanh toán và xử lý</h5></Card.Header>
                <Card.Body className="p-4">
                    <Row className="g-3">
                        {!isCreateFlow && <Col md={4}><Field label="Tiền thu hộ COD (VNĐ)" name="codAmount" type="number" value={formData.codAmount} onChange={handleChange} required min="0" /></Col>}
                        <Col md={isCreateFlow ? 6 : 4}><Form.Group><Form.Label className="fw-semibold">Người trả phí *</Form.Label><Form.Select name="feePayer" value={formData.feePayer} onChange={handleChange}><option value="SENDER">Người gửi</option><option value="RECEIVER">Người nhận</option></Form.Select></Form.Group></Col>
                        <Col md={isCreateFlow ? 6 : 4}><Form.Group><Form.Label className="fw-semibold">Phương thức thanh toán *</Form.Label><Form.Select name="paymentMethod" value={formData.paymentMethod} onChange={handleChange} required><option value="CASH">Tiền mặt</option><option value="BANK_TRANSFER">Chuyển khoản</option></Form.Select></Form.Group></Col>
                        {!isCreateFlow && <Col xs={12}><Form.Group><Form.Label className="fw-semibold">Mô tả hàng hóa</Form.Label><Form.Control as="textarea" rows={3} name="description" value={formData.description || ''} onChange={handleChange} /></Form.Group></Col>}
                    </Row>
                </Card.Body>
            </Card>

            <CargoTicketDetailSection
                draftDetails={draftDetails}
                onAdd={handleAddDetail}
                onChange={handleDetailChange}
                onRemove={handleRemoveDetail}
            />

            {occupiedVolume > MAX_CARGO_VOLUME_M3 && (
                <Alert variant="danger" className="cargo-volume-limit-alert">
                    Tổng thể tích hiện tại là {formatVolume(occupiedVolume)} m³, vượt giới hạn 2,5 m³.
                </Alert>
            )}

            <Button type="submit" disabled={!isFormComplete || submitting || optionsLoading || Boolean(optionsError)} className="px-4 py-2 d-flex align-items-center gap-2 custom-btn-general">
                <BsCheckCircle />{submitting ? 'Đang lưu...' : submitLabel}
            </Button>
        </Form>
    );
}

/** Shows the selected vehicle without exposing an implementation-facing trip identifier. */
function CoachSummary({ lockedTrip }) {
    return (
        <div className="cargo-form-coach-summary" aria-label={`Xe thực hiện ${lockedTrip.licensePlate || 'chưa có biển số'}`}>
            <span>Xe thực hiện</span>
            <strong>{lockedTrip.licensePlate || 'Chưa gán biển số'}</strong>
            <small>Ghé văn phòng lúc {formatDateTime(lockedTrip.pickupTime)}</small>
        </div>
    );
}

function PartyCard({ title, prefix, data, onChange, setFormData }) {
    return <Card className="shadow-sm border-0 h-100"><Card.Header className="bg-white py-3"><h5 className="fw-bold mb-0">{title}</h5></Card.Header><Card.Body className="p-4 d-flex flex-column gap-3">
        <PhoneAutocomplete label="Số điện thoại" prefix={prefix} data={data} onChange={onChange} setFormData={setFormData} />
        <Field label="Họ tên" name={`${prefix}Name`} value={data[`${prefix}Name`]} onChange={onChange} required maxLength={100} />
    </Card.Body></Card>;
}

function Field({ label, ...props }) {
    return <Form.Group><Form.Label className="fw-semibold">{label}{props.required ? ' *' : ''}</Form.Label><Form.Control {...props} /></Form.Group>;
}

function Dropdown({ label, options, optionValue, renderOption, loading, emptyLabel = '-- Chọn --', required = false, disabled = false, ...props }) {
    return (
        <Form.Group>
            <Form.Label className="fw-semibold">{label}{required ? ' *' : ''}</Form.Label>
            <Form.Select {...props} required={required} disabled={loading || disabled}>
                <option value="">{loading ? 'Đang tải dữ liệu...' : emptyLabel}</option>
                {options.map((option) => (
                    <option key={option[optionValue]} value={option[optionValue]}>
                        {renderOption(option)}
                    </option>
                ))}
            </Form.Select>
        </Form.Group>
    );
}

const TRIP_STATUS_MAP = {
    'SCHEDULED': 'Đã lên lịch',
    'IN_PROGRESS': 'Đang chạy',
    'COMPLETED': 'Hoàn thành',
    'CANCELLED': 'Đã hủy'
};

const tripLabel = (trip) => {
    const time = trip.departureTime ? formatDateTime(trip.departureTime) : 'Chưa có giờ';
    const localizedStatus = trip.status ? (TRIP_STATUS_MAP[trip.status] || trip.status) : '';
    const coachType = trip.coachTypeName ? ` - Loại xe: ${trip.coachTypeName}` : '';
    return `Mã: ${trip.tripId} - Giờ chạy: ${time}${coachType} - [${localizedStatus}]`;
};

function formatDateTime(value) {
    if (!value) return 'Chưa có giờ';
    const date = new Date(value);
    const datePart = date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    const timePart = date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', hour12: false });
    return `${timePart} ${datePart}`;
}

function filterTripsForSelectedStops(trips, stops, pickupStopId, dropoffStopId) {
    const hasPickup = stops.some((stop) => String(stop.stopPointId) === String(pickupStopId));
    const hasDropoff = stops.some((stop) => String(stop.stopPointId) === String(dropoffStopId));
    if (!hasPickup || !hasDropoff) return [];
    return trips.filter((trip) => {
        // Backend already filters by time and stops. 
        // We only check this to prevent showing stale trips from a previous selection while loading.
        if (trip.pickupStopId !== undefined && String(trip.pickupStopId) !== String(pickupStopId)) return false;
        if (trip.dropoffStopId !== undefined && String(trip.dropoffStopId) !== String(dropoffStopId)) return false;
        return true;
    });
}

function excludeSelectedStop(stops, selectedStopId) {
    return stops.filter((stop) => String(stop.stopPointId) !== String(selectedStopId));
}

function optionalId(value) {
    return value === '' || value === null || value === undefined ? null : Number(value);
}

function optionalText(value) {
    const normalized = String(value ?? '').trim();
    return normalized || null;
}

/** Validates all visible and system-owned fields before enabling form submission. */
function isCargoTicketFormComplete(form, details, requireDimensions) {
    const requiredText = [form.senderName, form.senderPhone, form.receiverName, form.receiverPhone];
    const headerIsComplete = requiredText.every(value => String(value ?? '').trim())
        && Number(form.tripId) > 0
        && Number(form.pickupStopId) > 0
        && Number(form.dropoffStopId) > 0
        && String(form.pickupStopId) !== String(form.dropoffStopId)
        && Number(form.soldBy) > 0
        && ['SENDER', 'RECEIVER'].includes(form.feePayer)
        && ['CASH', 'BANK_TRANSFER'].includes(form.paymentMethod);

    const detailsAreComplete = details.length > 0 && details.every(detail => {
        const baseIsComplete = Number(detail.cargoTypePriceId) > 0
            && Number.isInteger(Number(detail.quantity))
            && Number(detail.quantity) > 0
            && Number(detail.weightKg) > 0
            && Number(detail.dimensionVol) > 0;
        const dimensionsAreComplete = !requireDimensions
            || [detail.length, detail.width, detail.height].every(value => Number(value) > 0);
        return baseIsComplete && dimensionsAreComplete;
    });
    return headerIsComplete
        && detailsAreComplete
        && calculateOccupiedVolume(details) <= MAX_CARGO_VOLUME_M3;
}

/** Calculates order volume using the backend business formula. */
function calculateOccupiedVolume(details) {
    return details.reduce((total, detail) => {
        const dimensionVol = Number(detail.dimensionVol);
        const quantity = Number(detail.quantity);
        if (!Number.isFinite(dimensionVol) || !Number.isFinite(quantity)) return total;
        return total + (dimensionVol * quantity);
    }, 0);
}

/** Formats cubic metres without exposing floating-point noise in the warning. */
function formatVolume(value) {
    return Number(value).toLocaleString('vi-VN', { maximumFractionDigits: 6 });
}

function buildCargoTicketRequest(form, draftDetails) {
    return {
        tripId: optionalId(form.tripId),
        customerId: optionalId(form.customerId),
        senderName: form.senderName.trim(),
        senderPhone: form.senderPhone.trim(),
        receiverName: form.receiverName.trim(),
        receiverPhone: form.receiverPhone.trim(),
        totalPrice: Number(form.totalPrice),
        description: optionalText(form.description),
        feePayer: form.feePayer,
        codAmount: Number(form.codAmount),
        pickupStopId: Number(form.pickupStopId),
        dropoffStopId: Number(form.dropoffStopId),
        status: form.status,
        soldBy: form.soldBy ? { staffId: Number(form.soldBy) } : null,
        paymentMethod: form.paymentMethod,
        details: (draftDetails || []).map(d => ({
            cargoTicketDetailId: d.cargoTicketDetailId,
            cargoTypePriceId: Number(d.cargoTypePriceId),
            description: optionalText(d.description),
            quantity: Number(d.quantity),
            weightKg: Number(d.weightKg),
            dimensionVol: Number(d.dimensionVol)

        }))
    };
}
