import { useNavigate } from 'react-router-dom';
import { BsBoxSeam, BsClipboardCheck } from 'react-icons/bs';
import '../../../features/cargoTickets/styles/CargoOperations.css';

/** Entry screen that separates order creation from destination verification. */
export default function CargoTicketPage() {
    const navigate = useNavigate();

    return (
        <main className="cargo-operations-page">
            <header className="cargo-page-heading">
                <p className="cargo-eyebrow">Vận hành hàng hóa</p>
                <h1>Quản lý đơn hàng</h1>
                <p>Chọn đúng nghiệp vụ để xem chuyến xe, người chịu trách nhiệm và trạng thái kiện hàng.</p>
            </header>

            <section className="cargo-feature-grid" aria-label="Chức năng quản lý đơn hàng">
                <button type="button" className="cargo-feature-card" onClick={() => navigate('/staff/cargo-tickets/send')}>
                    <span className="cargo-feature-icon"><BsBoxSeam /></span>
                    <span><strong>Gửi hàng</strong><small>Chọn chuyến xe còn chỗ và lập đơn mới</small></span>
                    <span className="cargo-feature-arrow">→</span>
                </button>
                <button type="button" className="cargo-feature-card" onClick={() => navigate('/staff/cargo-tickets/check')}>
                    <span className="cargo-feature-icon"><BsClipboardCheck /></span>
                    <span><strong>Kiểm tra hàng</strong><small>Xem hàng đã đến và xác nhận người nhận</small></span>
                    <span className="cargo-feature-arrow">→</span>
                </button>
            </section>
        </main>
    );
}
