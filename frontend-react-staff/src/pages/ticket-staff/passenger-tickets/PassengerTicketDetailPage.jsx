import { Alert, Button, Container } from 'react-bootstrap';
import { useState } from 'react';
import { BsArrowLeft } from 'react-icons/bs';
import { useNavigate, useParams } from 'react-router-dom';
import {
    ChangePassengerInfoModal,
    PassengerTicketDetailPanel,
    PassengerTicketSeatQrModal,
    usePassengerTicketDetail,
    usePassengerTicketSeatQr,
} from '../../../features/passenger-tickets';

export default function PassengerTicketDetailPage() {
    const { ticketCode } = useParams();
    const navigate = useNavigate();
    const { data, loading, error, applyDetail } = usePassengerTicketDetail(ticketCode);
    const { qrPreview, showQr, closeQr } = usePassengerTicketSeatQr(ticketCode);
    const [editSeat, setEditSeat] = useState(null);

    const handleBack = () => {
        closeQr();
        setEditSeat(null);

        // Browser history already holds the search URL with filters from before detail.
        if (window.history.length > 1) {
            navigate(-1);
            return;
        }

        navigate('/staff/passenger-tickets/search');
    };

    const handlePassengerInfoUpdated = (updatedTicket) => {
        applyDetail(updatedTicket);
    };

    return (
        <Container fluid className="py-2" style={{ maxWidth: '1400px' }}>
            <div className="d-flex flex-wrap align-items-center gap-3 mb-4">
                <Button
                    variant="outline-secondary"
                    className="d-flex align-items-center gap-2"
                    onClick={handleBack}
                >
                    <BsArrowLeft />
                    Quay lại danh sách
                </Button>
                <div>
                    <h2 className="fw-bold text-dark mb-1">
                        {ticketCode ? `Chi tiết vé ${ticketCode}` : 'Chi tiết vé'}
                    </h2>
                    <p className="text-muted mb-0">Xem và cập nhật thông tin vé hành khách.</p>
                </div>
            </div>

            {loading && (
                <div className="py-5 text-center text-muted">Đang tải chi tiết vé...</div>
            )}
            {!loading && error && (
                <Alert variant="danger">{error}</Alert>
            )}
            {!loading && !error && data && (
                <PassengerTicketDetailPanel
                    ticket={data}
                    onShowQr={showQr}
                    onEditPassenger={setEditSeat}
                    activeQrDetailId={qrPreview?.ticketDetailId}
                    qrLoading={Boolean(qrPreview?.loading)}
                />
            )}

            <PassengerTicketSeatQrModal preview={qrPreview} onClose={closeQr} />

            <ChangePassengerInfoModal
                isOpen={Boolean(editSeat)}
                ticketCode={ticketCode}
                seat={editSeat}
                onClose={() => setEditSeat(null)}
                onSuccess={handlePassengerInfoUpdated}
            />
        </Container>
    );
}
