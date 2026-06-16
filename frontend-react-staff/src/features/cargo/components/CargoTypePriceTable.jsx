import { BsPencilFill } from "react-icons/bs";
import { Button, Table } from "react-bootstrap";
import { formatCurrency } from "../../../utils/formatters";
export default function CargoTypePriceTable({ 
    data, 
    loading, 
    cargoTypes,
    onEditInfo 
}) {
    
    if (loading) {
        return (
            <div className="text-center p-5 text-secondary fw-medium">
                Đang tải dữ liệu...
            </div>
        );
    }

    const getCargoTypeName = (id) => {
        if (!cargoTypes) return id;
        const type = cargoTypes.find(t => t.cargoTypeId === id);
        return type ? type.cargoTypeName : id;
    };

    return (
        <Table responsive hover className="align-middle mb-0">
            <thead className="table-light text-secondary">
                <tr>
                    <th className="py-3 px-3">ID</th>
                    <th className="py-3 px-3">Loại Hàng</th>
                    <th className="py-3 px-3">Đơn Vị</th>
                    <th className="py-3 px-3">Đơn Giá</th>
                    <th className="py-3 px-3">Bắt Đầu</th>
                    <th className="py-3 px-3">Kết Thúc</th>
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
                        <tr key={item.cargoTypePriceId}>
                            <td className="px-3 fw-medium text-secondary">#{item.cargoTypePriceId}</td>
                            <td className="px-3 fw-bold text-dark">{getCargoTypeName(item.cargoTypeId)}</td>
                            <td className="px-3">{item.unit}</td>
                            <td className="px-3 fw-bold text-primary">
                                {formatCurrency(item.pricePerUnit)}
                            </td>
                            <td className="px-3">
                                {item.startEffectiveDate ? new Date(item.startEffectiveDate).toLocaleString() : ''}
                            </td>
                            <td className="px-3">
                                {item.endEffectiveDate ? new Date(item.endEffectiveDate).toLocaleString() : ''}
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
