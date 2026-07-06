import { Container } from 'react-bootstrap';
import { Step3Payment, BookingWizardShell, buildTripShellLabels, loadPaymentSession } from '../../../features/booking';
import { useParams } from 'react-router-dom';
import { useSelector } from 'react-redux';

export default function BookingPaymentPage() {
    const { transactionId } = useParams();
    const {tripInfo, paymentInfo} = useSelector((state) => state.booking);
    const sessionPayment = loadPaymentSession(transactionId);
    const shellFromTripInfo = buildTripShellLabels(tripInfo);
    
    const tripTitle = paymentInfo?.tripTitle
        || sessionPayment?.tripTitle
        || shellFromTripInfo.tripTitle;
    const tripDate = paymentInfo?.tripDate
        || sessionPayment?.tripDate
        || shellFromTripInfo.tripDate;

    return (
        <Container className="py-4">
            <BookingWizardShell paymentMode tripTitle={tripTitle} tripDate={tripDate}>
                <Step3Payment />
            </BookingWizardShell>
        </Container>
    );
}
