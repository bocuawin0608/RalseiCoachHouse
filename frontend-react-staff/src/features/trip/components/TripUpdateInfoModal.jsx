import { Alert, Button, Form, Modal, Spinner } from 'react-bootstrap';
import { useEffect, useMemo, useState } from 'react';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import { tripApi } from '../api/tripApi';
import './TripUpdateInfoModal.css';

const normalizeTime = (value) => {
    if (!value) return '';
    if (Array.isArray(value)) return `${String(value[0]).padStart(2, '0')}:${String(value[1]).padStart(2, '0')}`;
    return String(value).substring(0, 5);
};

/** Complete, conflict-aware editor for every mutable field shown in the trip view. */
export default function TripUpdateInfoModal({ isOpen, data, routes, onClose, onSuccess }) {
    const [formData, setFormData] = useState({ routeId: '', coachId: '', driverId: '', attendantId: '', departureDate: '', departureTime: '', status: '' });
    const [resources, setResources] = useState({ coaches: [], drivers: [], attendants: [] });
    const [isLoading, setIsLoading] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const today = new Date().toLocaleDateString('en-CA');
    const isIncident = data?.coachStatus === 'HAVE_INCIDENT';

    useEffect(() => {
        if (!data || !isOpen) return;
        // The modal keeps an editable draft that must be reset for each selected trip.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setFormData({
            routeId: String(data.routeId || ''), coachId: isIncident ? '' : String(data.coachId || ''),
            driverId: isIncident ? '' : String(data.driverId || ''), attendantId: String(data.attendantId || ''),
            departureDate: String(data.departureDate || '').substring(0, 10),
            departureTime: normalizeTime(data.departureTime), status: data.tripStatus || 'SCHEDULED'
        });
        setError(null);
    }, [data, isOpen, isIncident]);

    useEffect(() => {
        if (!isOpen || !data || !formData.routeId) return;
        if (!isIncident && (!formData.departureDate || !formData.departureTime)) return;
        // Clear stale options immediately while the conflict-aware lookup is running.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setIsLoading(true);
        setResources({ coaches: [], drivers: [], attendants: [] });
        const request = isIncident
            ? Promise.all([
                tripApi.getIncidentReplacementCoaches(data.tripId, formData.routeId),
                tripApi.getIncidentReplacementDrivers(data.tripId)
            ]).then(([coaches, drivers]) => ({ coaches, drivers, attendants: [] }))
            : (() => {
                const departureTime = `${formData.departureDate}T${formData.departureTime}:00`;
                return Promise.all([
                    tripApi.getAvailableCoaches({ routeId: formData.routeId, departureTime, excludeTripId: data.tripId }),
                    tripApi.getAvailableDrivers({ departureTime, excludeTripId: data.tripId }),
                    tripApi.getAvailableAttendants({ departureTime, excludeTripId: data.tripId })
                ]).then(([coaches, drivers, attendants]) => ({ coaches, drivers, attendants }));
            })();
        request.then(setResources)
            .catch((err) => {
                setResources({ coaches: [], drivers: [], attendants: [] });
                setError(err.response?.data?.message || err.response?.data || 'Không thể tải xe và tài xế đang rảnh.');
            })
            .finally(() => setIsLoading(false));
    }, [isOpen, data, isIncident, formData.routeId, formData.departureDate, formData.departureTime]);

    const isPast = useMemo(() => {
        if (isIncident) return false;
        if (!formData.departureDate || !formData.departureTime) return true;
        const selectedMinute = new Date(`${formData.departureDate}T${formData.departureTime}:00`);
        const currentMinute = new Date();
        currentMinute.setSeconds(0, 0);
        return selectedMinute < currentMinute;
    }, [formData.departureDate, formData.departureTime, isIncident]);

    const handleChange = ({ target: { name, value } }) => {
        if (name === 'departureDate' && value && value < today) {
            setFormData((previous) => ({ ...previous, departureDate: today }));
            setError('Không thể chọn ngày trong quá khứ.');
            return;
        }
        setFormData((previous) => ({ ...previous, [name]: value }));
        setError(null);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (isPast) return setError('Thời gian khởi hành phải ở tương lai.');
        setIsSubmitting(true);
        try {
            if (isIncident) {
                await tripApi.replaceIncidentCoach(data.tripId, {
                    routeId: Number(formData.routeId),
                    coachId: Number(formData.coachId),
                    driverId: Number(formData.driverId)
                });
            } else {
                await tripApi.updateTrip(data.tripId, {
                    routeId: Number(formData.routeId), coachId: Number(formData.coachId),
                    driverId: Number(formData.driverId), attendantId: Number(formData.attendantId),
                    departureTime: `${formData.departureDate}T${formData.departureTime}:00`, status: formData.status
                });
            }
            await onSuccess();
            onClose();
        } catch (err) {
            setError(err.response?.data?.message || err.response?.data || 'Có lỗi xảy ra khi cập nhật.');
        } finally { setIsSubmitting(false); }
    };

    const renderOptions = (items) => items.map((item) => (
        <option key={item.id} value={item.id}>{item.displayName}{item.secondaryText ? ` — ${item.secondaryText}` : ''}</option>
    ));

    if (!isOpen) return null;
    return (
        <Modal show onHide={onClose} centered size={isIncident ? undefined : 'lg'} backdrop="static">
            <Modal.Header closeButton><Modal.Title className="fs-5 fw-bold">{isIncident ? `Điều xe thay thế · Chuyến #${data?.tripId}` : `Cập nhật chuyến #${data?.tripId}`}</Modal.Title></Modal.Header>
            <Modal.Body>
                {isIncident && <Alert variant="danger" className="trip-replacement-notice"><BsExclamationTriangleFill /><span><strong>Xe {data?.licensePlate} không thể tiếp tục.</strong> Chuyến sẽ khởi hành lại ngay khi lưu; toàn bộ hành khách và hàng hóa hiện tại được giữ nguyên, và chuyến không mở bán vé mới.</span></Alert>}
                <Form id="update-trip-form" onSubmit={handleSubmit} className={`trip-update-grid${isIncident ? ' is-replacement' : ''}`}>
                    <Form.Group><Form.Label>Tuyến đường *</Form.Label><Form.Select name="routeId" value={formData.routeId} onChange={handleChange} required>{routes.map((route) => <option key={route.routeId} value={route.routeId}>{route.routeName}</option>)}</Form.Select></Form.Group>
                    {!isIncident && <Form.Group><Form.Label>Trạng thái chuyến *</Form.Label><Form.Select name="status" value={formData.status} onChange={handleChange} required><option value="SCHEDULED">Đã lên lịch</option><option value="IN_PROGRESS">Đang hoạt động</option><option value="COMPLETED">Hoàn thành</option></Form.Select></Form.Group>}
                    {!isIncident && <Form.Group><Form.Label>Ngày khởi hành *</Form.Label><Form.Control type="date" min={today} name="departureDate" value={formData.departureDate} onChange={handleChange} required /></Form.Group>}
                    {!isIncident && <Form.Group><Form.Label>Giờ khởi hành *</Form.Label><Form.Control type="time" step="300" min={formData.departureDate === today ? new Date().toTimeString().slice(0, 5) : undefined} name="departureTime" value={formData.departureTime} onChange={handleChange} required /></Form.Group>}
                    <Form.Group><Form.Label>Xe khách đang rảnh *</Form.Label><Form.Select name="coachId" value={formData.coachId} onChange={handleChange} disabled={isLoading} required><option value="">Chọn xe khách</option>{renderOptions(resources.coaches)}</Form.Select></Form.Group>
                    <Form.Group><Form.Label>Tài xế đang rảnh *</Form.Label><Form.Select name="driverId" value={formData.driverId} onChange={handleChange} disabled={isLoading} required><option value="">Chọn tài xế</option>{renderOptions(resources.drivers)}</Form.Select></Form.Group>
                    {!isIncident && <Form.Group><Form.Label>Phụ xe đang rảnh *</Form.Label><Form.Select name="attendantId" value={formData.attendantId} onChange={handleChange} disabled={isLoading} required><option value="">Chọn phụ xe</option>{renderOptions(resources.attendants)}</Form.Select></Form.Group>}
                    {isLoading && <div className="trip-update-loading"><Spinner size="sm" /> Đang kiểm tra lịch xe và tài xế…</div>}
                    {error && <Alert variant="danger" className="trip-update-alert"><BsExclamationTriangleFill /> {error}</Alert>}
                </Form>
            </Modal.Body>
            <Modal.Footer className="trip-modal-footer"><Button variant="outline-secondary" onClick={onClose}>Hủy bỏ</Button><Button type="submit" form="update-trip-form" disabled={isSubmitting || isLoading || isPast || !formData.routeId || !formData.coachId || !formData.driverId} className="custom-btn-general">{isSubmitting ? 'Đang điều xe…' : 'Lưu thay đổi'}</Button></Modal.Footer>
        </Modal>
    );
}
