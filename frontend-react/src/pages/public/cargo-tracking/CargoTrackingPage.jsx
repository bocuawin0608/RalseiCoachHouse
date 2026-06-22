import { useState } from 'react';
import { cargoTrackingApi } from '../../../features/cargo/api/cargoTrackingApi';
import './CargoTrackingPage.css';

const statusLabels = {
    RECEIVED: 'Đã nhận hàng',
    LOADED: 'Đã lên xe',
    ARRIVED: 'Đã đến nơi',
    DELIVERED: 'Đã giao',
    CANCELLED: 'Đã hủy',
    REJECTED: 'Từ chối',
    ABANDONED: 'Bỏ hàng',
};

const StatusTimeline = ({ status }) => {
    const steps = ['RECEIVED', 'LOADED', 'ARRIVED', 'DELIVERED'];
    const currentIdx = steps.indexOf(status);

    return (
        <div className="tracking-timeline">
            {steps.map((step, i) => (
                <div key={step} className={`timeline-step ${i <= currentIdx ? 'active' : ''}`}>
                    <div className="step-dot">{i <= currentIdx ? '✓' : i + 1}</div>
                    <span className="step-label">{statusLabels[step]}</span>
                </div>
            ))}
        </div>
    );
};

const CargoTrackingPage = () => {
    const [code, setCode] = useState('');
    const [data, setData] = useState(null);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleTrack = async (e) => {
        e.preventDefault();
        if (!code.trim()) return;
        setLoading(true);
        setError('');
        setData(null);
        try {
            const res = await cargoTrackingApi.trackByCode(code.trim());
            setData(res);
        } catch (err) {
            if (err?.response?.status === 404) {
                setError('Không tìm thấy đơn hàng với mã này.');
            } else {
                setError('Có lỗi xảy ra, vui lòng thử lại sau.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="tracking-page">
            <div className="tracking-hero">
                <h1>Tra cứu đơn hàng</h1>
                <p>Nhập mã vận đơn để theo dõi hành trình giao nhận hàng hóa</p>
                <form onSubmit={handleTrack} className="tracking-form">
                    <input
                        type="text"
                        value={code}
                        onChange={(e) => setCode(e.target.value)}
                        placeholder="Nhập mã vận đơn..."
                        className="tracking-input"
                    />
                    <button type="submit" className="tracking-btn" disabled={loading}>
                        {loading ? 'Đang tra...' : 'Tra cứu'}
                    </button>
                </form>
                {error && <p className="tracking-error">{error}</p>}
            </div>

            {data && (
                <div className="tracking-result">
                    <StatusTimeline status={data.status} />

                    <div className="tracking-info-grid">
                        <div className="info-card">
                            <h3>Thông tin người gửi</h3>
                            <p><strong>Tên:</strong> {data.senderName}</p>
                            <p><strong>SĐT:</strong> {data.senderPhone}</p>
                        </div>
                        <div className="info-card">
                            <h3>Thông tin người nhận</h3>
                            <p><strong>Tên:</strong> {data.receiverName}</p>
                            <p><strong>SĐT:</strong> {data.receiverPhone}</p>
                        </div>
                        <div className="info-card">
                            <h3>Điểm giao nhận</h3>
                            <p><strong>Nơi lấy:</strong> {data.pickupStopName}</p>
                            <p><strong>Nơi trả:</strong> {data.dropoffStopName}</p>
                        </div>
                        <div className="info-card">
                            <h3>Chuyến xe</h3>
                            <p><strong>Tuyến:</strong> {data.tripRouteName || 'N/A'}</p>
                            <p><strong>Khởi hành:</strong> {data.tripDepartureTime ? new Date(data.tripDepartureTime).toLocaleString('vi-VN') : 'N/A'}</p>
                        </div>
                    </div>

                    <div className="info-card full-width">
                        <h3>Thông tin đơn hàng</h3>
                        <p><strong>Mã vận đơn:</strong> {data.ticketCode}</p>
                        <p><strong>Trạng thái:</strong> <span className="status-badge">{statusLabels[data.status] || data.status}</span></p>
                        {data.description && <p><strong>Mô tả:</strong> {data.description}</p>}
                        <p><strong>Người trả cước:</strong> {data.feePayer === 'SENDER' ? 'Người gửi' : 'Người nhận'}</p>
                        {data.codAmount > 0 && <p><strong>COD:</strong> {data.codAmount.toLocaleString('vi-VN')} đ</p>}
                        <p className="total-price"><strong>Tổng cước:</strong> {data.totalPrice.toLocaleString('vi-VN')} đ</p>
                    </div>

                    {data.items?.length > 0 && (
                        <div className="info-card full-width">
                            <h3>Chi tiết hàng hóa</h3>
                            <table className="items-table">
                                <thead>
                                    <tr>
                                        <th>Mô tả</th>
                                        <th>SL</th>
                                        <th>Kg</th>
                                        <th>Khối (m³)</th>
                                        <th>ĐVT</th>
                                        <th>Thành tiền</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {data.items.map((item, i) => (
                                        <tr key={i}>
                                            <td>{item.description || 'N/A'}</td>
                                            <td>{item.quantity}</td>
                                            <td>{item.weightKg}</td>
                                            <td>{item.dimensionVol}</td>
                                            <td>{item.unit || 'N/A'}</td>
                                            <td>{item.calculatedPrice.toLocaleString('vi-VN')} đ</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default CargoTrackingPage;
