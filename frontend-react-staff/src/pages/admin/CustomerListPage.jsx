import { useState } from 'react';
import { Container, Card } from 'react-bootstrap';
import { useCustomers, CustomerFilter, CustomerTable, CustomerDetailModal } from '../../features/manage-customers';
import Pagination from '../../components/common/Pagination';

export default function CustomerListPage() {
    const { customers, loading, error, filters, pageInfo, setPageInfo, handleFilterChange, handleReset, sortBy, sortDir, handleSort } = useCustomers();

    const [detailData, setDetailData] = useState(null);

    return (
        <Container fluid className="py-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="fw-bold m-0">Danh sách khách hàng</h4>
            </div>

            <CustomerFilter filters={filters} onFilterChange={handleFilterChange} onReset={handleReset} />

            <Card>
                <Card.Body className="p-0">
                    <CustomerTable
                        customers={customers}
                        loading={loading}
                        error={error}
                        onViewDetail={(c) => setDetailData(c)}
                        sortBy={sortBy}
                        sortDir={sortDir}
                        onSort={handleSort}
                    />
                </Card.Body>
                {!loading && customers.length > 0 && (
                    <Card.Footer className="d-flex justify-content-center">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </Card.Footer>
                )}
            </Card>

            <CustomerDetailModal isOpen={!!detailData} data={detailData} onClose={() => setDetailData(null)} />
        </Container>
    );
}
