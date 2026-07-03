import { Table, Badge, Button } from 'react-bootstrap';

export default function CoachTable({ data, loading, statusLabels, onRowClick }) {
    if (loading) {
        return (
            <div className="text-center p-5 text-secondary fw-medium">
                Đang tải dữ liệu...
            </div>
        );
    }

    return (
        <Table hover responsive className="align-middle mb-0">
            <thead className="table-light text-secondary">
                <tr>
                    <th className="py-3 px-3">Biển số xe</th>
                    <th className="py-3 px-3">Loại xe</th>
                    <th className="py-3 px-3">Xuất xứ xe</th>
                    <th className="py-3 px-3">Số ghế ngồi</th>
                    <th className="py-3 px-3">Trạng thái</th>
                    <th className="py-3 px-3 text-center">Hành động</th>
                </tr>
            </thead>
            <tbody>
                {!data || data.length === 0 ? (
                    <tr>
                        <td colSpan="6" className="text-center p-5 text-muted">
                            Không tìm thấy dữ liệu
                        </td>
                    </tr>
                ) : (data.map(coach => (
                    <tr key={coach.coachId}>
                        <td className="px-3 fw-bold text-dark">{coach.licensePlate}</td>
                        <td className="px-3">{coach.coachTypeName}</td>
                        <td className="px-3">{coach.manufacturerAndYear}</td>
                        <td className="px-3">{coach.totalSeat}</td>
                        <td className="px-3">
                            <Badge bg={statusLabels[coach.status || 'RETIRED'].bg} className="px-2 py-1 rounded-pill">
                                {statusLabels[coach.status || 'RETIRED'].text}
                            </Badge>
                        </td>
                        <td className="px-3 text-center" onClick={(e) => e.stopPropagation()}>
                            <Button
                                size="sm"
                                className="fw-medium shadow-sm custom-btn-general"
                                onClick={() => onRowClick(coach)}
                            >
                                Quản lý
                            </Button>
                        </td>
                    </tr>
                )))}
            </tbody>
        </Table>
    );
}
