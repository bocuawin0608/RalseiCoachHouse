import { useEffect, useState } from 'react';
import { Button, Form, Modal, Spinner } from 'react-bootstrap';
import { RecaptchaVerifier, signInWithPhoneNumber, signOut } from 'firebase/auth';
import { auth } from '../../auth';

const formatPhoneForFirebase = (phone) => {
    if (phone.startsWith('0')) {
        return `+84${phone.slice(1)}`;
    }
    return phone;
};

let bookingRecaptchaVerifier = null;

const getBookingRecaptchaVerifier = () => {
    if (!bookingRecaptchaVerifier) {
        bookingRecaptchaVerifier = new RecaptchaVerifier(auth, 'booking-recaptcha-container', {
            size: 'invisible',
        });
    }
    return bookingRecaptchaVerifier;
};

const resetBookingRecaptchaVerifier = () => {
    if (bookingRecaptchaVerifier) {
        bookingRecaptchaVerifier.clear();
        bookingRecaptchaVerifier = null;
    }
};

export default function PhoneOtpModal({ phone, show, onVerified, onClose }) {
    const [step, setStep] = useState(1);
    const [otp, setOtp] = useState('');
    const [confirmationResult, setConfirmationResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!show || !phone) {
            return;
        }

        setStep(1);
        setOtp('');
        setConfirmationResult(null);
        setError('');
        setLoading(true);

        const sendOtp = async () => {
            try {
                const verifier = getBookingRecaptchaVerifier();
                const formattedPhone = formatPhoneForFirebase(phone);
                const confirmResult = await signInWithPhoneNumber(auth, formattedPhone, verifier);
                setConfirmationResult(confirmResult);
                setStep(2);
            } catch (err) {
                console.error(err);
                resetBookingRecaptchaVerifier();
                setError('Không thể gửi mã OTP. Vui lòng thử lại!');
            } finally {
                setLoading(false);
            }
        };

        sendOtp();
    }, [show, phone]);

    const handleVerifyOtp = async (event) => {
        event.preventDefault();
        if (!confirmationResult) return;

        setError('');
        setLoading(true);

        try {
            const userCredential = await confirmationResult.confirm(otp);
            const idToken = await userCredential.user.getIdToken();
            await signOut(auth);
            onVerified({ idToken });
            setOtp('');
            setStep(1);
            setConfirmationResult(null);
        } catch (err) {
            console.error(err);
            setError('Mã OTP không chính xác. Vui lòng thử lại!');
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        setOtp('');
        setError('');
        setStep(1);
        setConfirmationResult(null);
        onClose();
    };

    return (
        <>
            <div id="booking-recaptcha-container" />
            <Modal show={show} onHide={handleClose} centered backdrop="static">
                <Modal.Header closeButton>
                    <Modal.Title className="fw-bold" style={{ fontSize: '1rem' }}>
                        Xác thực số điện thoại
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p className="text-muted mb-3" style={{ fontSize: '0.9rem' }}>
                        Số điện thoại <strong>{phone}</strong> chưa từng đặt vé trên hệ thống.
                        Vui lòng xác thực OTP để tiếp tục.
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
                    ) : null}
                </Modal.Body>
            </Modal>
        </>
    );
}
