import { FiX } from 'react-icons/fi';
import { formatCargoDateTime } from '../utils/cargoLookupFormatters';

/** Displays the assigned trip timeline from pickup through drop-off. */
export default function CargoRouteModal({ order, onClose }) {
    return (
        <div className="cargo-route-modal" role="dialog" aria-modal="true" aria-labelledby="cargo-route-title">
            <section className="cargo-route-card">
                <header>
                    <div>
                        <h2 id="cargo-route-title">Điểm đón trả</h2>
                        <p>{order.routeName || `${order.pickupStop.city} - ${order.dropoffStop.city}`}</p>
                    </div>
                    <button type="button" onClick={onClose} aria-label="Đóng lộ trình"><FiX /></button>
                </header>

                {order.routeStops.length > 0 ? (
                    <ol className="cargo-route-timeline">
                        {order.routeStops.map((stop) => (
                            <li
                                key={`${stop.stopPointId}-${stop.stopOrder}`}
                                className={stop.stopPointId === order.pickupStop.stopPointId
                                    || stop.stopPointId === order.dropoffStop.stopPointId ? 'is-terminal' : ''}
                            >
                                <time>{stop.estimatedTime ? new Date(stop.estimatedTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : '--:--'}</time>
                                <div><strong>{stop.name}</strong><span>{stop.address}, {stop.city}</span></div>
                            </li>
                        ))}
                    </ol>
                ) : (
                    <p className="cargo-route-empty">Chuyến xe chưa có lộ trình để hiển thị. Thời gian dự kiến: {formatCargoDateTime(order.departureTime)}.</p>
                )}

                <footer><button type="button" onClick={onClose}>Đóng</button></footer>
            </section>
        </div>
    );
}
