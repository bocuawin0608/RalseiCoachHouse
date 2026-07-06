import { useEffect, useRef, useState } from 'react';
import { FiArrowLeft, FiMapPin, FiX } from 'react-icons/fi';
import { useNavigate, useParams } from 'react-router-dom';
import { customerHistoryApi } from '../api/customerHistoryApi';
import {
    formatCustomerCurrency,
    formatCustomerDateTime,
    formatCustomerTicketStatus,
} from '../utils/customerHistoryFormatters';
import '../styles/customerHistory.css';

const EMPTY_QR_STATE = { url: '', seatCode: '', loading: false, error: '' };

/**
 * Displays one customer-owned transaction and protected QR images per seat.
 */
export default function CustomerHistoryDetailPage() {
    const { ticketCode } = useParams();
    const navigate = useNavigate();
    const qrObjectUrlRef = useRef('');
    const [pageState, setPageState] = useState({ data: null, loading: true, error: '' });
    const [qrState, setQrState] = useState(EMPTY_QR_STATE);

    useEffect(() => {
        let isMounted = true;

        customerHistoryApi.getDetail(ticketCode)
            .then((data) => {
                if (isMounted) setPageState({ data, loading: false, error: '' });
            })
            .catch(() => {
                if (isMounted) {
                    setPageState({ data: null, loading: false, error: 'Không tìm thấy giao dịch trong tài khoản của bạn.' });
                }
            });

        return () => { isMounted = false; };
    }, [ticketCode]);

    useEffect(() => () => {
        if (qrObjectUrlRef.current) URL.revokeObjectURL(qrObjectUrlRef.current);
    }, []);

    /** Releases the temporary blob URL to prevent browser memory leaks. */
    const releaseQrObjectUrl = () => {
        if (qrObjectUrlRef.current) {
            URL.revokeObjectURL(qrObjectUrlRef.current);
            qrObjectUrlRef.current = '';
        }
    };

    /** Closes the QR dialog and releases its protected image blob. */
    const closeQr = () => {
        releaseQrObjectUrl();
        setQrState(EMPTY_QR_STATE);
    };

    /** Requests a QR image only when the customer explicitly selects an owned seat. */
    const openSeatQr = async (seat) => {
        releaseQrObjectUrl();
        setQrState({ url: '', seatCode: seat.seatCode, loading: true, error: '' });

        try {
            const imageBlob = await customerHistoryApi.getSeatQr(seat.ticketDetailId);
            const objectUrl = URL.createObjectURL(imageBlob);
            qrObjectUrlRef.current = objectUrl;
            setQrState({ url: objectUrl, seatCode: seat.seatCode, loading: false, error: '' });
        } catch {
            setQrState({
                url: '',
                seatCode: seat.seatCode,
                loading: false,
                error: 'Mã QR chưa sẵn sàng cho ghế này.',
            });
        }
    };

    if (pageState.loading) {
        return <main className="customer-history-page"><div className="customer-history-state">Đang tải chi tiết giao dịch...</div></main>;
    }

    if (pageState.error || !pageState.data) {
        return <main className="customer-history-page"><div className="customer-history-state customer-history-state--error">{pageState.error}</div></main>;
    }

    const booking = pageState.data;

    return (
        <main className="customer-history-page">
            <section className="customer-detail-shell">
                <header className="customer-detail-heading">
                    <button type="button" onClick={() => navigate('/history')}>
                        <FiArrowLeft aria-hidden="true" /> Quay lại
                    </button>
                    <div><span>CHI TIẾT GIAO DỊCH</span><h1>{booking.ticketCode}</h1></div>
                    <strong className={`ticket-status ticket-status--${booking.status?.toLowerCase()}`}>
                        {formatCustomerTicketStatus(booking.status)}
                    </strong>
                </header>

                <div className="customer-detail-grid">
                    <article className="customer-detail-panel customer-detail-panel--wide">
                        <h2>Thông tin chuyến đi</h2>
                        <p className="customer-detail-route"><FiMapPin aria-hidden="true" /> {booking.routeName}</p>
                        <dl>
                            <div><dt>Điểm đón</dt><dd>{booking.pickupStopName}</dd></div>
                            <div><dt>Điểm trả</dt><dd>{booking.dropoffStopName}</dd></div>
                            <div><dt>Thời gian xuất bến</dt><dd>{formatCustomerDateTime(booking.departureTime)}</dd></div>
                            <div><dt>Loại xe</dt><dd>{booking.coachTypeName}</dd></div>
                        </dl>
                    </article>

                    <article className="customer-detail-panel">
                        <h2>QR chuyến đi</h2>
                        <p className="customer-detail-note">Chọn số ghế để xem mã QR kiểm tra vé.</p>
                        <div className="customer-seat-list">
                            {booking.seats.map((seat) => (
                                <button type="button" key={seat.ticketDetailId} onClick={() => openSeatQr(seat)}>
                                    Ghế {seat.seatCode}
                                </button>
                            ))}
                        </div>
                    </article>

                    <article className="customer-detail-panel">
                        <h2>Chi phí hành trình</h2>
                        <dl>
                            <div><dt>Số ghế</dt><dd>{booking.seats.length}</dd></div>
                            <div><dt>Tổng tiền</dt><dd className="customer-detail-price">{formatCustomerCurrency(booking.totalPrice)}</dd></div>
                            <div><dt>Trạng thái</dt><dd>{formatCustomerTicketStatus(booking.status)}</dd></div>
                            <div><dt>Thanh toán</dt><dd>{booking.paymentMethod || 'Chưa cập nhật'}</dd></div>
                        </dl>
                    </article>

                    <article className="customer-detail-panel customer-detail-panel--wide">
                        <h2>Thông tin hành khách</h2>
                        <dl>
                            <div><dt>Họ và tên</dt><dd>{booking.fullName}</dd></div>
                            <div><dt>Số điện thoại</dt><dd>{booking.phone}</dd></div>
                            <div><dt>Email</dt><dd>{booking.email || 'Không cung cấp'}</dd></div>
                            <div><dt>Ngày đặt</dt><dd>{formatCustomerDateTime(booking.bookedAt)}</dd></div>
                        </dl>
                    </article>
                </div>
            </section>

            {(qrState.loading || qrState.url || qrState.error) && (
                <div className="customer-qr-modal" role="dialog" aria-modal="true" aria-label={`Mã QR ghế ${qrState.seatCode}`}>
                    <div className="customer-qr-card">
                        <button type="button" className="customer-qr-card__close" onClick={closeQr} aria-label="Đóng mã QR">
                            <FiX aria-hidden="true" />
                        </button>
                        <span>MÃ KIỂM TRA VÉ</span>
                        <h2>Ghế {qrState.seatCode}</h2>
                        {qrState.loading && <p>Đang tạo mã QR...</p>}
                        {qrState.error && <p className="customer-qr-card__error">{qrState.error}</p>}
                        {qrState.url && <img src={qrState.url} alt={`Mã QR kiểm tra vé ghế ${qrState.seatCode}`} />}
                        <p>Xuất trình mã này cho nhân viên khi lên xe.</p>
                    </div>
                </div>
            )}
        </main>
    );
}
