import { BsPencilFill } from "react-icons/bs";
import { Badge, Button, Table } from "react-bootstrap";

export default function CargoTypeTable({
    data,
    loading,
    onEditInfo
}) {
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
                    <th className="py-3 px-3">Tên loại hàng</th>
                    <th className="py-3 px-3">Trạng thái</th>
                    <th className="py-3 px-3 text-center">Hành động</th>
                </tr>
            </thead>
            <tbody>
                {!data || data.length === 0 ? (
                    <tr>
                        <td colSpan="4" className="text-center p-5 text-muted">
                            Không tìm thấy dữ liệu
                        </td>
                    </tr>
                ) : (
                    data.map((item) => (
                        <tr key={item.cargoTypeId}>
                            <td className="px-3 fw-medium text-secondary">#{item.cargoTypeId}</td>
                            <td className="px-3 fw-bold text-dark">{item.cargoTypeName}</td>
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
                                        className="d-flex align-items-center custom-btn-general"
                                        onClick={() => onEditInfo(item)}
                                        title="Sửa thông tin"
                                    >
                                        <BsPencilFill size={16} />
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
