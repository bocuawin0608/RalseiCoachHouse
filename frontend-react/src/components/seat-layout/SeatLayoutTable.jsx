export default function SeatLayoutTable({ layouts }) {
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

                        <td style={{ textAlign: 'center', display: 'flex', gap: '5px', justifyContent: 'center' }}>
                                <button style={{ cursor: 'pointer' }}>
                                    Xem chi tiết
                                </button>
                                
                                <button style={{ cursor: 'pointer' }}>
                                    Sửa thông tin
                                </button>
                                
                                <button 
                                    style={{ 
                                        cursor: 'pointer', 
                                        backgroundColor: layout.isActive ? '#ff4d4f' : '#52c41a',
                                        color: 'white',
                                        border: 'none',
                                        padding: '2px 8px',
                                        borderRadius: '4px'
                                    }}
                                >
                                    {layout.isActive ? 'Tắt layout' : 'Bật layout'}
                                </button>
                            </td>
                    </tr>
                )))}
            </tbody>
        </table>
    );
}