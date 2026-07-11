import { BsPencilFill } from "react-icons/bs";
import { Badge, Button, Table } from "react-bootstrap";
import { formatCurrency } from "../../../utils/formatters";
import '../styles/CargoTypeManagement.css';

/**
 * Displays cargo types and their surcharge data in one staff table.
 */
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
        <Table responsive hover className="align-middle mb-0 cargo-type-management-table">
            <thead className="table-light text-secondary">
                <tr>
                    <th className="py-3 px-3">Tên loại hàng</th>
                    <th className="py-3 px-3">Đơn vị</th>
                    <th className="py-3 px-3">Đơn giá</th>
                    <th className="py-3 px-3">Trạng thái</th>
                    <th className="py-3 px-3 text-center">Hành động</th>
                </tr>
            </thead>
            <tbody>
                {!data || data.length === 0 ? (
                    <tr>
                        <td colSpan="5" className="cargo-type-management-empty">
                            Không tìm thấy dữ liệu
                        </td>
                    </tr>
                ) : (
                    data.map((item) => (
                        <tr key={item.cargoTypeId}>
                            <td className="px-3 fw-bold text-dark">{item.cargoTypeName}</td>
                            <td className="px-3">{item.unit || '---'}</td>
                            <td className="px-3 cargo-type-management-table__price">
                                {formatCurrency(item.pricePerUnit)}
                            </td>
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
