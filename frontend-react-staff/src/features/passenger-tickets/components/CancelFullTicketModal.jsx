import { Alert, Button, Form, Modal } from 'react-bootstrap';
import { useEffect, useMemo, useRef, useState } from 'react';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import { staffPassengerTicketApi } from '../api/staffPassengerTicketApi';
import { calculateStaffRefundPreview, formatCurrency } from '../utils/passengerTicketFormatters';
import { resolveTicketContactPhone } from '../utils/ticketContactPhone';
import CustomerPhoneOtpModal from './CustomerPhoneOtpModal';

const EMPTY_FORM = {
    bankName: '',
    accountHolder: '',
    accountNumber: '',
    reason: '',
};

export default function CancelFullTicketModal({ isOpen, ticket, onClose, onSuccess }) {
    const [form, setForm] = useState(EMPTY_FORM);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);
    const [otpOpen, setOtpOpen] = useState(false);
    const pendingCancelRef = useRef(null);

    const refundPreview = ticket
        ? calculateStaffRefundPreview(ticket.paymentAmount, ticket.refundTierLabel)
        : null;
    const contactPhone = useMemo(() => resolveTicketContactPhone(ticket), [ticket]);

    useEffect(() => {
        if (isOpen) {
            setForm(EMPTY_FORM);
            setError(null);
            setOtpOpen(false);
            pendingCancelRef.current = null;
        }
    }, [isOpen, ticket?.ticketCode]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((current) => ({ ...current, [name]: value }));
        setError(null);
    };

    const submitCancelWithOtp = async (firebaseIdToken) => {
        if (!ticket || !pendingCancelRef.current) return;

        setIsSubmitting(true);
        setError(null);
        try {
            const updatedTicket = await staffPassengerTicketApi.cancelFull(ticket.ticketCode, {
                firebaseIdToken,
                ...pendingCancelRef.current,
            });
            pendingCancelRef.current = null;
            setOtpOpen(false);
            onSuccess?.(updatedTicket);
            onClose();
        } catch (requestError) {
            setError(requestError.response?.data?.message || 'Không thể hủy vé.');
            setOtpOpen(false);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        if (!ticket) return;

        if (!contactPhone) {
            setError('Vé không có số điện thoại hành khách để xác thực OTP.');
            return;
        }

        pendingCancelRef.current = {
            bankName: form.bankName.trim(),
            accountHolder: form.accountHolder.trim(),
            accountNumber: form.accountNumber.trim(),
            reason: form.reason.trim() || undefined,
        };
        setError(null);
        setOtpOpen(true);
    };

    if (!isOpen || !ticket) return null;

    return (
        <>
            <Modal show={isOpen} onHide={onClose} centered backdrop="static">
                <Modal.Header closeButton>
                    <Modal.Title className="fs-5 fw-bold text-danger">
                        Hủy vé {ticket.ticketCode}
                    </Modal.Title>
                </Modal.Header>

                <Modal.Body className="px-4 pb-0">
                    <Alert variant="warning" className="py-2 px-3 border-0">
                        Vé sẽ chuyển sang trạng thái <strong>Đã hủy</strong> và ghế được trả lại hệ thống.
                        Yêu cầu hoàn tiền sẽ được ghi nhận theo chính sách của nhà xe.
                        Thao tác cần OTP xác nhận từ số <strong>{contactPhone || '—'}</strong>.
                    </Alert>

                    <div className="mb-3 p-3 bg-light border rounded">
                        <div className="text-muted small">Mức hoàn theo chính sách</div>
                        <div className="fw-semibold">{ticket.refundTierLabel || '—'}</div>
                        <div className="text-muted small mt-2">Số tiền hoàn dự kiến</div>
                        <div className="fw-bold fs-5 text-primary">
                            {refundPreview != null ? formatCurrency(refundPreview) : 'Không được hủy'}
                        </div>
                    </div>

                    <Form id="cancel-full-ticket-form" onSubmit={handleSubmit}>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-semibold text-secondary">
                                Tên ngân hàng <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Control
                                type="text"
                                name="bankName"
                                value={form.bankName}
                                onChange={handleChange}
                                required
                                maxLength={100}
                                className="py-2"
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label className="fw-semibold text-secondary">
                                Tên chủ tài khoản <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Control
                                type="text"
                                name="accountHolder"
                                value={form.accountHolder}
                                onChange={handleChange}
                                required
                                maxLength={150}
                                className="py-2"
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label className="fw-semibold text-secondary">
                                Số tài khoản <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Control
                                type="text"
                                name="accountNumber"
                                value={form.accountNumber}
                                onChange={handleChange}
                                required
                                inputMode="numeric"
                                pattern="[0-9]{6,30}"
                                maxLength={30}
                                className="py-2"
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label className="fw-semibold text-secondary">Lý do (tuỳ chọn)</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={2}
                                name="reason"
                                value={form.reason}
                                onChange={handleChange}
                                maxLength={500}
                                className="py-2"
                            />
                        </Form.Group>

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
                        Đóng
                    </Button>
                    <Button
                        type="submit"
                        form="cancel-full-ticket-form"
                        variant="danger"
                        disabled={isSubmitting || refundPreview == null}
                        className="px-4"
                    >
                        {isSubmitting ? 'Đang xử lý...' : 'Tiếp tục — xác nhận OTP'}
                    </Button>
                </Modal.Footer>
            </Modal>

            <CustomerPhoneOtpModal
                show={otpOpen}
                phone={contactPhone}
                title="Xác nhận OTP trước khi hủy vé"
                description={(
                    <>
                        Nhờ khách hàng cung cấp OTP gửi tới số <strong>{contactPhone}</strong> để đồng ý
                        hủy vé.
                    </>
                )}
                onClose={() => {
                    if (!isSubmitting) {
                        setOtpOpen(false);
                        pendingCancelRef.current = null;
                    }
                }}
                onVerified={({ idToken }) => submitCancelWithOtp(idToken)}
            />
        </>
    );
}
