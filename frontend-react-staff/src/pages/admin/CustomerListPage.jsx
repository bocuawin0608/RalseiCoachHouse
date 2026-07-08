import { useState } from 'react';
import { Container, Button, Card } from 'react-bootstrap';
import { BsPlusLg } from 'react-icons/bs';
import { useCustomers, CustomerFilter, CustomerTable, CustomerCreateModal, CustomerUpdateModal, CustomerDetailModal, customerApi } from '../../features/manage-customers';
import Pagination from '../../components/common/Pagination';

export default function CustomerListPage() {
    const { customers, loading, error, filters, pageInfo, setPageInfo, handleFilterChange, handleReset, refetch } = useCustomers();

    const [modalState, setModalState] = useState({ type: null, data: null });
    const closeModal = () => setModalState({ type: null, data: null });

    const handleToggleActive = (c) => {
        const action = c.active !== false ? 'vô hiệu hóa' : 'kích hoạt';
        if (window.confirm(`Bạn có chắc chắn muốn ${action} khách hàng "${c.customerName}"?`)) {
            customerApi.toggleActive(c.customerId)
                .then(() => refetch())
                .catch(err => alert(err.response?.data?.message || 'Thao tác thất bại.'));
        }
    };

    return (
        <Container fluid className="py-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="fw-bold m-0">Quản lý khách hàng</h4>
                <Button variant="success" size="sm" onClick={() => setModalState({ type: 'create', data: null })}>
                    <BsPlusLg className="me-1" /> Thêm khách hàng
                </Button>
            </div>

            <CustomerFilter filters={filters} onFilterChange={handleFilterChange} onReset={handleReset} />

            <Card>
                <Card.Body className="p-0">
                    <CustomerTable
                        customers={customers}
                        loading={loading}
                        error={error}
                        onViewDetail={(c) => setModalState({ type: 'detail', data: c })}
                        onEdit={(c) => setModalState({ type: 'edit', data: c })}
                        onToggleActive={handleToggleActive}
                    />
                </Card.Body>
                {!loading && customers.length > 0 && (
                    <Card.Footer className="d-flex justify-content-center">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </Card.Footer>
                )}
            </Card>

            <CustomerCreateModal isOpen={modalState.type === 'create'} onClose={closeModal} onSuccess={refetch} />
            <CustomerUpdateModal isOpen={modalState.type === 'edit'} data={modalState.data} onClose={closeModal} onSuccess={refetch} />
            <CustomerDetailModal isOpen={modalState.type === 'detail'} data={modalState.data} onClose={closeModal} />
        </Container>
    );
}
