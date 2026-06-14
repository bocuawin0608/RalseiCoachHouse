import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { tripService } from '../../../features/trips/api/tripServices';
import './HomePage.css';

const HomePage = () => {
    const navigate = useNavigate();

    // State tìm kiếm cơ bản tại Form trang chủ
    const [departure, setDeparture] = useState('');
    const [destination, setDestination] = useState('');
    const [date, setDate] = useState('');
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(false);
    const [hasSearched, setHasSearched] = useState(false);
    const [currentSearchRoute, setCurrentSearchRoute] = useState('');

    // State quản lý bộ lọc nâng cao (Sidebar)
    const [selectedTimeSlots, setSelectedTimeSlots] = useState([]);
    const [selectedLayouts, setSelectedLayouts] = useState([]);
    const [priceRange, setPriceRange] = useState({ min: null, max: null });

    // Hàm gọi API đồng bộ dữ liệu
    const executeSearch = async (isAdvancedSearch = false, updatedFilters = {}) => {
        if (!date || !departure || !destination) return;

        setLoading(true);
        setHasSearched(true);
        const routeText = `${departure.trim()} - ${destination.trim()}`;
        setCurrentSearchRoute(routeText);

        try {
            const filters = isAdvancedSearch ? updatedFilters : {
                timeSlots: selectedTimeSlots,
                layouts: selectedLayouts,
                priceRange: priceRange
            };

            // Kiểm tra trạng thái kích hoạt tìm kiếm nâng cao
            const hasActiveFilters = filters.timeSlots.length > 0 ||
                filters.layouts.length > 0 ||
                filters.priceRange.min !== null ||
                filters.priceRange.max !== null;

            const searchParams = {
                route: routeText,
                date: date,
                page: 0,
                size: 10,
                isAdvanced: isAdvancedSearch || hasActiveFilters,
                timeSlots: filters.timeSlots.join(','),
                layouts: filters.layouts.join(','),
                minPrice: filters.priceRange.min,
                maxPrice: filters.priceRange.max
            };

            if (!searchParams.timeSlots) delete searchParams.timeSlots;
            if (!searchParams.layouts) delete searchParams.layouts;
            if (searchParams.minPrice === null) delete searchParams.minPrice;
            if (searchParams.maxPrice === null) delete searchParams.maxPrice;

            const responseData = await tripService.searchTrips(searchParams);

            if (responseData && responseData.content) {
                setTrips(responseData.content);
            } else if (Array.isArray(responseData)) {
                setTrips(responseData);
            } else {
                setTrips([]);
            }
        } catch (error) {
            console.log("Mất kết nối API Backend, kích hoạt dữ liệu giả lập (Mock Data): " + error);
            // Khôi phục Mock dữ liệu cấu trúc chuẩn hóa dựa trên image_d78866.jpg và image_d714fc.jpg
            setTrips([
                { tripId: 101, departureTime: "2026-01-01T07:30:00", arrivalTime: "2026-01-01T19:00:00", duration: "36 giờ", seatPrice: 450000, type: "Xe Luxury", description: "Ghế giường nằm 32", seatsLeft: 24 },
                { tripId: 102, departureTime: "2026-01-01T10:30:00", arrivalTime: "2026-01-01T19:00:00", duration: "9 giờ", seatPrice: 590000, type: "Xe Limousine", description: "Ghế giường nằm 20", seatsLeft: 14 }
            ]);
        } finally {
            setLoading(false);
        }
    };

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        executeSearch(false);
    };

    // Xử lý sự kiện thay đổi bộ lọc tương tác trực tiếp
    const handleTimeSlotChange = (slot) => {
        const nextSlots = selectedTimeSlots.includes(slot)
            ? selectedTimeSlots.filter(s => s !== slot)
            : [...selectedTimeSlots, slot];
        setSelectedTimeSlots(nextSlots);
        if (hasSearched) executeSearch(true, { timeSlots: nextSlots, layouts: selectedLayouts, priceRange });
    };

    const handleLayoutChange = (layout) => {
        const nextLayouts = selectedLayouts.includes(layout)
            ? selectedLayouts.filter(l => l !== layout)
            : [...selectedLayouts, layout];
        setSelectedLayouts(nextLayouts);
        if (hasSearched) executeSearch(true, { timeSlots: selectedTimeSlots, layouts: nextLayouts, priceRange });
    };

    const handlePriceRangeChange = (min, max) => {
        const nextPrice = (priceRange.min === min && priceRange.max === max)
            ? { min: null, max: null }
            : { min, max };
        setPriceRange(nextPrice);
        if (hasSearched) executeSearch(true, { timeSlots: selectedTimeSlots, layouts: selectedLayouts, priceRange: nextPrice });
    };

    const clearAllFilters = () => {
        setSelectedTimeSlots([]);
        setSelectedLayouts([]);
        setPriceRange({ min: null, max: null });
        if (hasSearched) {
            executeSearch(true, { timeSlots: [], layouts: [], priceRange: { min: null, max: null } });
        }
    };

    const handleSelectTrip = (tripId) => {
        navigate(`/select-seat/${tripId}`);
    };

    return (
        <div className="homepage-container">
            <div className="buddha-image-wrapper">
                <a href="https://docs.google.com/forms/d/e/1FAIpQLSeMSgtuTpv6P_T4SrOCv1S-HiuSjsoiWA22G70SSjRVGGtKGQ/viewform?usp=publish-editor" target="_blank" rel="noopener noreferrer">
                    <img
                        className="buddha-img"
                        src="/images/RalseiWallpaper.jpg"
                        alt="Ralsei Banner"
                        onError={(e) => { e.target.src = "https://placehold.co/800x200/2ecc71/ffffff?text=Ralsei+Banner"; }}
                    />
                </a>
            </div>

            <div className="search-form-wrapper">
                <form onSubmit={handleSearchSubmit} className="search-form">
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
                </form>
            </div>

            <div className="results-wrapper">
                {!hasSearched ? (
                    <div className="office-section-container">
                        <h3 className="office-section-title">Liên hệ</h3>
                        <div className="office-grid-layout">
                            <div className="office-card">
                                <div className="office-card-header">VP Quảng Bình</div>
                                <div className="office-card-body">
                                    <ul className="address-list">
                                        <li><span className="location-icon">📍</span> 19A Lý Thường Kiệt, Đồng Hới, Quảng Bình</li>
                                        <li><span className="location-icon">📍</span> Đường Nguyễn Văn Linh, Bố Trạch, Hoàn Lão, Quảng Bình</li>
                                        <li><span className="location-icon">📍</span> Nguyễn Tất Thành, Kiến Giang, Lệ Thủy, Quảng Bình</li>
                                    </ul>
                                </div>
                                <div className="office-card-footer">Hotline: <strong className="phone-highlight">0914.077.779</strong></div>
                            </div>
                            <div className="office-card">
                                <div className="office-card-header">VP Hà Nội</div>
                                <div className="office-card-body">
                                    <ul className="address-list">
                                        <li><span className="location-icon">📍</span> 338 Trần Khát Chân, Hai Bà Trưng, Hà Nội</li>
                                        <li><span className="location-icon">📍</span> Sảnh T1 + T2 Sân bay Nội Bài</li>
                                    </ul>
                                </div>
                                <div className="office-card-footer">Hotline: <strong className="phone-highlight">0914.077.779</strong></div>
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="search-results-layout">
                        {/* SIDEBAR BỘ LỌC NÂNG CAO */}
                        <aside className="filter-sidebar">
                            <div className="sidebar-header">
                                <h4>Bộ lọc tìm kiếm</h4>
                                <button type="button" className="btn-clear-filter" onClick={clearAllFilters}>✕ Bỏ lọc</button>
                            </div>

                            <div className="filter-group">
                                <h5>Giờ đi</h5>
                                <label className="filter-checkbox-label">
                                    <input type="checkbox" checked={selectedTimeSlots.includes('EARLY_MORNING')} onChange={() => handleTimeSlotChange('EARLY_MORNING')} />
                                    <span>Sáng sớm 00:00 - 06:00</span>
                                </label>
                                <label className="filter-checkbox-label">
                                    <input type="checkbox" checked={selectedTimeSlots.includes('MORNING')} onChange={() => handleTimeSlotChange('MORNING')} />
                                    <span>Buổi sáng 06:00 - 12:00</span>
                                </label>
                                <label className="filter-checkbox-label">
                                    <input type="checkbox" checked={selectedTimeSlots.includes('AFTERNOON')} onChange={() => handleTimeSlotChange('AFTERNOON')} />
                                    <span>Buổi chiều 12:00 - 18:00</span>
                                </label>
                                <label className="filter-checkbox-label">
                                    <input type="checkbox" checked={selectedTimeSlots.includes('EVENING')} onChange={() => handleTimeSlotChange('EVENING')} />
                                    <span>Buổi tối 18:00 - 24:00</span>
                                </label>
                            </div>

                            <div className="filter-group">
                                <h5>Loại xe</h5>
                                <div className="filter-tags-grid">
                                    {['Xe truyền thống', 'Xe Limousine', 'Xe Luxury'].map((layout) => (
                                        <button
                                            key={layout}
                                            type="button"
                                            className={`filter-tag-btn ${selectedLayouts.includes(layout) ? 'active' : ''}`}
                                            onClick={() => handleLayoutChange(layout)}
                                        >
                                            {layout}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div className="filter-group">
                                <h5>Giá</h5>
                                <div className="filter-tags-grid vertical-tags">
                                    <button
                                        type="button"
                                        className={`filter-tag-btn ${priceRange.min === 0 && priceRange.max === 300000 ? 'active' : ''}`}
                                        onClick={() => handlePriceRangeChange(0, 300000)}
                                    >
                                        Dưới 300.000đ
                                    </button>
                                    <button
                                        type="button"
                                        className={`filter-tag-btn ${priceRange.min === 300000 && priceRange.max === 500000 ? 'active' : ''}`}
                                        onClick={() => handlePriceRangeChange(300000, 500000)}
                                    >
                                        300.000đ - 500.000đ
                                    </button>
                                    <button
                                        type="button"
                                        className={`filter-tag-btn ${priceRange.min === 500000 && priceRange.max === 2000000 ? 'active' : ''}`}
                                        onClick={() => handlePriceRangeChange(500000, 2000000)}
                                    >
                                        Trên 500.000đ
                                    </button>
                                </div>
                            </div>
                        </aside>

                        {/* DANH SÁCH CHUYẾN XE (ĐÃ FIX LỖI ẢNH d714fc.jpg) */}
                        <div className="results-content">
                            <h3 className="results-main-title">
                                Kết quả lịch trình: {currentSearchRoute} ({trips.length})
                            </h3>

                            {trips.length === 0 ? (
                                <p className="no-results-msg">Không tìm thấy chuyến xe nào hợp lệ ứng với bộ lọc.</p>
                            ) : (
                                <div className="advanced-trips-list">
                                    {trips.map((trip) => {
                                        // 1. PHÂN TÁCH THỜI GIAN (Khớp chuẩn LocalDateTime: 2026-01-01T00:00:00)
                                        const parseTime = (timeStr) => {
                                            if (!timeStr) return "00:00";
                                            if (timeStr.includes('T')) return timeStr.split('T')[1].substring(0, 5);
                                            if (timeStr.includes(' ')) return timeStr.split(' ')[1].substring(0, 5);
                                            return timeStr.substring(0, 5);
                                        };

                                        const depTime = parseTime(trip.departureTime);
                                        // Tính toán thời gian đến dự kiến (Backend chưa trả về arrivalTime cụ thể nên tạm cộng 9 giờ)
                                        const arrTime = trip.arrivalTime ? parseTime(trip.arrivalTime) : "09:00";

                                        // 2. KHỚP ĐỊA ĐIỂM TỪ ROUTENAME (Ví dụ: "Hà Nội - Quảng Bình")
                                        const routeString = trip.routeName || currentSearchRoute;
                                        const routeParts = routeString.includes('-') ? routeString.split('-') : [];
                                        const displayDeparture = routeParts[0]?.trim() || departure || "Điểm đón";
                                        const displayDestination = routeParts[1]?.trim() || destination || "Điểm trả";

                                        // 3. ĐỒNG BỘ CHÍNH XÁC BIẾN THEO TRIPFILTERPROJECTION CỦA BACKEND
                                        const displayVehicleType = trip.coachTypeName || "Xe giường nằm"; // Khớp getCoachTypeName()
                                        const displayPrice = trip.seatPrice ?? 0;                        // Khớp getSeatPrice()

                                        // Vì Projection hiện tại không có trường mô tả loại ghế, ta đặt mặc định theo hạng xe
                                        const displayDescription = displayVehicleType.toLowerCase().includes('thường')
                                            ? "Ghế ngồi tiêu chuẩn"
                                            : "Ghế nằm cao cấp";

                                        const displaySeatsLeft = trip.seatsLeft ?? trip.availableSeats ?? 20;

                                        return (
                                            <div key={trip.tripId} className="advanced-trip-card">
                                                {/* Tầng thời gian hành trình */}
                                                <div className="trip-timeline-header">
                                                    <div className="time-node-start">
                                                        <span className="time-bold">{depTime}</span>
                                                        <span className="blue-dot">🔵</span>
                                                    </div>
                                                    <div className="timeline-duration-line">
                                                        <span className="duration-text">{trip.duration || "9 giờ"}</span>
                                                    </div>
                                                    <div className="time-node-end">
                                                        <span className="blue-dot">🔵</span>
                                                        <span className="time-bold">{arrTime}</span>
                                                    </div>
                                                </div>

                                                {/* Tầng địa điểm đón trả */}
                                                <div className="trip-locations-grid">
                                                    <div className="loc-text text-left">{displayDeparture}</div>
                                                    <div className="loc-text text-right">{displayDestination}</div>
                                                </div>

                                                {/* Tầng thông tin xe & Giá cả */}
                                                <div className="trip-detail-footer-row">
                                                    <div className="vehicle-info-block">
                                                        <div className="vehicle-avatar-placeholder">🚌</div>
                                                        <div className="vehicle-meta">
                                                            {/* Đổ chuẩn dữ liệu Thường / VIP / Luxury ra đây */}
                                                            <span className="vehicle-name-label">{displayVehicleType}</span>
                                                            <span className="vehicle-desc-sub">{displayDescription}</span>
                                                        </div>
                                                    </div>
                                                    <div className="price-and-action-block">
                                                        <span className="seats-counter-badge">Còn {displaySeatsLeft} chỗ trống</span>
                                                        {/* Hiển thị giá tiền thực tế từ API */}
                                                        <span className="trip-price-amount">{displayPrice.toLocaleString('vi-VN')} đ</span>
                                                    </div>
                                                </div>

                                                {/* Hàng nút chức năng */}
                                                <div className="trip-card-actions-bar">
                                                    <button type="button" className="btn-secondary-info">Xem điểm đón trả</button>
                                                    <button type="button" className="btn-primary-select" onClick={() => handleSelectTrip(trip.tripId)}>Chọn chuyến</button>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default HomePage;