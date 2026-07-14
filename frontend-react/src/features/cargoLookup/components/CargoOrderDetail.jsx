import { FiArrowLeft, FiMapPin } from 'react-icons/fi';
import {
    formatCargoCurrency,
    formatCargoDateTime,
    formatCargoStatus,
    formatFeePayer,
} from '../utils/cargoLookupFormatters';

/** Read-only detail view for one selected cargo order. */
export default function CargoOrderDetail({ order, onBack, onOpenRoute, onDisable }) {
    return (
        <section className="cargo-detail-shell">
            <header className="cargo-detail-heading">
                <button type="button" onClick={onBack}><FiArrowLeft /> Quay lại</button>
                <div><span>CHI TIẾT ĐƠN HÀNG</span><h1>{order.ticketCode}</h1></div>
                <div className="d-flex flex-column align-items-end gap-2">
                    <strong className={`cargo-status cargo-status--${order.status?.toLowerCase()}`}>{formatCargoStatus(order.status)}</strong>
                    {order.status === 'RECEIVED' && onDisable && (
                        <button type="button" className="btn btn-sm btn-outline-danger fw-semibold" onClick={() => onDisable(order)}>
                            Hủy đơn hàng
                        </button>
                    )}
                </div>
            </header>

            <div className="cargo-detail-grid">
                <article className="cargo-detail-panel cargo-detail-panel--wide">
                    <h2>Thông tin vận chuyển</h2>
                    <button type="button" className="cargo-detail-route" onClick={onOpenRoute}>
                        <FiMapPin /> {order.routeName || `${order.pickupStop.city} - ${order.dropoffStop.city}`}
                        <span>Xem lộ trình</span>
                    </button>
                    <dl>
                        <div><dt>Điểm nhận hàng</dt><dd>{order.pickupStop.name}<small>{order.pickupStop.address}, {order.pickupStop.city}</small></dd></div>
                        <div><dt>Điểm trả hàng</dt><dd>{order.dropoffStop.name}<small>{order.dropoffStop.address}, {order.dropoffStop.city}</small></dd></div>
                        <div><dt>Thời gian xuất bến</dt><dd>{formatCargoDateTime(order.departureTime)}</dd></div>
                        <div><dt>Biển số xe</dt><dd>{order.licensePlate || 'Chưa phân xe'}</dd></div>
                        <div><dt>Tài xế</dt><dd>{order.driverName || 'Chưa phân công'}</dd></div>
                        <div><dt>Điểm tiếp nhận</dt><dd>{order.pickupStop.name || 'Chưa cập nhật'}</dd></div>
                    </dl>
                </article>

                <article className="cargo-detail-panel">
                    <h2>Thông tin người gửi</h2>
                    <dl><div><dt>Họ và tên</dt><dd>{order.sender.name}</dd></div><div><dt>Số điện thoại</dt><dd>{order.sender.phone}</dd></div></dl>
                </article>

                <article className="cargo-detail-panel">
                    <h2>Thông tin người nhận</h2>
                    <dl><div><dt>Họ và tên</dt><dd>{order.receiver.name}</dd></div><div><dt>Số điện thoại</dt><dd>{order.receiver.phone}</dd></div></dl>
                </article>

                <article className="cargo-detail-panel cargo-detail-panel--wide">
                    <h2>Thông tin hàng hóa</h2>
                    <div className="cargo-item-list">
                        {order.items.map((item) => (
                            <section key={item.cargoTicketDetailId}>
                                <div><strong>{item.cargoTypeName}</strong><b>{formatCargoCurrency(item.calculatedPrice)}</b></div>
                                <p>{item.description || 'Không có mô tả'}</p>
                                <ul>
                                    <li>Số lượng: {item.quantity} {item.unit || ''}</li>
                                    <li>Khối lượng: {item.weightKg} kg</li>
                                    <li>Thể tích: {item.dimensionVol} m³</li>
                                </ul>
                            </section>
                        ))}
                    </div>
                </article>

                <article className="cargo-detail-panel cargo-detail-panel--wide">
                    <h2>Chi phí và trạng thái</h2>
                    <dl>
                        <div><dt>Tổng phí vận chuyển</dt><dd className="cargo-detail-price">{formatCargoCurrency(order.totalPrice)}</dd></div>
                        <div><dt>Tiền thu hộ (COD)</dt><dd>{formatCargoCurrency(order.codAmount)}</dd></div>
                        <div><dt>Người trả phí</dt><dd>{formatFeePayer(order.feePayer)}</dd></div>
                        <div><dt>Trạng thái</dt><dd>{formatCargoStatus(order.status)}</dd></div>
                        <div><dt>Ngày tạo đơn</dt><dd>{formatCargoDateTime(order.bookedAt)}</dd></div>
                    </dl>
                </article>
            </div>
        </section>
    );
}
