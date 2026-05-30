import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { tripService } from '../services/tripServices';
import '../style/HomePage.css';

const HomePage = () => {
    const navigate = useNavigate();

    const [departure, setDeparture] = useState('');
    const [destination, setDestination] = useState('');
    const [date, setDate] = useState('');
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(false);
    const [hasSearched, setHasSearched] = useState(false);
    const handleSearch = async (e) => {
        e.preventDefault();
        setLoading(true);
        setHasSearched(true);
        try {
            const searchParams = {
                route: `${departure} - ${destination}`,
                start: `${date}T00:00:00`,
                end: `${date}T23:59:59`,
                page: 0,
                size: 10
            };

            const responseData = await tripService.searchTrips(searchParams);
            if (responseData && responseData.content) {
                setTrips(responseData.content);
            } else if (Array.isArray(responseData)) {
                setTrips(responseData);
            } else {
                setTrips([]);
            }

        } catch (error) {
            console.log("Mất kết nối API Backend, kích hoạt dữ liệu giả lập (Mock Data).");
            setTrips([
                { tripId: 101, routeName: "Hà Nội - Quảng Bình", departureTime: "2026-05-28 20:00", price: 350000, type: "Limousine VIP" },
                { tripId: 102, routeName: "Hà Nội - Quảng Bình", departureTime: "2026-05-28 22:30", price: 250000, type: "Luxury Giường nằm" }
            ]);
        } finally {
            setLoading(false);
        }
    };

    const handleSelectTrip = (tripId) => {
        navigate(`/select-seat/${tripId}`);
    };

    return (
        <div className="homepage-container">
            <header className="homepage-header">
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
                <div className="header-right">
                    <div className="host-phone-number">SĐT: 0917051937</div>
                    <button className="btn-head" onClick={() => navigate('/login')}>Đăng nhập</button>
                    <button className="btn-head" onClick={() => navigate('/register')}>Đăng ký</button>
                </div>
            </header>

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

            <div className="search-form-wrapper">
                <form onSubmit={handleSearch} className="search-form">
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

            <div className="results-wrapper">
                    {!hasSearched ? (
<div className="office-section-container">
    <h3 className="office-section-title">Liên hệ</h3>

    <div className="office-grid-layout">
        
        {/* CARD 1: VĂN PHÒNG QUẢNG TRỊ */}
        <div className="office-card">
            <div className="office-card-header">
                VP Quảng Trị (Quảng Bình cũ)
            </div>
            <div className="office-card-body">
                <ul className="address-list">
                    <li><span className="location-icon">📍</span> 19A Lý Thường Kiệt, Đồng Hới, Quảng Trị</li>
                    <li><span className="location-icon">📍</span> 38 Xuân Diệu, Đồng Hới, Quảng Trị</li>
                    <li><span className="location-icon">📍</span> 23 Hùng Vương, Ba Đồn, Quảng Trị</li>
                    <li><span className="location-icon">📍</span> 105 Trần Hưng Đạo, Đồng Lê, Tuyên Hóa, Quảng Trị (Đối diện viện kiểm soát)</li>
                    <li><span className="location-icon">📍</span> Đường Nguyễn Văn Linh, Bố Trạch, Hoàn Lão, Quảng Trị</li>
                    <li><span className="location-icon">📍</span> Tổ dân phố Xuân Tiến, Thị Trấn Phong Nha, Quảng Trị</li>
                    <li><span className="location-icon">📍</span> Tổ dân phố, thị trấn Quy Đạt, huyện Minh Hóa, tỉnh Quảng Trị</li>
                    <li><span className="location-icon">📍</span> Nguyễn Tất Thành, Kiến Giang, Lệ Thủy, Quảng Trị</li>
                    <li><span className="location-icon">📍</span> Tổ dân phố 2, Thị Trấn Lệ Ninh, Mỹ Đức, Quảng Trị</li>
                    <li><span className="location-icon">📍</span> Thôn Thượng Giang, Cảnh Dương, Quảng Trị (Gần cổng chào Cảnh Dương)</li>
                    <li><span className="location-icon">📍</span> Thanh Trạch, Bố Trạch, Quảng Trị (Đường ra Cảng Gianh)</li>
                </ul>
            </div>
            <div className="office-card-footer">
                Hotline: <strong className="phone-highlight">0914.077.779 - 0963.388.388</strong>
            </div>
        </div>

        {/* CARD 2: VĂN PHÒNG HÀ NỘI */}
        <div className="office-card">
            <div className="office-card-header">
                VP Hà Nội
            </div>
            <div className="office-card-body">
                <ul className="address-list">
                    <li><span className="location-icon">📍</span> 338 Trần Khát Chân, Thanh Nhàn, Hai Bà Trưng, Hà Nội</li>
                    <li><span className="location-icon">📍</span> Nhà số 1 ngõ 2 Cổng Làng Đình Thôn, Nam Từ Liêm, Hà Nội</li>
                    <li><span className="location-icon">📍</span> Liền Kề 531 khu A Dịch Vụ Đô Lộ, Yên Nghĩa, Hà Đông, Hà Nội</li>
                    <li><span className="location-icon">📍</span> Sảnh T1 + T2 Sân bay Nội Bài</li>
                    <li><span className="location-icon">📍</span> Chợ Ninh Hiệp, Gia Lâm, Hà Nội</li>
                    <li><span className="location-icon">📍</span> 24 Ao Sào, Thịnh Liệt, Hoàng Mai, Hà Nội</li>
                </ul>
            </div>
            <div className="office-card-footer">
                Hotline: <strong className="phone-highlight">0914.077.779 - 0963.388.388</strong>
            </div>
        </div>

        {/* CARD 3: VĂN PHÒNG VINH, NGHỆ AN */}
        <div className="office-card">
            <div className="office-card-header">
                VP Vinh, Nghệ An
            </div>
            <div className="office-card-body">
                <ul className="address-list">
                    <li><span className="location-icon">📍</span> 149 Nguyễn Trãi, Phường Quán Bàu, TP. Vinh</li>
                </ul>
            </div>
            <div className="office-card-footer">
                Hotline: <strong className="phone-highlight">0914.077.779 - 0963.388.388</strong>
            </div>
        </div>

    </div>
</div>
                ) : (
                    /* KỊCH BẢN 2: USER ĐÃ BẤM TÌM KIẾM -> ĐƯA KẾT QUẢ HOẶC BÁO KHÔNG CÓ CHUYẾN */
                    <>
                        <h3 className="results-title">Kết Quả Tìm Kiếm Chuyến Xe</h3>
                        {trips.length === 0 ? (
                            <p className="no-results-msg">Không tìm thấy chuyến xe nào hợp lệ cho ngày này.</p>
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
                                            <button className="btn-select-seat">Chọn Xe →</button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </>
                )}
            </div>
            <footer className="homepage-footer">
                {/* TẦNG TRÊN: THÔNG TIN CHI TIẾT */}
                <div className="footer-main-content">

                    {/* Cột 1: Thương hiệu & Ứng dụng */}
                    <div className="footer-col col-brand">
                        <img className="footer-logo" src="/media/ralseiiii.jpg" alt="Logo Tuan MV" />
                        <h4 className="company-name-main">CÔNG TY TNHH DU LỊCH HOLA RALSEI</h4>
                        <div className="app-download-section">
                            <p className="download-title">Tải App Xe Ralsei</p>
                            <div className="app-buttons">
                                <button className="btn-app-store">🍏 App Store</button>
                                <button className="btn-google-play">🤖 Google Play</button>
                            </div>
                        </div>
                    </div>

                    {/* Cột 2: Pháp lý & Liên hệ */}
                    <div className="footer-col col-info">
                        <span className="col-badge badge-orange">CÔNG TY CHỦ QUẢN</span>
                        <h4 className="company-legal-name">CÔNG TY TNHH DU LỊCH HOLA RALSEI</h4>
                        <ul className="info-list">
                            <li>📝 <strong>M.S.D.N:</strong> 0106766690 (Ví dụ)</li>
                            <li>👤 <strong>Chịu trách nhiệm:</strong> Đoàn Ngọc Đức</li>
                            <li>📍 <strong>Địa chỉ:</strong> FPT University,Hòa Lạc, Thạch Thất, Hà Nội</li>
                            <li>📞 <strong>Điện thoại:</strong> 0917051937</li>
                            <li>🌐 <strong>Website:</strong> https://ralseicoachhouse.com</li>
                        </ul>
                    </div>

                    {/* Cột 3: Chính sách điều khoản */}
                    <div className="footer-col col-links">
                        <span className="col-badge badge-blue">THÔNG TIN CẦN BIẾT</span>
                        <ul className="policy-list">
                            <li><a href="/about">➔ Về chúng tôi</a></li>
                            <li><a href="/privacy">➔ Chính sách bảo mật (Quyền riêng tư)</a></li>
                            <li><a href="/terms">➔ Điều khoản và điều kiện sử dụng</a></li>
                            <li><a href="/dispute">➔ Chính sách và quy trình tranh chấp, khiếu nại</a></li>
                            <li><a href="/user-privacy">➔ Chính sách bảo mật thông tin người dùng</a></li>
                            <li><a href="/payment-privacy">➔ Chính sách bảo mật thông tin thanh toán</a></li>
                        </ul>
                    </div>

                </div>

                <hr className="footer-divider" />

                {/* TẦNG DƯỚI: COPYRIGHT & BỘ CÔNG THƯƠNG */}
                <div className="footer-bottom">
                    <p className="copyright-text">
                        © Bản quyền thuộc về <strong>CÔNG TY TNHH DU LỊCH HOLA RALSEI</strong>
                    </p>
                    <div className="bocongthuong-badge">
                        <a href="https://youtu.be/dQw4w9WgXcQ?si=amb8yMhNNJsFE4Y6" target="_blank" rel="noopener noreferrer">
                            <img
                                src="https://webmedia.com.vn/images/2021/09/logo-da-thong-bao-bo-cong-thuong.png"
                                alt="Đã thông báo Bộ Công Thương"
                                style={{ height: '40px' }}
                            />
                        </a>
                    </div>
                </div>
            </footer>
        </div>
    );
};

export default HomePage;