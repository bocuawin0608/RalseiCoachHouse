import { Form } from 'react-bootstrap';
import { getChildBirthYearMax, getChildBirthYearMin } from '../utils/passengerChangeForm';

/** Passenger + accompanied-child fields used by the change-ticket session. */
export default function PassengerInfoFields({ form, fieldErrors, onChange, fieldIdPrefix = 'seat' }) {
    return (
        <>
            <Form.Group className="mb-3">
                <Form.Label className="fw-semibold text-secondary">
                    Họ tên <span className="text-danger">*</span>
                </Form.Label>
                <Form.Control
                    type="text"
                    value={form.fullName}
                    onChange={(e) => onChange('fullName', e.target.value)}
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
                    onChange={(e) => onChange('phone', e.target.value)}
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
                    onChange={(e) => onChange('email', e.target.value)}
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
                    id={`has-child-switch-${fieldIdPrefix}`}
                    label="Có trẻ em đi kèm"
                    checked={form.hasChild}
                    onChange={(e) => onChange('hasChild', e.target.checked)}
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
                            onChange={(e) => onChange('childFullname', e.target.value)}
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
                            min={getChildBirthYearMin()}
                            max={getChildBirthYearMax()}
                            value={form.childBirthYear}
                            onChange={(e) => onChange('childBirthYear', e.target.value)}
                            className="py-2"
                            isInvalid={Boolean(fieldErrors.childBirthYear)}
                        />
                        <Form.Control.Feedback type="invalid">{fieldErrors.childBirthYear}</Form.Control.Feedback>
                    </Form.Group>
                </div>
            )}
        </>
    );
}
