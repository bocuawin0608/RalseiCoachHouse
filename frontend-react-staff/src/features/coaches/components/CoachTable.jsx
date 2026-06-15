import { Table, Badge, Button } from 'react-bootstrap';
import { BsEye, BsLayoutSplit, BsPencilFill } from 'react-icons/bs';

const STATUS_COLOR = {
    ACTIVE: 'success',
    MAINTENANCE: 'warning',
    RETIRED: 'danger'
}

export default function CoachTable({ data, loading, onViewDetail, onEditInfo, onEditSeatMap }) {
    
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
                        <td colSpan="7" className="text-center p-5 text-muted">
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
                            <Badge bg={STATUS_COLOR[coach.status || 'RETIRED']}>
                                {coach.status}
                            </Badge>
                        </td>
                        <td className="px-3">
                            <div className="d-flex gap-2 justify-content-center align-items-center">
                                <Button variant="primary" className="d-flex align-items-center" 
                                    onClick={() => onViewDetail(coach)} title='Xem chi tiết'>
                                        <BsEye size={16} />
                                </Button>
                                <Button variant="primary" className="d-flex align-items-center" 
                                    onClick={() => onEditInfo(coach)} title='Sửa thông tin'>
                                        <BsPencilFill size={16} />
                                </Button>
                                <Button 
                                    variant="primary"
                                    className="d-flex align-items-center"
                                    onClick={() => onEditSeatMap?.(coach)}
                                    title="Sửa sơ đồ ghế của xe"
                                >
                                    <BsLayoutSplit size={16} />
                                </Button>
                            </div>
                        </td>
                    </tr>
                )))}
            </tbody>
        </Table>
    );
};