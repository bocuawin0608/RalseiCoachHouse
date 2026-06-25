import { Alert, Card, Button } from 'react-bootstrap';
import { MdCheckCircle } from 'react-icons/md';

export default function Step3Payment({ tripId, seatIds, passengerInfo, holdToken }) {
    
    // Tại đây ông sẽ gọi 1 API kiểu: bookingApi.createTicket(tripId, data, holdToken)
    // Nếu thành công thì hiển thị QR hoặc thông báo.

    return (
        <div className="text-center py-4">
            <MdCheckCircle size={80} color="var(--ralsei-green)" className="mb-3" />
            <h3 className="fw-bold mb-4">Xác nhận thông tin</h3>
            
            <Alert variant="success" className="d-inline-block text-start mb-4">
                <strong>Hành khách:</strong> {passengerInfo.fullName} <br/>
                <strong>Số điện thoại:</strong> {passengerInfo.phone} <br/>
                <strong>Số lượng ghế:</strong> {seatIds.length} <br/>
                <hr/>
                <em>Hệ thống đang chờ thanh toán... (Tích hợp VNPay/Momo ở đây)</em>
            </Alert>
            
            <div>
                <Button 
                    className="fw-bold px-5 py-2 border-0"
                    style={{ backgroundColor: 'var(--ralsei-pink)', color: 'white' }}
                >
                    Xác nhận thanh toán
                </Button>
            </div>
        </div>
    );
}