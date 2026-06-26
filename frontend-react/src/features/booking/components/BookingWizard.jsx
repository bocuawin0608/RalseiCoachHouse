import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from 'react-bootstrap';
import { useSelector, useDispatch } from 'react-redux';
import Step1SeatSelection from './Step1SeatSelection';
import Step2PassengerInfo from './Step2PassengerInfo';
import Step3Payment from './Step3Payment';
import { bookingApi } from '../api/bookingApi';
import { resetBooking, setStep } from '../reducers/bookingSlice'; 
import axiosClient from '../../../api/axiosClient';

export default function BookingWizard({tripId}) {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const { step, holdToken, selectedSeats } = useSelector((state) => state.booking);

    const latestStateRef = useRef({ selectedSeats, step, holdToken, tripId });

    useEffect(() => {
        latestStateRef.current = { selectedSeats, step, holdToken, tripId };
    }, [selectedSeats, step, holdToken, tripId]);

    useEffect(() => {
        const handleBeforeUnload = () => {
            const { selectedSeats: seats, holdToken: token, tripId: id } = latestStateRef.current;
            
            if (seats.length > 0) {
                const baseUrl = axiosClient.defaults.baseURL || '';
                const fullUrl = `${baseUrl}/v1/bookings/trips/${id}/seats/release/beacon`;
                
                const params = new URLSearchParams({ session: token });
                navigator.sendBeacon(fullUrl, params);
            }
        };

        window.addEventListener('beforeunload', handleBeforeUnload);
        window.addEventListener('pagehide', handleBeforeUnload);
        
        return () => {
            window.removeEventListener('beforeunload', handleBeforeUnload);
            window.removeEventListener('pagehide', handleBeforeUnload);

            const { selectedSeats: seats, step: currentStep, holdToken: token, tripId: id } = latestStateRef.current;
            
            if (seats.length > 0 && currentStep !== 3) {
                bookingApi.releaseSeats(id, { tripSeatIds: seats.map(seatObj => seatObj.tripSeatId) }, token).catch(() => {});
            }
            
            dispatch(resetBooking()); 
        };
    }, [dispatch]);

    const handleBack = async () => {
        if (step > 1) {
            if(step === 2) {
                try {
                    await bookingApi.releaseSeats(tripId, {tripSeatIds: selectedSeats.map(seatObj => seatObj.tripSeatId)}, holdToken);
                } catch(error) {
                    console.error("Lỗi khi giải phóng ghế:" + error.response?.data?.message);
                }
            }
            dispatch(setStep(step - 1)); 
        } else {
            navigate(0); 
        }
    };

    const renderStepContent = () => {
        switch (step) {
            case 1: return (<Step1SeatSelection tripId={tripId} />)
            case 2: return (<Step2PassengerInfo tripId={tripId} />);
            case 3: 
                return (
                    <div>Step3</div>
                    // <Step3Payment 
                    //     tripId={tripId} 
                    //     seatIds={selectedSeatIds}
                    //     passengerInfo={passengerInfo}
                    //     holdToken={holdToken}
                    // />
                );
            default: 
                return <div className="text-danger text-center fw-bold">Xin lỗi vì sự cố. Vui lòng liên hệ nhà xe!</div>;
        }
    };
    

    return (
        <Card className="shadow-sm border-0 rounded-4 overflow-hidden">
            <Card.Header className="bg-white border-bottom pt-3 pb-1 px-3">
                
                <div className="d-flex align-items-center gap-3 mb-3">
                    <button 
                        onClick={handleBack}
                        className="btn btn-sm d-flex align-items-center gap-1 px-3 py-1.5 rounded-pill border border-dark"
                        style={{ 
                            backgroundColor: '#fff', 
                            color: '#000', 
                            fontSize: '0.9rem', 
                            fontWeight: '600',
                            transition: 'all 0.2s ease'
                        }}
                        onMouseOver={(e) => { e.currentTarget.style.backgroundColor = 'var(--ralsei-black)'; e.currentTarget.style.color = '#fff'; }}
                        onMouseOut={(e) => { e.currentTarget.style.backgroundColor = '#fff'; e.currentTarget.style.color = '#000'; }}
                    >
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                            <line x1="19" y1="12" x2="5" y2="12"></line>
                            <polyline points="12 19 5 12 12 5"></polyline>
                        </svg>
                        Quay lại
                    </button>

                    <div className="d-flex flex-column text-start">
                        <span className="fw-bold text-dark lh-sm" style={{ fontSize: '1rem', letterSpacing: '-0.01em' }}>
                            19A Lý Thường Kiệt, Đồng Hới - Hà Nội
                        </span>
                        <small className="text-muted mt-0.5" style={{ fontSize: '0.8rem', fontWeight: '500' }}>
                            Thứ hai, 22/06/2026
                        </small>
                    </div>
                </div>

                
                <div className="row text-center g-0 my-2.5" style={{ fontSize: '0.9rem', fontWeight: '600' }}>
                    <div className="col">
                        <div style={{ color: step >= 1 ? 'var(--ralsei-black)' : '#a0a0a0', transition: 'color 0.3s' }}>
                            <span className="me-1.5">❶</span> Chọn ghế ngồi
                        </div>
                    </div>
                    <div className="col">
                        <div style={{ color: step >= 2 ? 'var(--ralsei-black)' : '#a0a0a0', transition: 'color 0.3s' }}>
                            <span className="me-1.5">❷</span> Thông tin lịch trình
                        </div>
                    </div>
                    <div className="col">
                        <div style={{ color: step >= 3 ? 'var(--ralsei-black)' : '#a0a0a0', transition: 'color 0.3s' }}>
                            <span className="me-1.5">❸</span> Thanh toán online
                        </div>
                    </div>
                </div>

                
                <div className="progress" style={{ height: '3px', margin: '8px -16px -4px -16px', borderRadius: 0, backgroundColor: '#f0f0f0' }}>
                    <div className="progress-bar" role="progressbar" 
                        style={{ 
                            width: step === 1 ? '16.66%' : step === 2 ? '50%' : '100%', 
                            backgroundColor: 'var(--ralsei-black)',
                            transition: 'width 0.4s ease'
                        }}>
                    </div>
                </div>
            </Card.Header>

            <Card.Body className="p-4 p-md-5">
                {renderStepContent()}
            </Card.Body>
        </Card>
    );
}