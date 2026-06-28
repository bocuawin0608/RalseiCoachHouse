import { Container } from 'react-bootstrap';
import { Step3Payment, BookingWizardShell } from '../../../features/booking';

export default function BookingPaymentPage() {
    return (
        <Container className="py-4">
            <BookingWizardShell paymentMode>
                <Step3Payment />
            </BookingWizardShell>
        </Container>
    );
}
