import { useEffect, useState } from 'react';
import { Alert, Button, Form, Modal } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import {
    formatCurrency,
    formatRefundMethod,
    getTransactionIdConfig,
    validateTransactionId,
} from '../utils/refundFormatters';

export default function RefundConfirmPaymentModal({
    isOpen,
    refund,
    isSubmitting,
    error,
    onClose,
    onConfirm,
}) {
    const [transactionId, setTransactionId] = useState('');
    const [validationError, setValidationError] = useState(null);

    const config = refund ? getTransactionIdConfig(refund.refundMethod) : null;

    useEffect(() => {
        if (isOpen) {
            setTransactionId('');
            setValidationError(null);
        }
    }, [isOpen, refund?.refundId]);

    if (!isOpen || !refund) return null;

    const handleSubmit = (event) => {
        event.preventDefault();
        const nextError = validateTransactionId(refund.refundMethod, transactionId);
        if (nextError) {
            setValidationError(nextError);
            return;
        }

        onConfirm({
            transactionId: transactionId.trim() || undefined,
        });
    };

    return (
        <Modal show={isOpen} onHide={onClose} centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="text-success">Xác nhận đã thanh toán</Modal.Title>
            </Modal.Header>

            <Form id="refund-confirm-form" onSubmit={handleSubmit}>
                <Modal.Body>
                    <Alert variant="warning" className="d-flex align-items-start gap-2 py-2 px-3 border-0">
                        <BsExclamationTriangleFill className="mt-1 flex-shrink-0" />
                        <span>
                            Bạn có chắc chắn đã chuyển khoản/thanh toán hoàn tiền thành công cho khách hàng?
                            Hành động này không thể hoàn tác.
                        </span>
                    </Alert>

                    <div className="mb-3 p-3 bg-light border rounded">
                        <div className="text-muted small">Mã vé</div>
                        <div className="fw-semibold">{refund.ticketCode}</div>
                        <div className="text-muted small mt-2">Số tiền hoàn</div>
                        <div className="fw-bold text-primary">{formatCurrency(refund.amount)}</div>
                        <div className="text-muted small mt-2">Phương thức</div>
                        <div>{formatRefundMethod(refund.refundMethod)}</div>
                    </div>

                    <Form.Group>
                        <Form.Label className="fw-semibold text-secondary">
                            {config.label}
                            {config.required && <span className="text-danger"> *</span>}
                        </Form.Label>
                        <Form.Control
                            type="text"
                            value={transactionId}
                            onChange={(event) => {
                                setTransactionId(event.target.value);
                                setValidationError(null);
                            }}
                            placeholder={config.placeholder}
                            maxLength={config.maxLength || 100}
                            required={config.required}
                        />
                    </Form.Group>

                    {(validationError || error) && (
                        <Alert variant="danger" className="mt-3 mb-0 py-2 px-3">
                            {validationError || error}
                        </Alert>
                    )}
                </Modal.Body>

                <Modal.Footer>
                    <Button variant="outline-secondary" onClick={onClose} disabled={isSubmitting}>
                        Hủy
                    </Button>
                    <Button type="submit" form="refund-confirm-form" variant="success" disabled={isSubmitting}>
                        {isSubmitting ? 'Đang xử lý…' : 'Xác nhận'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
