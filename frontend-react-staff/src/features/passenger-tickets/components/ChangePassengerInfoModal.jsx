import { Alert, Button, Form, Modal } from 'react-bootstrap';
import { useEffect, useState } from 'react';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';

const PHONE_PATTERN = /^0(3|5|7|8|9)[0-9]{8}$/;
const EMAIL_PATTERN = /^[\w.+-]+@[\w.-]+\.[A-Za-z]{2,}$/;
const FULL_NAME_PATTERN = /^[\p{L}][\p{L}\s'.\-]{1,99}$/u;

function buildInitialForm(seat) {
    const hasChild = Boolean(seat?.childFullname);
    return {
        fullName: seat?.fullName || '',
        phone: seat?.phone || '',
        email: seat?.email || '',
        hasChild,
        childFullname: seat?.childFullname || '',
        childBirthYear: seat?.childBirthYear ? String(seat.childBirthYear) : '',
    };
}

function validateForm(form) {
    const errors = {};

    if (!form.fullName.trim()) {
        errors.fullName = 'Vui lòng nhập họ tên.';
    } else if (!FULL_NAME_PATTERN.test(form.fullName.trim())) {
        errors.fullName = 'Họ tên không hợp lệ.';
    }

    if (!form.phone.trim()) {
        errors.phone = 'Vui lòng nhập số điện thoại.';
    } else if (!PHONE_PATTERN.test(form.phone.trim())) {
        errors.phone = 'Số điện thoại không hợp lệ.';
    }

    if (!form.email.trim()) {
        errors.email = 'Vui lòng nhập email.';
    } else if (!EMAIL_PATTERN.test(form.email.trim())) {
        errors.email = 'Email không hợp lệ.';
    }

    if (form.hasChild) {
        if (!form.childFullname.trim()) {
            errors.childFullname = 'Vui lòng nhập tên bé.';
        } else if (!FULL_NAME_PATTERN.test(form.childFullname.trim())) {
            errors.childFullname = 'Họ tên bé không hợp lệ.';
        }

        const birthYear = Number(form.childBirthYear);
        const currentYear = new Date().getFullYear();
        if (!form.childBirthYear) {
            errors.childBirthYear = 'Vui lòng nhập năm sinh của bé.';
        } else if (!Number.isInteger(birthYear) || birthYear < 2010 || birthYear > currentYear) {
            errors.childBirthYear = `Năm sinh phải từ 2010 đến ${currentYear}.`;
        }
    }

    return errors;
}

function buildPayload(form, seat) {
    const payload = {
        fullName: form.fullName.trim(),
        phone: form.phone.trim(),
        email: form.email.trim(),
    };

    const hadChild = Boolean(seat?.childFullname);

    if (form.hasChild) {
        payload.accompaniedChild = {
            fullname: form.childFullname.trim(),
            birthYear: Number(form.childBirthYear),
        };
    } else if (hadChild) {
        payload.removeAccompaniedChild = true;
    }

    return payload;
}

function hasChanges(form, seat) {
    if (!seat) return false;

    const payload = buildPayload(form, seat);
    const samePassenger = payload.fullName === seat.fullName
        && payload.phone === seat.phone
        && payload.email === (seat.email || '');

    if (!samePassenger) return true;

    if (payload.removeAccompaniedChild) return true;

    if (payload.accompaniedChild) {
        return payload.accompaniedChild.fullname !== (seat.childFullname || '')
            || payload.accompaniedChild.birthYear !== seat.childBirthYear;
    }

    return false;
}

export default function ChangePassengerInfoModal({
    isOpen,
    ticketCode,
    seat,
    onClose,
    onSuccess,
}) {
    const [form, setForm] = useState(buildInitialForm(seat));
    const [fieldErrors, setFieldErrors] = useState({});
    const [error, setError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (isOpen && seat) {
            setForm(buildInitialForm(seat));
            setFieldErrors({});
            setError(null);
        }
    }, [isOpen, seat]);

    const handleChange = (field, value) => {
        setForm((prev) => ({ ...prev, [field]: value }));
        setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
        setError(null);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        const validationErrors = validateForm(form);
        if (Object.keys(validationErrors).length > 0) {
            setFieldErrors(validationErrors);
            return;
        }

        if (!hasChanges(form, seat)) {
            onClose();
            return;
        }

        setIsSubmitting(true);
        setError(null);

        try {
            const updatedTicket = await staffPassengerTicketApi.changePassengerInfo(
                ticketCode,
                seat.ticketDetailId,
                buildPayload(form, seat)
            );
            onSuccess?.(updatedTicket);
            onClose();
        } catch (requestError) {
            setError(requestError.response?.data?.message || 'Không thể cập nhật thông tin hành khách.');
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen || !seat) return null;

    return (
        <Modal show={isOpen} onHide={onClose} centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Sửa thông tin hành khách — ghế {seat.seatCode}
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4 pb-0">
                <Form id="change-passenger-form" onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">
                            Họ tên <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="text"
                            value={form.fullName}
                            onChange={(e) => handleChange('fullName', e.target.value)}
                            required
                            maxLength={100}
                            className="py-2"
                            isInvalid={Boolean(fieldErrors.fullName)}
                        />
                        <Form.Control.Feedback type="invalid">{fieldErrors.fullName}</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">
                            Số điện thoại <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="tel"
                            value={form.phone}
                            onChange={(e) => handleChange('phone', e.target.value)}
                            required
                            maxLength={11}
                            className="py-2"
                            isInvalid={Boolean(fieldErrors.phone)}
                        />
                        <Form.Control.Feedback type="invalid">{fieldErrors.phone}</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label className="fw-semibold text-secondary">
                            Email <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="email"
                            value={form.email}
                            onChange={(e) => handleChange('email', e.target.value)}
                            required
                            maxLength={120}
                            className="py-2"
                            isInvalid={Boolean(fieldErrors.email)}
                        />
                        <Form.Control.Feedback type="invalid">{fieldErrors.email}</Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Check
                            type="switch"
                            id="has-child-switch"
                            label="Có trẻ em đi kèm"
                            checked={form.hasChild}
                            onChange={(e) => handleChange('hasChild', e.target.checked)}
                        />
                    </Form.Group>

                    {form.hasChild && (
                        <div className="p-3 bg-light border rounded mb-3">
                            <Form.Group className="mb-3">
                                <Form.Label className="fw-semibold text-secondary">
                                    Tên bé <span className="text-danger">*</span>
                                </Form.Label>
                                <Form.Control
                                    type="text"
                                    value={form.childFullname}
                                    onChange={(e) => handleChange('childFullname', e.target.value)}
                                    maxLength={100}
                                    className="py-2"
                                    isInvalid={Boolean(fieldErrors.childFullname)}
                                />
                                <Form.Control.Feedback type="invalid">{fieldErrors.childFullname}</Form.Control.Feedback>
                            </Form.Group>

                            <Form.Group className="mb-0">
                                <Form.Label className="fw-semibold text-secondary">
                                    Năm sinh <span className="text-danger">*</span>
                                </Form.Label>
                                <Form.Control
                                    type="number"
                                    min={2010}
                                    max={new Date().getFullYear()}
                                    value={form.childBirthYear}
                                    onChange={(e) => handleChange('childBirthYear', e.target.value)}
                                    className="py-2"
                                    isInvalid={Boolean(fieldErrors.childBirthYear)}
                                />
                                <Form.Control.Feedback type="invalid">{fieldErrors.childBirthYear}</Form.Control.Feedback>
                            </Form.Group>
                        </div>
                    )}

                    {error && (
                        <Alert variant="danger" className="mb-3 py-2 px-3 border-0 d-flex align-items-center gap-2">
                            <BsExclamationTriangleFill />
                            <span>{error}</span>
                        </Alert>
                    )}
                </Form>
            </Modal.Body>

            <Modal.Footer className="bg-light border-0 rounded-bottom">
                <Button variant="outline-secondary" onClick={onClose} disabled={isSubmitting} className="px-4">
                    Hủy bỏ
                </Button>
                <Button
                    type="submit"
                    form="change-passenger-form"
                    disabled={isSubmitting}
                    className="px-4 custom-btn-general"
                >
                    {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
