import { useState, useEffect, useCallback, useMemo } from 'react';
import { Row, Col, Alert, Spinner } from 'react-bootstrap';
import { bookingApi } from '../api/bookingApi';
import { BsExclamationTriangleFill } from "react-icons/bs";
import { useDispatch, useSelector } from 'react-redux';
import { setSelectedSeats, setStep } from '../reducers/bookingSlice';
import SeatMapBuilder from './SeatMapBuilder';
import SeatIcon from '../../../components/common/SeatIcon';
import { formatCurrency } from '../../../utils/formatters';

export default function Step1SeatSelection({ tripId }) {
    const [seatList, setSeatList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isLocking, setIsLocking] = useState(false);

    const dispatch = useDispatch();
    const { holdToken, selectedSeats, tripInfo } = useSelector(state => state.booking);

    const fetchInitialData = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const res = await bookingApi.getSeatMap(tripId);
            setSeatList(res);
        } catch(error) {
            setError(error.response?.data?.message || "Có lỗi xảy ra khi lấy dữ liệu ghế. Vui lòng tải lại trang!")
        } finally {
            setLoading(false);
        }
    }, [tripId]);

    useEffect(() => {
        const load = async () => fetchInitialData();
        load();
    }, [fetchInitialData]);

    const toggleSeat = (seat) => {
        if (seat.status !== 'AVAILABLE') return;
        
        if (selectedSeats.map(seatObj => seatObj.tripSeatId).includes(seat.tripSeatId)) {
            dispatch(setSelectedSeats(selectedSeats.filter(seatObj => seatObj.tripSeatId !== seat.tripSeatId)));
        } else {
            if (selectedSeats.length >= 10) {
                alert("Chỉ được chọn tối đa 10 ghế!");
                return;
            }
            dispatch(setSelectedSeats([...selectedSeats, {tripSeatId: seat.tripSeatId, seatCode: seat.seatCode}]));
        }
    };

    const handleContinue = async () => {
        if (selectedSeats.length === 0) return;
        setIsLocking(true);
        try {
            await bookingApi.lockSeats(tripId, { tripSeatIds: selectedSeats.map(seatObj => seatObj.tripSeatId) }, holdToken);
            dispatch(setStep(2));
        } catch (err) {
            setError(err.response?.data?.message || "Ghế đã được đặt hoặc lỗi hệ thống. Vui lòng tải lại trang!");
            await fetchInitialData();
            dispatch(setSelectedSeats([]));
        } finally {
            setIsLocking(false);
        }
    };

    const parsedLayout = useMemo(() => {
        if(!seatList || seatList.length == 0) {
            return {floors: [], cols: 0, rows:0};
        }

        let maxFloor = 0;
        let maxRow = 0;
        let maxCol = 0;

        seatList.forEach(seat => {
            if(seat.floorIndex > maxFloor) maxFloor = seat.floorIndex;
            if(seat.rowIndex > maxRow) maxRow = seat.rowIndex;
            if(seat.colIndex > maxCol) maxCol = seat.colIndex;
        });

        const floorMatrix = Array(maxFloor).fill().map(() => Array(maxRow).fill().map(() => Array(maxCol).fill(null)));
        seatList.forEach(seat => {
            floorMatrix[seat.floorIndex-1][seat.rowIndex-1][seat.colIndex-1] = seat;
        })

        return {
            floors: floorMatrix,
            cols: maxCol,
            rows: maxRow
        }
    }, [seatList]);

    if (loading) return <div className="text-center py-5"><Spinner animation="border" /></div>;

    return (
        <div>
            {error && (
                <Alert variant='danger' className="mb-4 py-3 px-3 d-flex align-items-center gap-2 rounded-3">
                    <BsExclamationTriangleFill />
                    {error}
                </Alert>
            )}
            
            <Row className="g-4">
                
                <Col lg={9} md={8} className="d-flex justify-content-center border-end-md pe-lg-4">
                    <div className="d-flex flex-wrap justify-content-center gap-5 w-100">
                        {parsedLayout && parsedLayout.floors ? (
                            parsedLayout.floors.map((_, index) => (
                                <div key={index} className="floor-container text-center flex-grow-0">
                                    <p className="mb-3 text-muted fw-semibold" style={{ fontSize: '14px' }}>
                                        {index === 0 ? "Tầng 1" : "Tầng 2"}
                                    </p>
                                    
                                    <div className="py-2">
                                        <SeatMapBuilder 
                                            rows={parsedLayout.rows}
                                            cols={parsedLayout.cols}
                                            initialMatrix={parsedLayout.floors[index]}
                                            selectedSeatIds={selectedSeats.map(seatObj => seatObj.tripSeatId)}
                                            onSeatClick={toggleSeat}
                                        />
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className="text-center py-5 w-100">
                                <p className="text-muted mb-0 fst-italic">Xin lỗi vì sự cố. Vui lòng liên hệ nhà xe!</p>
                            </div>
                        )}
                    </div>
                </Col>
                
                <Col lg={3} md={4} className="ps-md-4 d-flex flex-column justify-content-start border-start">
                    <div className="d-flex flex-column gap-3 pt-2">
                        <div className="d-flex align-items-center gap-3" style={{ fontSize: '14px', color: '#4B5563' }}>
                            <SeatIcon status="AVAILABLE" code="" /> <span>Còn trống</span>
                        </div>
                        <div className="d-flex align-items-center gap-3" style={{ fontSize: '14px', color: '#4B5563' }}>
                            <SeatIcon status="SELECTED" code="" /> <span>Đang chọn</span>
                        </div>
                        <div className="d-flex align-items-center gap-3" style={{ fontSize: '14px', color: '#4B5563' }}>
                            <SeatIcon status="SOLD" code="" /> <span>Đã đặt</span>
                        </div>
                    </div>
                    
                    {selectedSeats.length > 0 && (
                        <div className="mt-4 pt-3 border-top" style={{ fontSize: '14px' }}>
                            <div><strong>Đang chọn:</strong> {selectedSeats.length} ghế</div>
                            {tripInfo?.seatPrice > 0 && (
                                <div className="mt-2">
                                    <strong>Giá tạm tính:</strong> {selectedSeats.length} x {formatCurrency(tripInfo.seatPrice)}
                                </div>
                            )}
                            <div className="text-muted mt-1" style={{ fontSize: '12px' }}>
                                Chưa bao gồm phụ thu điểm đón/trả và giảm giá
                            </div>
                        </div>
                    )}

                    <div className="d-flex justify-content-center mt-5 pt-2">
                        <button 
                            className="fw-bold border-0 px-5 py-2 rounded-pill shadow-sm booking-btn-general" 
                            
                            disabled={selectedSeats.length === 0 || isLocking}
                            onClick={handleContinue}
                        >
                            {isLocking ? <Spinner size="sm" animation="border" /> : 'Tiếp tục'}
                        </button>
                    </div>
                </Col>
            </Row>
        </div>
    );
}