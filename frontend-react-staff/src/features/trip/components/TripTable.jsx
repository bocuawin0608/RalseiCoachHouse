import { BsPencilFill, BsTrashFill } from 'react-icons/bs';
import { Badge, Button, Table } from 'react-bootstrap';
import './TripTable.css';

/**
 * Map tripStatus string to Bootstrap badge variant.
 * Keeps visual feedback consistent across statuses.
 */
const getStatusVariant = (status) => {
    switch (status?.toLowerCase()) {
        case 'active':
        case 'scheduled':
            return 'success';
        case 'cancelled':
        case 'deleted':
            return 'danger';
        case 'completed':
            return 'secondary';
        default:
            return 'warning';
    }
};

/**
 * Render trip summaries in a responsive table.
 * Columns map directly to TripSummaryProjection fields.
 */
export default function TripTable({ data, loading, onEditInfo, onDelete }) {
    if (loading) {
        return <div className="trip-table-loading">Đang tải dữ liệu...</div>;
    }

    return (
        <Table responsive hover className="align-middle mb-0">
            <thead className="table-light text-secondary">
                <tr>
                    <th className="py-3 px-3">ID</th>
                    <th className="py-3 px-3">Biển số xe</th>
                    <th className="py-3 px-3">Hãng xe</th>
                    <th className="py-3 px-3">Loại xe</th>
                    <th className="py-3 px-3">Ngày khởi hành</th>
                    <th className="py-3 px-3">Giờ khởi hành</th>
                    <th className="py-3 px-3">Ghế trống / Tổng</th>
                    <th className="py-3 px-3">Trạng thái xe</th>
                    <th className="py-3 px-3">Trạng thái chuyến</th>
                    <th className="py-3 px-3 text-center">Hành động</th>
                </tr>
            </thead>
            <tbody>
                {!data || data.length === 0 ? (
                    <tr>
                        <td colSpan="10" className="trip-table-empty">
                            Không tìm thấy dữ liệu
                        </td>
                    </tr>
                ) : (
                    data.map((item) => (
                        <tr key={item.tripId}>
                            {/* tripId */}
                            <td className="trip-table-id">#{item.tripId}</td>

                            {/* licensePlate */}
                            <td className="trip-table-bold">{item.licensePlate}</td>

                            {/* manufacturer */}
                            <td className="trip-table-cell">{item.manufacturer}</td>

                            {/* coachTypeName */}
                            <td className="trip-table-cell">{item.coachTypeName}</td>

                            {/* departureDate - LocalDate */}
                            <td className="trip-table-cell">
                                {item.departureDate
                                    ? new Date(item.departureDate).toLocaleDateString('vi-VN')
                                    : '—'}
                            </td>

                            {/* departureTime - LocalTime string e.g. "08:30:00" */}
                            <td className="trip-table-cell">
                                {item.departureTime
                                    ? item.departureTime.substring(0, 5)
                                    : '—'}
                            </td>

                            {/* availableSeats / totalSeats */}
                            <td className="trip-table-cell">
                                {item.availableSeats} / {item.totalSeats}
                            </td>

                            {/* coachStatus */}
                            <td className="trip-table-status">
                                <Badge
                                    bg={item.coachStatus === 'ACTIVE' ? 'success' : 'secondary'}
                                    className="px-2 py-1 rounded-pill"
                                >
                                    {item.coachStatus}
                                </Badge>
                            </td>

                            {/* tripStatus */}
                            <td className="trip-table-status">
                                <Badge
                                    bg={getStatusVariant(item.tripStatus)}
                                    className="px-2 py-1 rounded-pill"
                                >
                                    {item.tripStatus}
                                </Badge>
                            </td>

                            {/* Actions */}
                            <td className="trip-table-actions">
                                <div className="trip-table-action-group">
                                    <Button
                                        className="d-flex align-items-center custom-btn-general"
                                        onClick={() => onEditInfo(item)}
                                        title="Sửa thông tin"
                                    >
                                        <BsPencilFill size={16} />
                                    </Button>
                                    <Button
                                        variant="outline-danger"
                                        className="d-flex align-items-center"
                                        onClick={() => onDelete(item)}
                                        title="Hủy chuyến"
                                    >
                                        <BsTrashFill size={16} />
                                    </Button>
                                </div>
                            </td>
                        </tr>
                    ))
                )}
            </tbody>
        </Table>
    );
}
