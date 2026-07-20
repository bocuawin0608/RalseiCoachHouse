import { useEffect, useState } from 'react';
import { Button, Modal } from 'react-bootstrap';
import { BsCheckCircleFill } from 'react-icons/bs';
import { formatCurrency } from '../../../utils/formatters';
import { cargoTicketApi } from '../api/cargoTicketApi';

export default function QrPaymentModal({ ticket, onClose, onSuccess }) {
    const [isSuccess, setIsSuccess] = useState(false);

    useEffect(() => {
        if (!ticket) return;

        let timeoutId;
        let cancelled = false;

        const checkOnce = async () => {
            try {
                const response = await cargoTicketApi.getCargoTicket(ticket.cargoTicketId);
                if (cancelled) return;
                if (response?.payment?.status === 'COMPLETED') {
                    setIsSuccess(true);
                    timeoutId = setTimeout(() => {
                        if (onSuccess) onSuccess();
                        onClose();
                    }, 2500);
                }
            } catch (error) {
                console.error('Failed to check payment status', error);
            }
        };

        checkOnce();
        const intervalId = setInterval(checkOnce, 3000);

        return () => {
            cancelled = true;
            clearInterval(intervalId);
            if (timeoutId) clearTimeout(timeoutId);
        };
    }, [ticket, onClose, onSuccess]);

    if (!ticket) return null;

    const qrSrc = ticket.qrUrl
        || (ticket.payment?.status === 'PENDING' && ticket.payment?.paymentMethod === 'BANK_TRANSFER'
            ? null
            : ticket.qrUrl);

    return (
        <Modal show onHide={onClose} centered backdrop={isSuccess ? 'static' : true}>
            <Modal.Header closeButton={!isSuccess}>
                <Modal.Title>Thanh toán chuyển khoản</Modal.Title>
            </Modal.Header>
            <Modal.Body className="text-center p-4">
                {isSuccess ? (
                    <div className="py-4">
                        <BsCheckCircleFill className="text-success mb-3" size={60} />
                        <h4 className="fw-bold text-success mb-2">Thanh toán thành công!</h4>
                        <p className="text-muted">Giao dịch đã được xác nhận bởi SePay.</p>
                    </div>
                ) : (
                    <>
                        <p className="fw-semibold mb-1">Đơn: {ticket.ticketCode}</p>
                        <p className="mb-1">Số tiền: <strong>{formatCurrency(ticket.totalPrice)}</strong></p>
                        <p className="mb-3 text-muted small">Mã giao dịch: {ticket.payment?.transactionId}</p>
                        {qrSrc ? (
                            <img
                                src={qrSrc}
                                alt="SePay QR Code"
                                style={{ maxWidth: '100%', width: '350px', borderRadius: '8px' }}
                            />
                        ) : (
                            <p className="text-danger small">Không có mã QR. Tải lại đơn hàng và thử lại.</p>
                        )}
                        <p className="mt-3 text-muted small">Quét mã QR để thanh toán qua ngân hàng đã cấu hình SePay</p>
                        <div className="d-flex align-items-center justify-content-center mt-3">
                            <div className="spinner-border spinner-border-sm text-primary" role="status"></div>
                            <span className="ms-2 text-primary small fw-medium">Đang chờ thanh toán...</span>
                        </div>
                    </>
                )}
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={onClose} disabled={isSuccess}>Đóng</Button>
            </Modal.Footer>
        </Modal>
    );
}
