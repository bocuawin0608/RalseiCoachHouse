import { Alert, Button, Container } from 'react-bootstrap';
import { useState } from 'react';
import { BsArrowLeft } from 'react-icons/bs';
import { useNavigate, useParams } from 'react-router-dom';
import {
    CancelFullTicketModal,
    ChangeTicketSessionModal,
    PassengerTicketDetailPanel,
    usePassengerTicketDetail,
} from '../../../features/passenger-tickets';

export default function PassengerTicketDetailPage() {
    const { ticketCode } = useParams();
    const navigate = useNavigate();
    const { data, loading, error, applyDetail, refetch } = usePassengerTicketDetail(ticketCode);
    const [changeTicketOpen, setChangeTicketOpen] = useState(false);
    const [cancelOpen, setCancelOpen] = useState(false);

    const handleBack = () => {
        setChangeTicketOpen(false);
        setCancelOpen(false);

        if (window.history.length > 1) {
            navigate(-1);
            return;
        }

        navigate('/staff/passenger-tickets/search');
    };

    const handleDetailUpdated = async (updatedTicket) => {
        applyDetail(updatedTicket);
        await refetch();
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
                    onChangeTicket={() => setChangeTicketOpen(true)}
                    onCancelTicket={() => setCancelOpen(true)}
                    suppressCancel={changeTicketOpen}
                />
            )}

            <ChangeTicketSessionModal
                isOpen={changeTicketOpen}
                ticket={data}
                onClose={() => setChangeTicketOpen(false)}
                onSuccess={handleDetailUpdated}
            />

            <CancelFullTicketModal
                isOpen={cancelOpen}
                ticket={data}
                onClose={() => setCancelOpen(false)}
                onSuccess={handleDetailUpdated}
            />
        </Container>
    );
}
