import { Button } from 'react-bootstrap';
import { BsArrowLeft } from 'react-icons/bs';
import { useNavigate } from 'react-router-dom';
import CargoQueuePanel from '../../../features/cargoTickets/components/CargoQueuePanel';
import '../../../features/cargoTickets/styles/CargoOperations.css';

/** Destination workflow: arrived cargo is read-only except for receipt confirmation. */
export default function CargoCheckPage() {
    const navigate = useNavigate();
    return <main className="cargo-operations-page">
        <div className="cargo-toolbar"><Button variant="link" className="cargo-back" onClick={() => navigate('/staff/cargo-tickets')}><BsArrowLeft /> Đơn hàng</Button></div>
        <header className="cargo-page-heading compact"><p className="cargo-eyebrow">Kiểm tra hàng</p><h1>Đơn hàng đã đến</h1><p>Đối chiếu người nhận, chuyến xe và người chịu trách nhiệm trước khi xác nhận bàn giao.</p></header>
        <CargoQueuePanel status="ARRIVED" confirmable />
    </main>;
}
