import { Button, Modal } from 'react-bootstrap';
import { formatCurrency } from '../../../utils/formatters';

export default function QrPaymentModal({ ticket, onClose }) {
    if (!ticket) return null;

    return (
        <Modal show onHide={onClose} centered>
            <Modal.Header closeButton>
                <Modal.Title>Thanh toán chuyển khoản</Modal.Title>
            </Modal.Header>
            <Modal.Body className="text-center p-4">
                <p className="fw-semibold mb-1">Vé: {ticket.ticketCode}</p>
                <p className="mb-1">Số tiền: <strong>{formatCurrency(ticket.totalPrice)}</strong></p>
                <p className="mb-3 text-muted small">Mã giao dịch: {ticket.payment?.transactionId}</p>
                <img
                    src={ticket.qrUrl}
                    alt="SePay QR Code"
                    style={{ maxWidth: '100%', width: '350px', borderRadius: '8px' }}
                />
                <p className="mt-3 text-muted small">Quét mã QR để thanh toán qua Vietcombank</p>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={onClose}>Đóng</Button>
            </Modal.Footer>
        </Modal>
    );
}
