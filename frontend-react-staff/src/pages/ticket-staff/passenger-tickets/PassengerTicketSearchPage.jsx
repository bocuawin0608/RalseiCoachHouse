import { Alert, Card, Container } from 'react-bootstrap';
import { useState } from 'react';
import Pagination from '../../../components/common/Pagination';
import {
    usePassengerTicketSearch,
    PassengerTicketSearchFilters,
    PassengerTicketSearchResults,
    PassengerTicketDetailModal,
} from '../../../features/passenger-tickets';

export default function PassengerTicketSearchPage() {
    const [selectedTicketCode, setSelectedTicketCode] = useState(null);

    const {
        filters,
        data,
        loading,
        error,
        hasSearched,
        hiddenTripId,
        pageInfo,
        setPageInfo,
        handleFilterChange,
        handleReset,
        handleSearch,
    } = usePassengerTicketSearch();

    return (
        <Container fluid className="py-2" style={{ maxWidth: '1400px' }}>
            <div className="mb-4">
                <h2 className="fw-bold text-dark mb-1">Tra cứu vé hành khách</h2>
                <p className="text-muted mb-0">
                    Tìm vé theo SĐT, mã vé hoặc ngày khởi hành.
                    {hiddenTripId && (
                        <span className="ms-1">
                            Đang lọc theo chuyến #{hiddenTripId}.
                        </span>
                    )}
                </p>
            </div>

            <PassengerTicketSearchFilters
                filters={filters}
                onFilterChange={handleFilterChange}
                onReset={handleReset}
                onSearch={handleSearch}
                searching={loading}
            />

            {error && <Alert variant="danger">{error}</Alert>}

            <Card className="shadow-sm border-0">
                <Card.Body className="p-0">
                    <PassengerTicketSearchResults
                        data={data}
                        loading={loading}
                        hasSearched={hasSearched || Boolean(hiddenTripId)}
                        onSelectTicket={setSelectedTicketCode}
                    />

                    {(hasSearched || hiddenTripId) && (
                        <div className="d-flex justify-content-center py-4 border-top">
                            <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                        </div>
                    )}
                </Card.Body>
            </Card>

            <PassengerTicketDetailModal
                ticketCode={selectedTicketCode}
                onClose={() => setSelectedTicketCode(null)}
            />
        </Container>
    );
}
