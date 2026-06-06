import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

const SelectSeatPage = () => {
    const { tripId } = useParams();
    const navigate = useNavigate();

    // Thông tin giả lập về chuyến xe dựa trên ID
    const [tripInfo, setTripInfo] = useState({
        routeName: "Hà Nội - Quảng Bình",
        departureTime: "2026-05-28 20:00",
        price: 350000,
        type: "Limousine VIP 2 Tầng"
    });

    // Danh sách ghế được chọn
    const [selectedSeats, setSelectedSeats] = useState([]);

    // Giả lập trạng thái ghế từ cơ sở dữ liệu: trống (available), đã đặt (booked)
    // Xe Limousine 2 tầng, mỗi tầng 3 dãy (A, B, C)
    const [seatsData, setSeatsData] = useState({
        tang1: [
            { id: "A01", status: "available" }, { id: "B01", status: "booked" }, { id: "C01", status: "available" },
            { id: "A02", status: "available" }, { id: "B02", status: "available" }, { id: "C02", status: "booked" },
            { id: "A03", status: "available" }, { id: "B03", status: "available" }, { id: "C03", status: "available" },
            { id: "A04", status: "booked" },    { id: "B04", status: "available" }, { id: "C04", status: "available" },
            { id: "A05", status: "available" }, { id: "B05", status: "available" }, { id: "C05", status: "available" }
        ],
        tang2: [
            { id: "A06", status: "available" }, { id: "B06", status: "available" }, { id: "C06", status: "available" },
            { id: "A07", status: "available" }, { id: "B07", status: "booked" }, { id: "C07", status: "available" },
            { id: "A08", status: "available" }, { id: "B08", status: "available" }, { id: "C08", status: "available" },
            { id: "A09", status: "available" }, { id: "B09", status: "available" }, { id: "C09", status: "booked" },
            { id: "A10", status: "available" }, { id: "B10", status: "available" }, { id: "C10", status: "available" }
        ]
    });

    const handleSeatClick = (seatId, status) => {
        if (status === "booked") return; 
        if (selectedSeats.includes(seatId)) {
            setSelectedSeats(selectedSeats.filter(seat => seat !== seatId));
        } else {
            // Nếu chưa chọn thì  vào danh sách
            setSelectedSeats([...selectedSeats, seatId]);
        }
    };

    const totalAmount = selectedSeats.length * tripInfo.price;

    const handleProceedPayment = () => {
        if (selectedSeats.length === 0) {
            alert("Vui lòng chọn ít nhất một ghế để tiếp tục hành trình!");
            return;
        }
        alert(`Đã xác nhận chọn các ghế: ${selectedSeats.join(', ')}. Chuyển hướng thanh toán...`);
    };

    return (
        <div className="seat-layout-container">
            {/* Thanh điều hướng thông tin chuyến xe */}
            <div className="layout-header">
                <button className="btn-back" onClick={() => navigate('/home')}>← Quay lại</button>
                <div className="trip-summary">
                    <h2>{tripInfo.routeName}</h2>
                    <p>Loại xe: <strong>{tripInfo.type}</strong> | Khởi hành: <strong>{tripInfo.departureTime}</strong></p>
                </div>
            </div>

            <div className="layout-main-content">
                <div className="seats-legend">
                    <div className="legend-item"><span className="legend-box seat-available"></span> Còn trống</div>
                    <div className="legend-item"><span className="legend-box seat-selecting"></span> Đang chọn</div>
                    <div className="legend-item"><span className="legend-box seat-booked"></span> Đã có người</div>
                </div>

                <div className="map-bus-wrapper">
                    
                    {/* TẦNG 1 */}
                    <div className="deck-column">
                        <h4 className="deck-title">TẦNG DƯỚI (TẦNG 1)</h4>
                        <div className="bus-grid">
                            {seatsData.tang1.map((seat) => {
                                let seatClass = "seat-box seat-available";
                                if (seat.status === "booked") seatClass = "seat-box seat-booked";
                                if (selectedSeats.includes(seat.id)) seatClass = "seat-box seat-selecting";

                                return (
                                    <div 
                                        key={seat.id} 
                                        className={seatClass}
                                        onClick={() => handleSeatClick(seat.id, seat.status)}
                                    >
                                        {seat.id}
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    {/* TẦNG 2 */}
                    <div className="deck-column">
                        <h4 className="deck-title">TẦNG TRÊN (TẦNG 2)</h4>
                        <div className="bus-grid">
                            {seatsData.tang2.map((seat) => {
                                let seatClass = "seat-box seat-available";
                                if (seat.status === "booked") seatClass = "seat-box seat-booked";
                                if (selectedSeats.includes(seat.id)) seatClass = "seat-box seat-selecting";

                                return (
                                    <div 
                                        key={seat.id} 
                                        className={seatClass}
                                        onClick={() => handleSeatClick(seat.id, seat.status)}
                                    >
                                        {seat.id}
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                </div>
            </div>

            {/* THANH THANH TOÁN DƯỚI ĐÁY TRANG (STICKY BOTTOM BAR) */}
            <div className="payment-sticky-bar">
                <div className="bar-left-info">
                    <div className="info-row">
                        <span>Ghế đã chọn:</span>
                        <strong className="text-highlight">
                            {selectedSeats.length > 0 ? selectedSeats.join(', ') : "Chưa chọn"}
                        </strong>
                    </div>
                    <div className="info-row">
                        <span>Tổng tiền:</span>
                        <strong className="price-highlight">{totalAmount.toLocaleString('vi-VN')} đ</strong>
                    </div>
                </div>
                <button className="btn-submit-booking" onClick={handleProceedPayment}>
                    Tiếp tục đặt vé →
                </button>
            </div>
        </div>
    );
};

export default SelectSeatPage;