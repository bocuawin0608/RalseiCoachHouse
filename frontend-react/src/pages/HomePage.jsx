import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { tripService } from '../services/tripServices';
// Đi lùi một tầng ra khỏi thư mục 'pages' để vào 'src', rồi đi vào 'styles'
import '../style/HomePage.css';

const HomePage = () => {
    const navigate = useNavigate();

    // Các State quản lý dữ liệu form và danh sách đổ về từ API
    const [departure, setDeparture] = useState('');
    const [destination, setDestination] = useState('');
    const [date, setDate] = useState('');
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(false);

  const handleSearch = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            // Đóng gói tham số khớp chính xác với @ModelAttribute TripSearchRequest bên Java
const searchParams = {
    route: `${departure} - ${destination}`,  // ✅ "Hà Nội - Quảng Bình"
    start: `${date}T00:00:00`,               // ✅ "2026-05-28T00:00:00"
    end: `${date}T23:59:59`,                 // ✅ "2026-05-28T23:59:59"
    page: 0,
    size: 10
};
            
            // Gọi Service mới tinh
            const responseData = await tripService.searchTrips(searchParams);
            
            // Bóc tách mảng từ PagedResponse cấu trúc Spring Data
            // Thường cấu trúc sẽ là responseData.content. Nếu không có thì dự phòng mảng rỗng.
            if (responseData && responseData.content) {
                setTrips(responseData.content);
            } else if (Array.isArray(responseData)) {
                setTrips(responseData); // Phòng hờ nếu bạn cấu trúc trả về mảng thẳng
            } else {
                setTrips([]);
            }
            
        } catch (error) {
            console.log("Mất kết nối API Backend, kích hoạt dữ liệu giả lập (Mock Data).");
            // Mock data cấu trúc giống hệt TripDetailProjection để bạn test UI không bị chết
            setTrips([
                { tripId: 101, routeName: "Hà Nội - Quảng Bình", departureTime: "2026-05-28 20:00", price: 350000, type: "Limousine VIP" },
                { tripId: 102, routeName: "Hà Nội - Quảng Bình", departureTime: "2026-05-28 22:30", price: 250000, type: "Luxury Giường nằm" }
            ]);
        } finally {
            setLoading(false);
        }
    };

    // Điều hướng sang trang chọn chỗ ngồi kèm theo ID chuyến xe
    const handleSelectTrip = (tripId) => {
        navigate(`/seat-layout/${tripId}`);
    };

    return (
        <div className="homepage-container">
            {/* Thanh Header phân cụm chuẩn cấu trúc như Hưng Long */}
            <header className="homepage-header">
                {/* Cụm bên trái: Logo + Hệ thống Router chính */}
                <div className="header-left">
                    <img
                        className="logo-img"
                        src="/media/ralseiiii.jpg"
                        alt="Logo Ralsei"
                        onError={(e) => {
                            e.target.src = "https://placehold.co/150x150/2ecc71/ffffff?text=Ralsei+Logo";
                        }}
                    />
                    <button className="btn-head" onClick={() => navigate('/')}>Trang chủ</button>
                    <button className="btn-head" onClick={() => navigate('/tra-cuu')}>Tra cứu đơn</button>
                    <button className="btn-head" onClick={() => navigate('/thue-xe')}>Thuê xe</button>
                    <button className="btn-head" onClick={() => navigate('/tin-tuc')}>Tin tức</button>
                </div>

                {/* Cụm bên phải: SĐT và Đăng nhập/Đăng ký */}
                <div className="header-right">
                    <div className="host-phone-number">SĐT: 0917051937</div>
                    <button className="btn-head" onClick={() => navigate('/login')}>Đăng nhập</button>
                    <button className="btn-head" onClick={() => navigate('/register')}>Đăng ký</button>
                </div>
            </header>

            {/* Khối hiển thị ảnh Banner nền của hệ thống */}
            <div className="buddha-image-wrapper">
                <a 
                    href="https://docs.google.com/forms/d/e/1FAIpQLSeMSgtuTpv6P_T4SrOCv1S-HiuSjsoiWA22G70SSjRVGGtKGQ/viewform?usp=publish-editor"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    <img
                        className="buddha-img"
                        src="/media/RalseiWallpaper.jpg"
                        alt="Ralsei Banner"
                        onError={(e) => {
                            e.target.src = "https://placehold.co/800x200/2ecc71/ffffff?text=Ralsei+Banner";
                        }}
                    />
                </a>
            </div>

            {/* Khối Form Tìm Kiếm Phân Tầng */}
            <div className="search-form-wrapper">
                <form onSubmit={handleSearch} className="search-form">
                    
                    {/* TẦNG 1: Loại vé & Hướng dẫn */}
                    <div className="form-tier-top">
                        <div className="radio-group">
                            <label className="radio-label">
                                <input type="radio" name="trip-type" defaultChecked />
                                <span className="radio-checkmark"></span> Một chiều
                            </label>
                            <label className="radio-label">
                                <input type="radio" name="trip-type" />
                                <span className="radio-checkmark"></span> Khứ hồi
                            </label>
                        </div>
                        <div className="policy-links">
                            <a href="#guide">Hướng dẫn đặt lịch trình</a>
                            <span className="v-divider">|</span>
                            <a href="#policy">Quy định chung</a>
                        </div>
                    </div>

                    {/* TẦNG 2: Các ô nhập liệu hàng ngang */}
                    <div className="form-tier-main">
                        <div className="input-block">
                            <span className="input-icon">🟢</span>
                            <div className="input-field-wrapper">
                                <label>Điểm đi</label>
                                <input type="text" value={departure} onChange={(e) => setDeparture(e.target.value)} placeholder="Nhập điểm đi" required />
                            </div>
                        </div>

                        <button type="button" className="btn-swap">⇄</button>

                        <div className="input-block">
                            <span className="input-icon">🔴</span>
                            <div className="input-field-wrapper">
                                <label>Điểm đến</label>
                                <input type="text" value={destination} onChange={(e) => setDestination(e.target.value)} placeholder="Nhập điểm đến" required />
                            </div>
                        </div>

                        <div className="input-block date-block">
                            <span className="input-icon">📅</span>
                            <div className="input-field-wrapper">
                                <label>Ngày đi</label>
                                <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
                            </div>
                        </div>

                        <button type="submit" className="btn-search-submit">
                            {loading ? 'Đang quét...' : 'Tìm lịch trình'}
                        </button>
                    </div>

                    {/* TẦNG 3: Tìm kiếm gần đây */}
                    <div className="form-tier-recent">
                        <span className="recent-title">Tìm kiếm gần đây</span>
                        <div className="recent-cards-container">
                            <div className="recent-card">
                                <span className="history-icon">🕒</span>
                                <div className="recent-info">
                                    <strong>Thành phố Hà Nội - Tỉnh Quảng Bình</strong>
                                    <p>30/05/2026 ➔ 30/06/2026</p>
                                    <span className="badge-khu-hoi">Khứ hồi</span>
                                </div>
                            </div>
                            <div className="recent-card">
                                <span className="history-icon">🕒</span>
                                <div className="recent-info">
                                    <strong>Thành phố Hà Nội - Tỉnh Quảng Bình</strong>
                                    <p>25/05/2026 ➔ 30/06/2026</p>
                                    <span className="badge-khu-hoi">Khứ hồi</span>
                                </div>
                            </div>
                        </div>
                    </div>

                </form>
            </div>

            {/* Khối Hiển Thị Kết Quả */}
            <div className="results-wrapper">
                <h3 className="results-title">Kết Quả Tìm Kiếm Chuyến Xe</h3>

                {trips.length === 0 ? (
                    <p className="no-results-msg">A di đà phật. Không tìm thấy chuyến xe nào hợp lệ.</p>
                ) : (
                    <div className="trips-list">
                        {trips.map((trip) => (
                            <div key={trip.tripId} className="trip-card" onClick={() => handleSelectTrip(trip.tripId)}>
                                <div className="trip-info">
                                    <h4>{trip.routeName || `${departure} - ${destination}`}</h4>
                                    <p>Khởi hành: <strong>{trip.departureTime}</strong></p>
                                    <span className="trip-type-tag">{trip.type || 'Standard'}</span>
                                </div>
                                <div className="trip-price-wrapper">
                                    <span className="trip-price">{(trip.price || 0).toLocaleString('vi-VN')} đ</span>
                                    <button className="btn-select-seat">Chọn Ghế →</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default HomePage;