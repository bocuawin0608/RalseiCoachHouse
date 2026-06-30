import { useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import Step1SeatSelection from './Step1SeatSelection';
import Step2PassengerInfo from './Step2PassengerInfo';
import BookingWizardShell from './BookingWizardShell';
import { bookingApi } from '../api/bookingApi';
import { resetBooking, setStep, setTripInfo } from '../reducers/bookingSlice';
import axiosClient from '../../../api/axiosClient';
import { buildTripShellLabels } from '../utils/tripInfo';

export default function BookingWizard({ tripId }) {
    const navigate = useNavigate();
    const location = useLocation();
    const dispatch = useDispatch();
    const { step, holdToken, selectedSeats, paymentInfo, tripInfo } = useSelector((state) => state.booking);
    const { tripTitle, tripDate } = buildTripShellLabels(tripInfo);

    const latestStateRef = useRef({ selectedSeats, step, holdToken, tripId, paymentInfo });

    useEffect(() => { window.scrollTo(0, 0);}, []);

    useEffect(() => {
        const incomingTripInfo = location.state; 

        if (incomingTripInfo) {
            dispatch(setTripInfo(incomingTripInfo));
        } else if (!tripInfo) {
            navigate('/', { replace: true });
        }
    }, [location.state, tripInfo, dispatch, navigate]);

    useEffect(() => {
        latestStateRef.current = { selectedSeats, step, holdToken, tripId, paymentInfo };
    }, [selectedSeats, step, holdToken, tripId, paymentInfo]);

    useEffect(() => {
        const handleBeforeUnload = () => {
            const { selectedSeats: seats, holdToken: token, tripId: id, paymentInfo: confirmedPayment } = latestStateRef.current;

            if (seats.length > 0 && !confirmedPayment) {
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

            const { selectedSeats: seats, holdToken: token, tripId: id, paymentInfo: confirmedPayment } = latestStateRef.current;

            if (seats.length > 0 && !confirmedPayment) {
                bookingApi.releaseSeats(id, { tripSeatIds: seats.map((seatObj) => seatObj.tripSeatId) }, token).catch(() => {});
            }

            if (!confirmedPayment) {
                dispatch(resetBooking());
            }
        };
    }, [dispatch]);

    const handleBack = async () => {
        if (step > 1) {
            if (step === 2) {
                try {
                    await bookingApi.releaseSeats(tripId, { tripSeatIds: selectedSeats.map((seatObj) => seatObj.tripSeatId) }, holdToken);
                } catch (backError) {
                    console.error('Lỗi khi giải phóng ghế:' + backError.response?.data?.message);
                }
            }
            dispatch(setStep(step - 1));
        } else {
            navigate(-1);
        }
    };

    const renderStepContent = () => {
        switch (step) {
            case 1:
                return <Step1SeatSelection tripId={tripId} />;
            case 2:
                return <Step2PassengerInfo tripId={tripId} />;
            default:
                return <div className="text-danger text-center fw-bold">Xin lỗi vì sự cố. Vui lòng liên hệ nhà xe!</div>;
        }
    };

    return (
        <BookingWizardShell step={step} onBack={handleBack} tripTitle={tripTitle} tripDate={tripDate}>
            {renderStepContent()}
        </BookingWizardShell>
    );
}
