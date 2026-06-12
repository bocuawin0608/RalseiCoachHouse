import { BsEye, BsPencilFill, BsSignpostSplit } from "react-icons/bs";
import { Badge, Button, Table } from "react-bootstrap";

export default function RouteTable({
    data,
    loading,
    onEditInfo,
    onViewDetail,
    onManageStops
}) {

    if (loading) {
        return (
            <div className="text-center p-5 text-secondary fw-medium">
                Đang tải dữ liệu...
            </div>
        );
    }

    if (!data || data.length === 0) {
        return <div className="text-center py-5 text-secondary">Không tìm thấy tuyến đường nào.</div>;
    }

    return (
        <Table responsive hover className="align-middle mb-0">
            <thead className="table-light text-secondary">
                <tr>
                    <th className="py-3 px-3">ID</th>
                    <th className="py-3 px-3">Tên tuyến đường</th>
                    <th className="py-3 px-3">Khoảng cách</th>
                    <th className="py-3 px-3">Thời gian đi</th>
                    <th className="py-3 px-3">Trạng thái</th>
                    <th className="py-3 px-3 text-center">Hành động</th>
                </tr>
            </thead>
            <tbody>
                {data.map((item) => (
                    <tr key={item.routeId}>
                        <td className="px-3 fw-medium text-secondary">#{item.routeId}</td>
                        <td className="px-3 fw-bold text-dark">{item.routeName}</td>
                        <td className="px-3">{item.totalKilometers} km</td>
                        <td className="px-3">{item.totalMinutes} min</td>
                        <td className="px-3">
                            <Badge
                                bg={item.active ? 'success' : 'secondary'}
                                className="px-2 py-1 rounded-pill"
                            >
                                {item.active ? 'Đang hoạt động' : 'Ngừng hoạt động'}
                            </Badge>
                        </td>
                        <td className="px-3">
                            <div className="d-flex gap-2 justify-content-center align-items-center">
                                <Button
                                    variant="primary"
                                    className="d-flex align-items-center"
                                    onClick={() => onViewDetail(item)}
                                    title="Xem chi tiết"
                                >
                                    <BsEye size={16} />
                                </Button>

                                <Button
                                    variant="primary"
                                    className="d-flex align-items-center"
                                    onClick={() => onEditInfo(item)}
                                    title="Sửa thông tin"
                                >
                                    <BsPencilFill size={16} />
                                </Button>

                                <Button
                                    variant="primary"
                                    className="d-flex align-items-center gap-1"
                                    onClick={() => onManageStops(item)}
                                    title="Quản lý trạm dừng"
                                >
                                    <BsSignpostSplit size={16} />
                                    <span className="small fw-medium"></span>
                                </Button>
                            </div>
                        </td>
                    </tr>
                ))
                }
            </tbody>
        </Table>
    );
}
