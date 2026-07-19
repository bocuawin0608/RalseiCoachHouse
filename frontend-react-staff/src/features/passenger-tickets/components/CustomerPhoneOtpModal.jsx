import { useEffect, useState } from 'react';
import { Button, Form, Modal, Spinner } from 'react-bootstrap';
import { RecaptchaVerifier, signInWithPhoneNumber, signOut } from 'firebase/auth';
import { auth } from '../../../config/firebase';

const RECAPTCHA_HOST_ID = 'staff-ticket-recaptcha-host';

const formatPhoneForFirebase = (phone) => {
    if (phone.startsWith('0')) {
        return `+84${phone.slice(1)}`;
    }
    return phone;
};

let staffRecaptchaVerifier = null;
let sendGeneration = 0;

const resetStaffRecaptchaVerifier = () => {
    if (staffRecaptchaVerifier) {
        try {
            staffRecaptchaVerifier.clear();
        } catch {
            // Widget may already be detached from DOM.
        }
        staffRecaptchaVerifier = null;
    }
    const host = document.getElementById(RECAPTCHA_HOST_ID);
    if (host) {
        host.replaceChildren();
    }
};

/**
 * Fresh DOM node each time — grecaptcha refuses to re-render the same element id.
 */
const createStaffRecaptchaVerifier = () => {
    resetStaffRecaptchaVerifier();
    const host = document.getElementById(RECAPTCHA_HOST_ID);
    if (!host) {
        throw new Error('Missing reCAPTCHA host element.');
    }
    const widget = document.createElement('div');
    widget.id = `staff-ticket-recaptcha-${Date.now()}`;
    host.appendChild(widget);
    staffRecaptchaVerifier = new RecaptchaVerifier(auth, widget.id, {
        size: 'invisible',
    });
    return staffRecaptchaVerifier;
};

/**
 * Firebase phone OTP consent before staff đổi vé / hủy vé.
 * OTP is sent to the ticket contact phone (first CONFIRMED passenger).
 */
export default function CustomerPhoneOtpModal({
    phone,
    show,
    title = 'Xác nhận OTP khách hàng',
    description,
    onVerified,
    onClose,
}) {
    const [step, setStep] = useState(1);
    const [otp, setOtp] = useState('');
    const [confirmationResult, setConfirmationResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [sendNonce, setSendNonce] = useState(0);

    useEffect(() => {
        if (!show || !phone) {
            // Invalidate in-flight sends; do NOT clear DOM here (race with grecaptcha).
            sendGeneration += 1;
            return undefined;
        }

        const myGeneration = ++sendGeneration;

        setStep(1);
        setOtp('');
        setConfirmationResult(null);
        setError('');
        setLoading(true);

        const sendOtp = async () => {
            try {
                const verifier = createStaffRecaptchaVerifier();
                const formattedPhone = formatPhoneForFirebase(phone);
                const confirmResult = await signInWithPhoneNumber(auth, formattedPhone, verifier);
                if (myGeneration !== sendGeneration) {
                    return;
                }
                setConfirmationResult(confirmResult);
                setStep(2);
            } catch (err) {
                console.error(err);
                if (myGeneration === sendGeneration) {
                    resetStaffRecaptchaVerifier();
                    setError('Không thể gửi mã OTP. Vui lòng thử lại!');
                }
            } finally {
                if (myGeneration === sendGeneration) {
                    setLoading(false);
                }
            }
        };

        void sendOtp();
        return undefined;
    }, [show, phone, sendNonce]);

    const isUpdatingTicket = step === 3;

    const handleVerifyOtp = async (event) => {
        event.preventDefault();
        if (!confirmationResult || isUpdatingTicket) return;

        setError('');
        setLoading(true);
        let otpAccepted = false;

        try {
            const userCredential = await confirmationResult.confirm(otp);
            const idToken = await userCredential.user.getIdToken();
            await signOut(auth);
            otpAccepted = true;
            resetStaffRecaptchaVerifier();
            setOtp('');
            setConfirmationResult(null);
            setStep(3);
            setLoading(false);
            await onVerified?.({ idToken, phone });
        } catch (err) {
            console.error(err);
            if (otpAccepted) {
                return;
            }
            setError('Mã OTP không chính xác. Vui lòng thử lại!');
            setLoading(false);
        }
    };

    const handleClose = () => {
        if (loading || isUpdatingTicket) return;
        sendGeneration += 1;
        resetStaffRecaptchaVerifier();
        setOtp('');
        setError('');
        setStep(1);
        setConfirmationResult(null);
        onClose();
    };

    const handleRetrySend = () => {
        if (loading || isUpdatingTicket) return;
        setSendNonce((value) => value + 1);
    };

    return (
        <>
            <div id={RECAPTCHA_HOST_ID} />
            <Modal
                show={show}
                onHide={handleClose}
                centered
                backdrop="static"
                keyboard={!loading && !isUpdatingTicket}
            >
                <Modal.Header closeButton={!isUpdatingTicket && !loading}>
                    <Modal.Title className="fw-bold" style={{ fontSize: '1rem' }}>
                        {isUpdatingTicket ? 'Đang cập nhật vé' : title}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {isUpdatingTicket ? (
                        <div className="text-center py-3">
                            <Spinner animation="border" size="sm" className="mb-3" />
                            <p className="text-muted mb-0" style={{ fontSize: '0.9rem' }}>
                                Hệ thống đang cập nhật vé, vui lòng không đóng cửa sổ này
                            </p>
                        </div>
                    ) : (
                        <>
                            <p className="text-muted mb-3" style={{ fontSize: '0.9rem' }}>
                                {description || (
                                    <>
                                        Để thao tác trên vé, khách hàng phải xác nhận OTP gửi tới số{' '}
                                        <strong>{phone}</strong>.
                                    </>
                                )}
                            </p>

                            {error && (
                                <div className="alert alert-danger py-2 px-3 mb-3" style={{ fontSize: '0.85rem' }}>
                                    {error}
                                </div>
                            )}

                            {loading && step === 1 ? (
                                <div className="text-center py-3">
                                    <Spinner animation="border" size="sm" />
                                    <span className="ms-2 text-muted">Đang gửi mã OTP...</span>
                                </div>
                            ) : step === 2 ? (
                                <Form onSubmit={handleVerifyOtp}>
                                    <Form.Group>
                                        <Form.Label className="fw-medium text-muted" style={{ fontSize: '0.85rem' }}>
                                            Nhập mã OTP
                                        </Form.Label>
                                        <Form.Control
                                            type="text"
                                            inputMode="numeric"
                                            value={otp}
                                            onChange={(event) => setOtp(event.target.value)}
                                            placeholder="Nhập 6 số"
                                            className="rounded-3 shadow-none"
                                            autoFocus
                                            disabled={loading}
                                        />
                                    </Form.Group>
                                    <div className="d-flex justify-content-end gap-2 mt-3">
                                        <Button variant="outline-secondary" onClick={handleClose} disabled={loading}>
                                            Hủy
                                        </Button>
                                        <Button type="submit" disabled={loading || !otp.trim()}>
                                            {loading ? 'Đang xác thực...' : 'Xác nhận OTP'}
                                        </Button>
                                    </div>
                                </Form>
                            ) : error ? (
                                <div className="d-flex justify-content-end gap-2">
                                    <Button variant="outline-secondary" onClick={handleClose}>
                                        Đóng
                                    </Button>
                                    <Button onClick={handleRetrySend}>Gửi lại OTP</Button>
                                </div>
                            ) : null}
                        </>
                    )}
                </Modal.Body>
            </Modal>
        </>
    );
}
