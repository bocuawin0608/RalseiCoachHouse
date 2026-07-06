import { Table, Badge, Button } from 'react-bootstrap';
import { formatCurrency } from '../../../utils/formatters';

export default function CoachTypeTable({ data, loading, onRowClick }) {
    if (loading) {
        return (
            <div className="text-center p-5 text-secondary fw-medium">
                Đang tải dữ liệu...
            </div>
        );
    }

    return (
        <Table responsive hover className="align-middle mb-0">
            <thead className="table-light text-secondary">
                <tr>
                    <th className="py-3 px-3">ID</th>
                    <th className="py-3 px-3">Tên loại xe</th>
                    <th className="py-3 px-3">Tổng số ghế</th>
                    <th className="py-3 px-3">Tổng số xe</th>
                    <th className="py-3 px-3">Giá hiện tại</th>
                    <th className="py-3 px-3">Trạng thái</th>
                    <th className="py-3 px-3 text-center">Hành động</th>
                </tr>
            </thead>
            <tbody>
                {!data || data.length === 0 ? (
                    <tr>
                        <td colSpan="7" className="text-center p-5 text-muted">
                            Không tìm thấy dữ liệu
                        </td>
                    </tr>
                ) : (
                    data.map((item) => (
                        <tr key={item.coachTypeId}>
                            <td className="px-3 fw-medium text-secondary">#{item.coachTypeId}</td>
                            <td className="px-3 fw-bold text-dark">{item.coachTypeName}</td>
                            <td className="px-3">{item.totalSeat} ghế</td>
                            <td className="px-3">{item.totalCoach ?? 0} xe</td>
                            <td className="px-3 fw-bold text-primary">
                                {formatCurrency(item.currentPrice || item.seatPrice)}
                            </td>
                            <td className="px-3">
                                <Badge
                                    bg={item.isActive ? 'success' : 'danger'}
                                    className="px-2 py-1 rounded-pill"
                                >
                                    {item.isActive ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                                </Badge>
                            </td>
                            <td className="px-3 text-center" onClick={(e) => e.stopPropagation()}>
                                <Button
                                    size="sm"
                                    className="fw-medium shadow-sm custom-btn-general"
                                    onClick={() => onRowClick(item)}
                                >
                                    Quản lý
                                </Button>
                            </td>
                        </tr>
                    ))
                )}
            </tbody>
        </Table>
    );
}
