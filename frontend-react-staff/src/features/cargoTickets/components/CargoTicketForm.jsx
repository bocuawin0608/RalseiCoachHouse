import { Alert, Button, Card, Col, Form, Row } from 'react-bootstrap';
import { BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { useMemo, useState, useEffect } from 'react';
import { useCargoTicketFormOptions } from '../hooks/useCargoTicketFormOptions';
import { useAuth } from '../../auth/context/AuthContext';
import PhoneAutocomplete from './PhoneAutocomplete';
import CargoTicketDetailSection from './CargoTicketDetailSection';

const EMPTY_FORM = {
    tripId: '', customerId: '', senderName: '', senderPhone: '',
    receiverName: '', receiverPhone: '',
    totalPrice: '', description: '', feePayer: 'SENDER', codAmount: 0,
    pickupStopId: '', dropoffStopId: '', status: 'RECEIVED', soldBy: '',
    paymentMethod: 'CASH'
};

export default function CargoTicketForm({ initialData, onSubmit, submitLabel = 'Lưu đơn gửi hàng' }) {
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
    const { trips, customers, stops, sellers, loading: optionsLoading, error: optionsError } = useCargoTicketFormOptions(
        formData.pickupStopId,
        formData.dropoffStopId
    );

    useEffect(() => {
        if (sellers?.length > 0 && user?.username && !formData.soldBy && !initialData?.cargoTicketId) {
            const match = sellers.find(s => s.username === user.username);
            if (match) {
                setFormData(prev => ({ ...prev, soldBy: match.staffId }));
            }
        }
    }, [sellers, user, formData.soldBy, initialData]);
    const eligibleTrips = useMemo(
        () => filterTripsForSelectedStops(trips, stops, formData.pickupStopId, formData.dropoffStopId),
        [trips, stops, formData.pickupStopId, formData.dropoffStopId]
    );
    const pickupStopOptions = useMemo(
        () => excludeSelectedStop(stops, formData.dropoffStopId),
        [stops, formData.dropoffStopId]
    );
    const dropoffStopOptions = useMemo(
        () => excludeSelectedStop(stops, formData.pickupStopId),
        [stops, formData.pickupStopId]
    );

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((previous) => ({
            ...previous,
            [name]: value,
            ...((name === 'pickupStopId' || name === 'dropoffStopId') ? { tripId: '' } : {})
        }));
        setError('');
    };

    const handleAddDetail = () => {
        setDraftDetails(prev => {
            const newDetails = structuredClone(prev);
            newDetails.push({ cargoTypePriceId: '', description: '', quantity: 1, weightKg: 0, dimensionVol: 0 });
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
        if (String(formData.pickupStopId) === String(formData.dropoffStopId)) {
            setError('Điểm nhận và điểm trả hàng phải khác nhau.');
            return;
        }

        if (!initialData?.cargoTicketId && draftDetails.length === 0) {
            setError('Vui lòng thêm ít nhất một chi tiết hàng hóa.');
            return;
        }

        const payload = buildCargoTicketRequest(formData, draftDetails);
        if (initialData?.cargoTicketId) {
            delete payload.details;
        }
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
                        <Col md={4}><Dropdown label="Điểm nhận" name="pickupStopId" value={formData.pickupStopId} onChange={handleChange} loading={optionsLoading} options={pickupStopOptions} optionValue="stopPointId" renderOption={(item) => item.stopPointName} required /></Col>
                        <Col md={4}><Dropdown label="Điểm trả" name="dropoffStopId" value={formData.dropoffStopId} onChange={handleChange} loading={optionsLoading} options={dropoffStopOptions} optionValue="stopPointId" renderOption={(item) => item.stopPointName} required /></Col>
                        <Col md={8}><Dropdown label="Chuyến đi phù hợp" name="tripId" value={formData.tripId ?? ''} onChange={handleChange} loading={optionsLoading} options={eligibleTrips} optionValue="tripId" renderOption={tripLabel} disabled={!formData.pickupStopId || !formData.dropoffStopId} /></Col>
                        <Col md={4}>
                            <Form.Group><Form.Label className="fw-semibold">Trạng thái *</Form.Label><Form.Select name="status" value={formData.status} onChange={handleChange} required>
                                <option value="RECEIVED">Đã nhận hàng</option><option value="LOADED">Đã xếp hàng</option>
                                <option value="ARRIVED">Đã đến nơi</option><option value="DELIVERED">Đã giao</option>
                                <option value="CANCELLED">Đã hủy</option><option value="REJECTED">Từ chối</option><option value="ABANDONED">Bỏ hàng</option>
                            </Form.Select></Form.Group>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            <Card className="shadow-sm border-0 mb-4">
                <Card.Header className="bg-white py-3"><h5 className="fw-bold mb-0">Thanh toán và xử lý</h5></Card.Header>
                <Card.Body className="p-4">
                    <Row className="g-3">
                        <Col md={4}><Field label="Tiền thu hộ COD (VNĐ)" name="codAmount" type="number" value={formData.codAmount} onChange={handleChange} required min="0" /></Col>
                        <Col md={4}><Form.Group><Form.Label className="fw-semibold">Người trả phí *</Form.Label><Form.Select name="feePayer" value={formData.feePayer} onChange={handleChange}><option value="SENDER">Người gửi</option><option value="RECEIVER">Người nhận</option></Form.Select></Form.Group></Col>
                        <Col md={4}><Form.Group><Form.Label className="fw-semibold">Phương thức thanh toán *</Form.Label><Form.Select name="paymentMethod" value={formData.paymentMethod} onChange={handleChange} required><option value="CASH">Tiền mặt</option><option value="BANK_TRANSFER">Chuyển khoản</option></Form.Select></Form.Group></Col>
                        <Col xs={12}><Form.Group><Form.Label className="fw-semibold">Mô tả hàng hóa</Form.Label><Form.Control as="textarea" rows={3} name="description" value={formData.description || ''} onChange={handleChange} /></Form.Group></Col>
                    </Row>
                </Card.Body>
            </Card>

            <CargoTicketDetailSection
                draftDetails={draftDetails}
                onAdd={handleAddDetail}
                onChange={handleDetailChange}
                onRemove={handleRemoveDetail}
            />

            <Button type="submit" disabled={submitting || optionsLoading || Boolean(optionsError)} className="px-4 py-2 d-flex align-items-center gap-2 custom-btn-general">
                <BsCheckCircle />{submitting ? 'Đang lưu...' : submitLabel}
            </Button>
        </Form>
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

const tripLabel = (trip) => {
    return `Chuyến đi ${trip.tripId}`;
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
            cargoTypePriceId: Number(d.cargoTypePriceId),
            description: optionalText(d.description),
            quantity: Number(d.quantity),
            weightKg: Number(d.weightKg),
            dimensionVol: Number(d.dimensionVol)

        }))
    };
}
