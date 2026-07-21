import { formatCurrency } from '../../../utils/formatters';

const FEE_PAYER_LABEL = {
    SENDER: 'Người gửi trả',
    RECEIVER: 'Người nhận trả'
};

/**
 * Compact cargo sticker for browser print / PDF.
 * Rendered into a print host; not shown in normal UI.
 */
export default function CargoTicketPrintLabel({ ticket, pieceCount = null }) {
    if (!ticket) return null;

    const cod = Number(ticket.codAmount || 0);
    const routeLine = [ticket.pickupStopName, ticket.dropoffStopName]
        .filter(Boolean)
        .join(' → ') || '—';

    return (
        <article className="cargo-print-label">
            <p className="cargo-print-cut-hint">Paper size → A5 (nhỏ nhất; không có custom mm)</p>
            <p className="cargo-print-brand">Nhà xe Ralsei · Tem gửi hàng</p>
            <h1 className="cargo-print-code">{ticket.ticketCode || '—'}</h1>

            <section className="cargo-print-section">
                <div className="cargo-print-label-row">
                    <span className="cargo-print-k">Người gửi</span>
                    <span className="cargo-print-v">
                        <span className="cargo-print-party-name">{ticket.senderName || '—'}</span>
                        <br />
                        {ticket.senderPhone || '—'}
                    </span>
                </div>
                <div className="cargo-print-label-row">
                    <span className="cargo-print-k">Người nhận</span>
                    <span className="cargo-print-v">
                        <span className="cargo-print-party-name">{ticket.receiverName || '—'}</span>
                        <br />
                        {ticket.receiverPhone || '—'}
                    </span>
                </div>
            </section>

            <section className="cargo-print-section">
                <p className="cargo-print-route">{routeLine}</p>
                {ticket.routeName && <p className="cargo-print-meta">Tuyến: {ticket.routeName}</p>}
            </section>

            <section className="cargo-print-section">
                <div className="cargo-print-label-row">
                    <span className="cargo-print-k">Trả phí</span>
                    <span className="cargo-print-v">
                        {FEE_PAYER_LABEL[ticket.feePayer] || ticket.feePayer || '—'}
                    </span>
                </div>
                <div className="cargo-print-label-row">
                    <span className="cargo-print-k">Cước phí</span>
                    <span className="cargo-print-v">{formatCurrency(ticket.totalPrice)}</span>
                </div>
                {cod > 0 && (
                    <div className="cargo-print-label-row">
                        <span className="cargo-print-k">COD</span>
                        <span className="cargo-print-v">{formatCurrency(cod)}</span>
                    </div>
                )}
                {pieceCount != null && pieceCount > 0 && (
                    <div className="cargo-print-label-row">
                        <span className="cargo-print-k">Số kiện</span>
                        <span className="cargo-print-v">{pieceCount}</span>
                    </div>
                )}
            </section>
        </article>
    );
}
