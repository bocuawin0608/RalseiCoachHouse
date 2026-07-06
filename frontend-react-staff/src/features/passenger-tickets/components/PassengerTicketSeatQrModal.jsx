import { Modal, Spinner } from 'react-bootstrap';

/**
 * Renders as a sibling of the ticket detail modal (not inside its body)
 * so Bootstrap can stack overlays correctly without resizing the parent dialog.
 */
export default function PassengerTicketSeatQrModal({ preview, onClose }) {
    return (
        <Modal
            show={Boolean(preview)}
            onHide={onClose}
            centered
            size="sm"
            enforceFocus={false}
            restoreFocus={false}
        >
            <Modal.Header closeButton>
                <Modal.Title className="fs-6">
                    QR ghế {preview?.seatCode}
                </Modal.Title>
            </Modal.Header>
            <Modal.Body className="text-center py-4">
                {preview?.loading && (
                    <div className="py-4">
                        <Spinner animation="border" size="sm" className="me-2" />
                        Đang tải mã QR...
                    </div>
                )}
                {!preview?.loading && preview?.error && (
                    <div className="text-danger small">{preview.error}</div>
                )}
                {!preview?.loading && preview?.url && (
                    <>
                        <img
                            src={preview.url}
                            alt={`QR ghế ${preview.seatCode}`}
                            width={240}
                            height={240}
                            className="mb-3"
                        />
                        <div className="fw-semibold">{preview.fullName}</div>
                        <div className="text-muted small">Ghế {preview.seatCode}</div>
                    </>
                )}
            </Modal.Body>
        </Modal>
    );
}
