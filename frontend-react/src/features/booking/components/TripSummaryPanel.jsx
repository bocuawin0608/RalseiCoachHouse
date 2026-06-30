import { formatTripDepartureLabel } from '../../../utils/formatters';

export default function TripSummaryPanel({
    tripInfo,
    pickupStopName,
    dropoffStopName,
    seatCount,
    seatCodes,
}) {
    if (!tripInfo) return null;

    return (
        <div className="mb-4 pb-3 border-bottom" style={{ borderColor: '#e0e0e0' }}>
            <div className="fw-bold mb-3" style={{ fontSize: '1.05rem', color: 'var(--ralsei-black)' }}>
                Thông tin chuyến đi
            </div>
            <div className="d-flex flex-column gap-2" style={{ fontSize: '0.85rem' }}>
                <SummaryRow label="Lộ trình" value={tripInfo.routeName || '---'} />
                <SummaryRow label="Thời gian xuất bến" value={formatTripDepartureLabel(tripInfo.departureTime)} />
                <SummaryRow label="Loại xe" value={tripInfo.coachTypeName || '---'} />
                <SummaryRow label="Điểm đón" value={pickupStopName || '---'} />
                <SummaryRow label="Điểm trả" value={dropoffStopName || '---'} />
                <SummaryRow label="Số lượng" value={`${seatCount} Ghế`} />
                <SummaryRow label="Vị trí" value={seatCodes?.length ? seatCodes.join(', ') : '---'} />
            </div>
        </div>
    );
}

function SummaryRow({ label, value }) {
    return (
        <div>
            <div className="text-muted mb-0" style={{ fontSize: '0.75rem' }}>{label}</div>
            <div className="fw-medium text-dark">{value}</div>
        </div>
    );
}