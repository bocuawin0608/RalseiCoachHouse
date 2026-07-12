import { useState } from 'react';
import { Container, Button, Card, Alert } from 'react-bootstrap';
import { BsPlusLg } from 'react-icons/bs';
import { useTicketAgencies, TicketAgencyFilter, TicketAgencyTable, TicketAgencyCreateModal, TicketAgencyUpdateModal, TicketAgencyDetailModal, ticketAgencyApi } from '../../features/manage-ticket-agencies';
import Pagination from '../../components/common/Pagination';

export default function TicketAgencyListPage() {
    const { agencies, loading, error, filters, pageInfo, setPageInfo, handleFilterChange, handleReset, refetch } = useTicketAgencies();
    const [modalState, setModalState] = useState({ type: null, data: null });
    const closeModal = () => setModalState({ type: null, data: null });

    const [toggleError, setToggleError] = useState('');

    const handleToggle = (a) => {
        const action = a.active !== false ? 'vô hiệu hóa' : 'kích hoạt';
        if (a.active !== false && a.staffCount > 0) {
            setToggleError(`Đại lý này còn ${a.staffCount} nhân viên đang làm việc. Vui lòng chuyển họ sang đại lý khác trước khi vô hiệu hóa.`);
            return;
        }
        setToggleError('');
        if (window.confirm(`Bạn có chắc chắn muốn ${action} bến xe "${a.ticketAgencyName}"?`)) {
            ticketAgencyApi.toggleActive(a.ticketAgencyId)
                .then(() => refetch())
                .catch(err => alert(err.response?.data?.message || 'Thao tác thất bại.'));
        }
    };

    return (
        <Container fluid className="py-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="fw-bold m-0">Đại lý bán vé</h4>
                <Button variant="success" size="sm" onClick={() => setModalState({ type: 'create', data: null })}>
                    <BsPlusLg className="me-1" /> Thêm bến xe
                </Button>
            </div>
            {toggleError && (
                <Alert variant="danger" dismissible onClose={() => setToggleError('')} className="py-2">{toggleError}</Alert>
            )}
            <TicketAgencyFilter filters={filters} onFilterChange={handleFilterChange} onReset={handleReset} />
            <Card>
                <Card.Body className="p-0">
                    <TicketAgencyTable agencies={agencies} loading={loading} error={error}
                        onViewDetail={(a) => setModalState({ type: 'detail', data: a })}
                        onEdit={(a) => setModalState({ type: 'edit', data: a })}
                        onToggleActive={handleToggle} />
                </Card.Body>
                {!loading && agencies.length > 0 && (
                    <Card.Footer className="d-flex justify-content-center">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </Card.Footer>
                )}
            </Card>
            <TicketAgencyCreateModal isOpen={modalState.type === 'create'} onClose={closeModal} onSuccess={refetch} />
            <TicketAgencyUpdateModal isOpen={modalState.type === 'edit'} data={modalState.data} onClose={closeModal} onSuccess={refetch} />
            <TicketAgencyDetailModal isOpen={modalState.type === 'detail'} data={modalState.data} onClose={closeModal} />
        </Container>
    );
}
