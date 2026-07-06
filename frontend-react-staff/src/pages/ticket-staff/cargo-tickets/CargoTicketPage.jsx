import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Container } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import Pagination from '../../../components/common/Pagination';
import { cargoTicketApi } from '../../../features/cargoTickets/api/cargoTicketApi';
import CargoTicketFilter from '../../../features/cargoTickets/components/CargoTicketFilter';
import CargoTicketTable from '../../../features/cargoTickets/components/CargoTicketTable';
import CargoTicketUpdateModal from '../../../features/cargoTickets/components/CargoTicketUpdateModal';
import { useCargoTickets } from '../../../features/cargoTickets/hooks/useCargoTickets';

export default function CargoTicketPage() {
    const navigate = useNavigate();
    const [selectedTicket, setSelectedTicket] = useState(null);
    const { tickets, loading, error, filters, pageInfo, setPageInfo, handleFilterChange, handleReset, refetch } = useCargoTickets();

    const handleDisable = async (ticket) => {
        if (!window.confirm(`Bạn có chắc muốn vô hiệu hóa vé ${ticket.ticketCode}?`)) return;
        try {
            await cargoTicketApi.disableCargoTicket(ticket.cargoTicketId);
            await refetch();
        } catch (err) {
            window.alert(err.response?.data?.message || 'Vô hiệu hóa vé hàng hóa thất bại.');
        }
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1400px' }}>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="m-0 fw-bold text-dark">Quản lý vé hàng hóa</h2>
                <Button className="fw-medium shadow-sm custom-btn-general" onClick={() => navigate('/staff/cargo-tickets/create')}>
                    + Thêm vé hàng hóa
                </Button>
            </div>

            <CargoTicketFilter filters={filters} onFilterChange={handleFilterChange} onReset={handleReset} />
            {error && <Alert variant="danger" className="d-flex align-items-center gap-2 border-0"><BsExclamationTriangleFill />{error}</Alert>}

            <Card className="shadow-sm border-0">
                <Card.Body className="p-0">
                    <CargoTicketTable data={tickets} loading={loading} onEdit={setSelectedTicket} onDisable={handleDisable} />
                    <div className="d-flex justify-content-center py-3 border-top"><Pagination pageInfo={pageInfo} onPageChange={setPageInfo} /></div>
                </Card.Body>
            </Card>

            {selectedTicket && <CargoTicketUpdateModal key={selectedTicket.cargoTicketId} data={selectedTicket} onClose={() => setSelectedTicket(null)} onSuccess={refetch} />}
        </Container>
    );
}
