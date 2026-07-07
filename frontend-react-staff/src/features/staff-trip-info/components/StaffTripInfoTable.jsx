import { Badge, Table } from 'react-bootstrap';
import { formatCurrency, formatDateTime } from '../../../utils/formatters';

const STATUS_BADGES = {
    SCHEDULED: 'success',
    IN_PROGRESS: 'warning',
    COMPLETED: 'secondary',
    CANCELLED: 'danger',
    CANCELED: 'danger',
};

/** Converts stored seat-layout JSON into a compact readable label. */
const formatSeatLayout = (seatLayoutName) => {
    if (!seatLayoutName) return '---';
    try {
        const layout = JSON.parse(seatLayoutName);
        const floors = layout.totalFloors ? `${layout.totalFloors} tầng` : '';
        const rows = layout.rows ? `${layout.rows} hàng` : '';
        const cols = layout.cols ? `${layout.cols} cột` : '';
        return [floors, rows, cols].filter(Boolean).join(' · ') || 'Sơ đồ tùy chỉnh';
    } catch {
        return seatLayoutName;
    }
};

/**
 * Displays upcoming trips for ticket staff.
 * The column order mirrors the operational flow: route, coach, crew, time,
 * status, price, and capacity.
 */
export default function StaffTripInfoTable({ data, loading }) {
    if (loading) {
        return <div className="staff-trip-info-loading">Đang tải danh sách chuyến xe...</div>;
    }

    return (
        <Table responsive hover className="staff-trip-info-table align-middle mb-0">
            <thead>
                <tr>
                    <th>Thành phố</th>
                    <th>Tuyến đường</th>
                    <th>Biển số xe</th>
                    <th>Loại xe</th>
                    <th>Sơ đồ ghế</th>
                    <th>Tài xế</th>
                    <th>Phụ xe</th>
                    <th>Khởi hành</th>
                    <th>Đến dự kiến</th>
                    <th>Thời lượng</th>
                    <th>Giá vé</th>
                    <th>Ghế trống</th>
                    <th>Trạng thái</th>
                </tr>
            </thead>
            <tbody>
                {data.length === 0 ? (
                    <tr>
                        <td colSpan="13" className="staff-trip-info-empty">
                            Không tìm thấy chuyến xe phù hợp.
                        </td>
                    </tr>
                ) : (
                    data.map((trip) => (
                        <tr key={trip.tripId}>
                            <td>{trip.departureCity || '---'}</td>
                            <td className="staff-trip-info-route">{trip.routeName || '---'}</td>
                            <td className="staff-trip-info-plate">{trip.licensePlate || '---'}</td>
                            <td>{trip.coachTypeName || '---'}</td>
                            <td>{formatSeatLayout(trip.seatLayoutName)}</td>
                            <td>{trip.driverName || 'Chưa phân công'}</td>
                            <td>{trip.attendantName || 'Chưa phân công'}</td>
                            <td>{formatDateTime(trip.departureTime)}</td>
                            <td>{formatDateTime(trip.arrivalTime)}</td>
                            <td>{trip.duration || '---'}</td>
                            <td>{formatCurrency(trip.seatPrice)}</td>
                            <td>{trip.availableSeats ?? 0} / {trip.totalSeats ?? 0}</td>
                            <td>
                                <Badge bg={STATUS_BADGES[trip.status] || 'info'}>
                                    {trip.status || 'UNKNOWN'}
                                </Badge>
                            </td>
                        </tr>
                    ))
                )}
            </tbody>
        </Table>
    );
}
