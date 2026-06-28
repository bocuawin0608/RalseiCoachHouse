import { Button, Container } from 'react-bootstrap';
import { BsArrowLeft } from 'react-icons/bs';
import { useNavigate } from 'react-router-dom';
import CargoTicketForm from '../../../features/cargoTickets/components/CargoTicketForm';
import { cargoTicketApi } from '../../../features/cargoTickets/api/cargoTicketApi';

export default function CargoTicketCreatePage() {
    const navigate = useNavigate();
    const handleSubmit = async (payload) => {
        await cargoTicketApi.createCargoTicket(payload);
        navigate('/staff/ticket/cargo-tickets');
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            <Button variant="link" className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2" onClick={() => navigate('/staff/ticket/cargo-tickets')}>
                <BsArrowLeft /> Quay lại danh sách
            </Button>
            <h2 className="mb-4 fw-bold text-dark">Thêm vé hàng hóa</h2>
            <CargoTicketForm onSubmit={handleSubmit} />
        </Container>
    );
}
