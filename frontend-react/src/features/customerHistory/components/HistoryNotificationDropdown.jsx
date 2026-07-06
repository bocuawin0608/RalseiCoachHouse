import { FiClock, FiMapPin } from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';
import { useCustomerHistory } from '../hooks/useCustomerHistory';
import { formatCustomerDateTime, formatCustomerTicketStatus } from '../utils/customerHistoryFormatters';

/**
 * Bell dropdown showing recent real bookings and linking directly to their details.
 */
export default function HistoryNotificationDropdown({ onClose }) {
    const navigate = useNavigate();
    const { data, loading, error } = useCustomerHistory();
    const recentBookings = data.slice(0, 5);

    /** Navigates to an exact transaction and closes the floating panel. */
    const openDetail = (ticketCode) => {
        onClose();
        navigate(`/history/detail/${ticketCode}`);
    };

    /** Opens the complete history screen from the dropdown footer. */
    const openHistory = () => {
        onClose();
        navigate('/history');
    };

    return (
        <section className="history-notifications" aria-label="Lịch sử chuyến đi gần đây">
            <header className="history-notifications__header">
                <strong>Chuyến đi gần đây</strong>
                <button type="button" onClick={openHistory}>Xem tất cả</button>
            </header>

            <div className="history-notifications__body">
                {loading && <p className="history-notifications__state">Đang tải...</p>}
                {!loading && error && <p className="history-notifications__state history-notifications__state--error">{error}</p>}
                {!loading && !error && recentBookings.length === 0 && (
                    <p className="history-notifications__state">Bạn chưa có chuyến đi nào.</p>
                )}

                {!loading && !error && recentBookings.map((booking) => (
                    <button
                        className="history-notification"
                        type="button"
                        key={booking.passengerTicketId}
                        onClick={() => openDetail(booking.ticketCode)}
                    >
                        <span className="history-notification__topline">
                            <strong>{booking.ticketCode}</strong>
                            <small>{formatCustomerTicketStatus(booking.status)}</small>
                        </span>
                        <span className="history-notification__route">
                            <FiMapPin aria-hidden="true" /> {booking.routeName}
                        </span>
                        <span className="history-notification__time">
                            <FiClock aria-hidden="true" /> {formatCustomerDateTime(booking.departureTime)}
                        </span>
                    </button>
                ))}
            </div>
        </section>
    );
}
