import { Alert, Button, Container } from 'react-bootstrap';
import { BsArrowLeft } from 'react-icons/bs';
import { useLocation, useNavigate } from 'react-router-dom';
import CargoTicketForm from '../../../features/cargoTickets/components/CargoTicketForm';
import { cargoTicketApi } from '../../../features/cargoTickets/api/cargoTicketApi';

export default function CargoTicketCreatePage() {
    const navigate = useNavigate();
    const { state } = useLocation();
    const selectedTrip = state?.trip;
    const handleSubmit = async (payload) => {
        await cargoTicketApi.createCargoTicket(payload);
        navigate('/staff/cargo-tickets/send');
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            <Button variant="link" className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2" onClick={() => navigate('/staff/cargo-tickets/send')}>
                <BsArrowLeft /> Quay lại danh sách chuyến
            </Button>
            <h2 className="mb-4 fw-bold text-dark">Thêm đơn gửi hàng</h2>
            {!selectedTrip ? (
                <Alert variant="warning">Bạn phải chọn một chuyến xe hợp lệ tại văn phòng của mình trước khi lập đơn.</Alert>
            ) : (
                <CargoTicketForm
                    initialData={{ tripId: selectedTrip.tripId, pickupStopId: selectedTrip.pickupStopId }}
                    lockedTrip={selectedTrip}
                    onSubmit={handleSubmit}
                />
            )}
        </Container>
    );
}
