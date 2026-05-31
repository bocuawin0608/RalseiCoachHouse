import { BsEye, BsPencilFill, BsCashStack, BsLayoutSplit } from "react-icons/bs";
import Button from "../common/Button";

export default function SeatLayoutTable({ layouts, navigate }) {
    return (
        <table border="1" cellPadding="10" style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Tên sơ đồ</th>
                    <th>Tổng số ghế</th>
                    <th>Giá hiện tại</th>
                    <th>Trạng thái</th>
                    <th style={{ textAlign: 'center' }}>Hành động</th>
                </tr>
            </thead>
            <tbody>
                {layouts.length === 0 ? (
                    <tr>
                        <td colSpan="6" style={{ textAlign: 'center' }}>Không tìm thấy dữ liệu</td>
                    </tr>
                ) : (layouts.map((layout) => (
                    <tr key={layout.seatLayoutId}>
                        <td>{layout.seatLayoutId}</td>
                        <td>{layout.seatLayoutName}</td>
                        <td>{layout.totalSeat}</td>
                        <td>{layout.currentPrice?.toLocaleString('vi-VN')} đ</td>

                        <td>
                            <span style={{ color: layout.isActive ? 'green' : 'red', fontWeight: 'bold' }}>
                                {layout.isActive ? 'Đang hoạt động' : 'Đã tắt'}
                            </span>
                        </td>

                        <td style={{ textAlign: 'center'}}>
                            <div style={{ display: 'flex', gap: '5px', justifyContent: 'center'}}>
                                <Button 
                                    variant="primary" 
                                    iconOnly={true} onClick={() => navigate(`/seat-layouts/${layout.seatLayoutId}`)}
                                    title="Xem chi tiết"
                                >
                                    <BsEye size={18} />
                                </Button>

                                <Button 
                                    variant="primary" 
                                    iconOnly={true} 
                                    title="Sửa thông tin">
                                    <BsPencilFill  size={15} />
                                </Button>

                                <Button 
                                    variant="primary" 
                                    iconOnly={true} 
                                    title="Sửa giá">
                                    <BsCashStack  size={18} />
                                </Button>

                                <Button 
                                    variant="primary" 
                                    iconOnly={true} 
                                    title="Sửa ghế">
                                    <BsLayoutSplit  size={18} />
                                </Button>
                            </div>
                        </td>
                    </tr>
                )))}
            </tbody>
        </table>
    );
}