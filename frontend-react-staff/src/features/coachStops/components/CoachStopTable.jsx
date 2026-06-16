import { Button, Table } from 'react-bootstrap';
import { BsEye, BsPencilFill } from 'react-icons/bs';

export default function CoachStopTable({ data, loading, onViewDetail, onEditInfo }) {
    if (loading) {
        return <div className="text-center py-5 text-secondary">Đang tải dữ liệu...</div>;
    }

    if (!data || data.length === 0) {
        return <div className="text-center py-5 text-secondary">Không tìm thấy điểm dừng nào.</div>;
    }

    return (
        <div className="table-responsive">
            <Table hover className="m-0 align-middle">
                <thead className="bg-light">
                    <tr>
                        <th className="py-3 px-3 ">ID</th>
                        <th className="py-3 px-3 ">Tên Điểm Dừng</th>
                        <th className="py-3 px-3 ">Địa Chỉ</th>
                        <th className="py-3 px-3 ">Thành Phố</th>
                        <th className="py-3 px-3 ">Trạng Thái</th>
                        <th className="py-3 px-3 text-center">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    {data.map((stop) => (
                        <tr key={stop.stopPointId}>
                            <td className="px-3 fw-medium text-secondary">#{stop.stopPointId}</td>
                            <td className="px-3 fw-bold text-dark">{stop.stopPointName}</td>
                            <td className="px-3">{stop.address}</td>
                            <td className="px-3">{stop.city}</td>
                            <td className="px-3">
                                <span className={`badge px-2 py-1 rounded-pill ${stop.active ? 'bg-success text-white' : 'bg-secondary text-white'}`}>
                                    {stop.active ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                                </span>
                            </td>
                            <td className="px-3">
                                <div className="d-flex gap-2 justify-content-center align-items-center">
                                    <Button
                                        className="d-flex align-items-center custom-btn-general"
                                        onClick={() => onViewDetail(stop)}
                                        title="Xem chi tiết"
                                    >
                                        <BsEye size={16} />
                                    </Button>
                                    <Button
                                        className="d-flex align-items-center custom-btn-general"
                                        onClick={() => onEditInfo(stop)}
                                        title="Sửa thông tin"
                                    >
                                        <BsPencilFill size={16} />
                                    </Button>
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </Table>
        </div>
    );
}
