import { useState } from 'react';
import { customerHistoryApi } from '../api/customerHistoryApi';

const EMPTY_FORM = { bankName: '', accountHolder: '', accountNumber: '' };

/**
 * Collects a bank destination and submits an authenticated ticket cancellation.
 */
export default function TicketCancellationModal({ booking, onClose, onCancelled }) {
    const [form, setForm] = useState(EMPTY_FORM);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    /** Updates one controlled refund field while clearing stale server errors. */
    const handleChange = (event) => {
        setError('');
        setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
    };

    /** Sends the cancellation once; the backend owns all policy and concurrency checks. */
    const handleSubmit = async (event) => {
        event.preventDefault();
        setSubmitting(true);
        setError('');

        try {
            const result = await customerHistoryApi.cancelTicket(booking.ticketCode, {
                bankName: form.bankName.trim(),
                accountHolder: form.accountHolder.trim(),
                accountNumber: form.accountNumber.trim(),
            });
            onCancelled(result);
        } catch (requestError) {
            setError(requestError.response?.data?.message || 'Không thể gửi yêu cầu hủy vé.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="ticket-cancel-modal" role="dialog" aria-modal="true" aria-labelledby="cancel-ticket-title">
            <form className="ticket-cancel-card" onSubmit={handleSubmit}>
                <h2 id="cancel-ticket-title">Xác nhận hủy vé</h2>
                <p>
                    Bạn có chắc chắn muốn hủy vé <strong>{booking.ticketCode}</strong>? Yêu cầu hoàn tiền
                    sẽ được ghi nhận và xử lý theo chính sách hiện hành.
                </p>

                <label htmlFor="refund-bank-name">Tên ngân hàng</label>
                <input id="refund-bank-name" name="bankName" value={form.bankName} onChange={handleChange} maxLength="100" required placeholder="Tên ngân hàng" />

                <label htmlFor="refund-account-holder">Tên chủ tài khoản</label>
                <input id="refund-account-holder" name="accountHolder" value={form.accountHolder} onChange={handleChange} maxLength="150" required placeholder="Tên chủ tài khoản" />

                <label htmlFor="refund-account-number">Số tài khoản</label>
                <input id="refund-account-number" name="accountNumber" value={form.accountNumber} onChange={handleChange} inputMode="numeric" pattern="[0-9]{6,30}" required placeholder="Số tài khoản" />

                <small>Số tiền hoàn sẽ được thông báo sau khi hệ thống xác nhận yêu cầu.</small>
                {error && <div className="ticket-cancel-card__error" role="alert">{error}</div>}

                <footer>
                    <button type="button" className="ticket-cancel-card__back" onClick={onClose} disabled={submitting}>Hủy</button>
                    <button type="submit" className="ticket-cancel-card__confirm" disabled={submitting}>
                        {submitting ? 'Đang xử lý...' : 'Xác nhận hủy'}
                    </button>
                </footer>
            </form>
        </div>
    );
}
