import { Alert, Modal } from 'react-bootstrap';
import { usePassengerTicketDetail } from '../hooks/usePassengerTicketDetail';
import { usePassengerTicketSeatQr } from '../hooks/usePassengerTicketSeatQr';
import PassengerTicketDetailPanel from './PassengerTicketDetailPanel';
import PassengerTicketSeatQrModal from './PassengerTicketSeatQrModal';

export default function PassengerTicketDetailModal({ ticketCode, onClose }) {
    const isOpen = Boolean(ticketCode);
    const { data, loading, error } = usePassengerTicketDetail(isOpen ? ticketCode : null);
    const { qrPreview, showQr, closeQr } = usePassengerTicketSeatQr(isOpen ? ticketCode : null);

    const handleCloseDetail = () => {
        closeQr();
        onClose();
    };

    return (
        <>
            <Modal
                show={isOpen}
                onHide={handleCloseDetail}
                size="xl"
                scrollable
                centered
                backdrop="static"
                enforceFocus={!qrPreview}
            >
                <Modal.Header closeButton>
                    <Modal.Title>
                        {ticketCode ? `Chi tiết vé ${ticketCode}` : 'Chi tiết vé'}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body className="p-4">
                    {loading && (
                        <div className="py-5 text-center text-muted">Đang tải chi tiết vé...</div>
                    )}
                    {!loading && error && (
                        <Alert variant="danger" className="mb-0">{error}</Alert>
                    )}
                    {!loading && !error && data && (
                        <PassengerTicketDetailPanel
                            ticket={data}
                            onShowQr={showQr}
                            activeQrDetailId={qrPreview?.ticketDetailId}
                            qrLoading={Boolean(qrPreview?.loading)}
                        />
                    )}
                </Modal.Body>
            </Modal>

            <PassengerTicketSeatQrModal preview={qrPreview} onClose={closeQr} />
        </>
    );
}
