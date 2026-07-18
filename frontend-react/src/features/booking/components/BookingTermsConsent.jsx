import { useState } from 'react';
import { Form, Collapse } from 'react-bootstrap';
import { BOOKING_TERMS_SECTIONS } from '../constants/bookingTermsContent';

/**
 * Light terms consent for Step 2: required checkbox + collapsible summary.
 */
export default function BookingTermsConsent({ register, error }) {
    const [expanded, setExpanded] = useState(false);

    return (
        <div className="mt-2 mb-3">
            <Form.Check
                type="checkbox"
                id="booking-accept-terms"
                className="fw-medium"
                style={{ fontSize: '0.85rem' }}
                isInvalid={Boolean(error)}
                {...register('acceptTerms', {
                    validate: (value) => value === true || 'Vui lòng đồng ý điều khoản trước khi chuyển sang bước thanh toán.',
                })}
                label={
                    <span style={{ color: 'var(--ralsei-black)' }}>
                        Tôi đã đọc và đồng ý với điều khoản đặt vé{' '}
                        <button
                            type="button"
                            className="btn btn-link p-0 align-baseline fw-semibold text-decoration-underline"
                            style={{ fontSize: '0.85rem', color: 'var(--ralsei-black)' }}
                            onClick={(event) => {
                                event.preventDefault();
                                event.stopPropagation();
                                setExpanded((prev) => !prev);
                            }}
                        >
                            (Click vào để xem chi tiết).
                        </button>
                    </span>
                }
            />
            {error && (
                <div className="text-danger mt-1" style={{ fontSize: '0.8rem' }}>
                    {error.message}
                </div>
            )}

            <Collapse in={expanded}>
                <div>
                    <div
                        className="mt-2 p-3 rounded-3"
                        style={{
                            maxHeight: '220px',
                            overflowY: 'auto',
                            backgroundColor: '#f7f8f7',
                            border: '1px solid #eceee9',
                            fontSize: '0.78rem',
                            lineHeight: 1.55,
                            color: '#5a635e',
                        }}
                    >
                        {BOOKING_TERMS_SECTIONS.map((section) => (
                            <div key={section.title} className="mb-2">
                                <div className="fw-semibold mb-1" style={{ color: 'var(--ralsei-black)' }}>
                                    {section.title}
                                </div>
                                <ul className="mb-0 ps-3">
                                    {section.items.map((item) => (
                                        <li key={item} className="mb-1">{item}</li>
                                    ))}
                                </ul>
                            </div>
                        ))}
                    </div>
                </div>
            </Collapse>
        </div>
    );
}
