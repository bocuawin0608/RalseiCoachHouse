import { useState, useEffect } from 'react';
import { Container, Button, Card } from 'react-bootstrap';
import { BsPlusLg, BsPersonCheck } from 'react-icons/bs';
import { useStaff, StaffFilter, StaffTable, StaffUpdateModal, StaffDetailModal, StaffOnboardModal, staffApi } from '../../features/manage-staff';
import { ticketAgencyApi } from '../../features/manage-ticket-agencies';
import Pagination from '../../components/common/Pagination';

export default function StaffListPage() {
    const { staffList, loading, error, filters, pageInfo, setPageInfo, handleFilterChange, handleReset, refetch } = useStaff();
    const [ticketAgencies, setTicketAgencies] = useState([]);
    const [modalState, setModalState] = useState({ type: null, data: null });
    const closeModal = () => setModalState({ type: null, data: null });

    useEffect(() => {
        ticketAgencyApi.filter({ page: 0, size: 999, isActive: true })
            .then(res => setTicketAgencies(res.content || []))
            .catch(() => setTicketAgencies([]));
    }, []);

    const handleDelete = (s) => {
        if (window.confirm(`Bạn có chắc chắn muốn xóa nhân viên "${s.staffName}"?`)) {
            staffApi.remove(s.staffId)
                .then(() => refetch())
                .catch(err => alert(err.response?.data?.message || 'Xóa thất bại.'));
        }
    };

    const handleToggle = (s) => {
        const action = s.active !== false ? 'vô hiệu hóa' : 'kích hoạt';
        if (window.confirm(`Bạn có chắc chắn muốn ${action} nhân viên "${s.staffName}"?`)) {
            staffApi.toggleActive(s.staffId)
                .then(() => refetch())
                .catch(err => alert(err.response?.data?.message || 'Thao tác thất bại.'));
        }
    };

    return (
        <Container fluid className="py-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="fw-bold m-0">Quản lý nhân viên</h4>
                <Button variant="success" size="sm" onClick={() => setModalState({ type: 'onboard', data: null })}>
                    <BsPersonCheck className="me-1" /> Onboard nhân viên
                </Button>
            </div>
            <StaffFilter filters={filters} onFilterChange={handleFilterChange} onReset={handleReset} ticketAgencies={ticketAgencies} />
            <Card>
                <Card.Body className="p-0">
                    <StaffTable staffList={staffList} loading={loading} error={error}
                        onViewDetail={(s) => setModalState({ type: 'detail', data: s })}
                        onEdit={(s) => setModalState({ type: 'edit', data: s })}
                        onToggleActive={handleToggle} onDelete={handleDelete} />
                </Card.Body>
                {!loading && staffList.length > 0 && (
                    <Card.Footer className="d-flex justify-content-center">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </Card.Footer>
                )}
            </Card>
            <StaffUpdateModal isOpen={modalState.type === 'edit'} data={modalState.data} onClose={closeModal} onSuccess={refetch} ticketAgencies={ticketAgencies} />
            <StaffDetailModal isOpen={modalState.type === 'detail'} data={modalState.data} onClose={closeModal} />
            <StaffOnboardModal isOpen={modalState.type === 'onboard'} onClose={closeModal} onSuccess={refetch} ticketAgencies={ticketAgencies} />
        </Container>
    );
}
