import { useEffect, useState } from 'react';
import { FiArrowRight, FiBox, FiClock, FiMapPin } from 'react-icons/fi';
import { cargoLookupApi } from '../api/cargoLookupApi';
import { formatCargoCurrency, formatCargoDateTime, formatCargoStatus } from '../utils/cargoLookupFormatters';
import CargoOrderDetail from './CargoOrderDetail';
import CargoRouteModal from './CargoRouteModal';
import '../styles/cargoLookup.css';

/** Displays cargo history owned by the authenticated customer account. */
export default function CargoLookupPage() {
    const [state, setState] = useState({ orders: [], loading: true, error: '' });
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [routeOrder, setRouteOrder] = useState(null);

    useEffect(() => {
        let active = true;
        cargoLookupApi.getHistory()
            .then((orders) => { if (active) setState({ orders, loading: false, error: '' }); })
            .catch(() => { if (active) setState({ orders: [], loading: false, error: 'Không thể tải lịch sử đơn hàng.' }); });
        return () => { active = false; };
    }, []);

    if (selectedOrder) return (
        <main className="cargo-history-page">
            <CargoOrderDetail order={selectedOrder} onBack={() => setSelectedOrder(null)} onOpenRoute={() => setRouteOrder(selectedOrder)} />
            {routeOrder && <CargoRouteModal order={routeOrder} onClose={() => setRouteOrder(null)} />}
        </main>
    );

    return (
        <main className="cargo-history-page">
            <section className="cargo-history-shell">
                <header className="cargo-history-heading"><FiBox /><div><span>LỊCH SỬ DỊCH VỤ</span><h1>Đơn hàng của bạn</h1><p>Chọn một đơn hàng để xem chi tiết vận chuyển.</p></div></header>
                {state.loading && <div className="cargo-history-state">Đang tải lịch sử...</div>}
                {state.error && <div className="cargo-history-state cargo-history-state--error">{state.error}</div>}
                {!state.loading && !state.error && state.orders.length === 0 && <div className="cargo-history-state">Bạn chưa có đơn hàng nào.</div>}
                <div className="cargo-history-list">
                    {state.orders.map((order) => (
                        <button type="button" className="cargo-history-card" key={order.cargoTicketId} onClick={() => setSelectedOrder(order)}>
                            <span>Mã đơn {order.ticketCode}</span><strong>{formatCargoStatus(order.status)}</strong>
                            <h2>{order.routeName}</h2>
                            <p><FiMapPin /> {order.pickupStop.name} <FiArrowRight /> {order.dropoffStop.name}</p>
                            <footer><span><FiClock /> {formatCargoDateTime(order.departureTime)}</span><b>{formatCargoCurrency(order.totalPrice)}</b></footer>
                        </button>
                    ))}
                </div>
            </section>
        </main>
    );
}
