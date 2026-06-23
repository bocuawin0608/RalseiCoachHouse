import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { Button, Container } from 'react-bootstrap';
import { BookingWizard } from '../../booking';

export default function SearchTripPage() {
    const {tripId}= useParams();
    const [activeTripId, setActiveTripId] = useState(null);

    if (activeTripId) {
        return (
            <Container className="py-4">
                <BookingWizard tripId={activeTripId} />
            </Container>
        );
    }

    return (
        <Container className="py-5 text-center">
            <h2>Kết quả tìm kiếm chuyến xe ngày 22-06-2026</h2>
            <p className="text-muted">Danh sách các chuyến xe hiện ra ở đây...</p>
            
            <Button 
                variant="success"
                style={{ marginBlock: '1rem' }} 
                onClick={() => setActiveTripId(tripId)}
            >
                Chọn chuyến này
            </Button>
        </Container>
    );
}