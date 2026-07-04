import { Container } from "react-bootstrap";
import { useParams } from "react-router-dom";
import { BookingWizard } from "../../../features/booking";

export default function BookingTripPage() {
    const {tripId}= useParams();

    return (
        <Container className="py-4">
            <BookingWizard tripId={tripId} />
        </Container>
    );
}