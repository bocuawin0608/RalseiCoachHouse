import { useState } from 'react';
import { FiChevronRight, FiClock, FiMapPin } from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';
import { useCustomerHistory } from '../hooks/useCustomerHistory';
import {
    formatCustomerCurrency,
    formatCustomerDateTime,
    formatCustomerTicketStatus,
} from '../utils/customerHistoryFormatters';
import '../styles/customerHistory.css';
import TicketCancellationModal from './TicketCancellationModal';

const CANCELLATION_CUTOFF_MILLISECONDS = 5 * 60 * 60 * 1000;

/**
 * Full customer service-history page backed entirely by authenticated API data.
 */
export default function CustomerHistoryPage() {
    const navigate = useNavigate();
    const { data: bookings, loading, error, updateBooking } = useCustomerHistory();
    const [cancellationBooking, setCancellationBooking] = useState(null);
    const [pageOpenedAt] = useState(() => Date.now());

    /** Opens a booking through its public, non-sequential ticket code. */
    const openBooking = (ticketCode) => navigate(`/history/detail/${ticketCode}`);

    /** Provides keyboard activation for each accessible booking card. */
    const handleCardKeyDown = (event, ticketCode) => {
        if (event.target !== event.currentTarget) return;
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            openBooking(ticketCode);
        }
    };

    /** A ticket remains cancellable only until five hours before departure. */
    const canCancel = (booking) => booking.status === 'CONFIRMED'
        && booking.departureTime
        && new Date(booking.departureTime).getTime() > pageOpenedAt + CANCELLATION_CUTOFF_MILLISECONDS;

    /** Pending tickets can resume payment while the hold countdown is still active. */
    const canPayNow = (booking) => booking.status === 'PENDING'
        && Boolean(booking.transactionId)
        && (!booking.paymentExpiresAt || new Date(booking.paymentExpiresAt).getTime() > Date.now());

    /** Opens the refund form without triggering navigation on the parent card. */
    const openCancellation = (event, booking) => {
        event.stopPropagation();
        setCancellationBooking(booking);
    };

    /** Continues an unpaid booking on the existing payment page. */
    const openPayment = (event, booking) => {
        event.stopPropagation();
        navigate(`/booking/payment/${encodeURIComponent(booking.transactionId)}`);
    };

    /** Reflects the committed cancellation immediately and closes the modal. */
    const handleCancelled = (result) => {
        updateBooking(result.ticketCode, { status: result.ticketStatus });
        setCancellationBooking(null);
    };

    return (
        <main className="customer-history-page">
            <section className="customer-history-shell">
                <header className="customer-history-heading">
                    <span>LỊCH SỬ DỊCH VỤ</span>
                    <h1>Chuyến đi của bạn</h1>
                    <p>Chọn một giao dịch để xem thông tin vé và mã QR lên xe.</p>
                </header>

                <div className="customer-history-list" aria-live="polite">
                    {loading && <div className="customer-history-state">Đang tải lịch sử...</div>}
                    {!loading && error && <div className="customer-history-state customer-history-state--error">{error}</div>}
                    {!loading && !error && bookings.length === 0 && (
                        <div className="customer-history-state">Bạn chưa có chuyến đi nào.</div>
                    )}

                    {!loading && !error && bookings.map((booking) => (
                        <article
                            className="customer-history-card"
                            key={booking.passengerTicketId}
                            role="button"
                            tabIndex="0"
                            onClick={() => openBooking(booking.ticketCode)}
                            onKeyDown={(event) => handleCardKeyDown(event, booking.ticketCode)}
                        >
                            <div className="customer-history-card__topline">
                                <span>Mã vé {booking.ticketCode}</span>
                                <strong className={`ticket-status ticket-status--${booking.status?.toLowerCase()}`}>
                                    {formatCustomerTicketStatus(booking.status)}
                                </strong>
                            </div>
                            <h2>{booking.routeName}</h2>
                            <div className="customer-history-card__route">
                                <FiMapPin aria-hidden="true" />
                                <span>{booking.pickupStopName}</span>
                                <FiChevronRight aria-hidden="true" />
                                <span>{booking.dropoffStopName}</span>
                            </div>
                            <footer className="customer-history-card__footer">
                                <span><FiClock aria-hidden="true" /> {formatCustomerDateTime(booking.departureTime)}</span>
                                <span>{booking.seats.length} ghế · {booking.seats.map((seat) => seat.seatCode).join(', ')}</span>
                                <strong>{formatCustomerCurrency(booking.totalPrice)}</strong>
                            </footer>
                            <div className="customer-history-card__actions">
                                {canPayNow(booking) && (
                                    <button
                                        type="button"
                                        onClick={(event) => openPayment(event, booking)}
                                    >
                                        Thanh toán ngay
                                    </button>
                                )}
                                <button
                                    type="button"
                                    disabled={!canCancel(booking)}
                                    title={canCancel(booking) ? 'Hủy vé và yêu cầu hoàn tiền' : 'Chỉ được hủy vé trước giờ xuất bến ít nhất 5 tiếng'}
                                    onClick={(event) => openCancellation(event, booking)}
                                >
                                    Hủy vé
                                </button>
                            </div>
                        </article>
                    ))}
                </div>
            </section>

            {cancellationBooking && (
                <TicketCancellationModal
                    booking={cancellationBooking}
                    onClose={() => setCancellationBooking(null)}
                    onCancelled={handleCancelled}
                />
            )}
        </main>
    );
}
