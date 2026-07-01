import { useState } from 'react';
import { Container, Button, Card } from 'react-bootstrap';
import { BsPlusLg } from 'react-icons/bs';
import { useRoles, RoleFilter, RoleTable, RoleCreateModal, RoleUpdateModal, RoleDetailModal, roleApi } from '../../features/manage-roles';
import Pagination from '../../components/common/Pagination';

export default function RoleListPage() {
    const { roles, loading, error, filters, pageInfo, setPageInfo, handleFilterChange, handleReset, refetch } = useRoles();

    const [modalState, setModalState] = useState({ type: null, data: null });

    const closeModal = () => setModalState({ type: null, data: null });

    const handleConfirmDelete = (role) => {
        if (window.confirm(`Bạn có chắc chắn muốn xóa vai trò "${role.roleName}"?`)) {
            roleApi.deleteRole(role.roleId)
                .then(() => refetch())
                .catch(err => alert(err.response?.data?.message || 'Xóa vai trò thất bại.'));
        }
    };

    const handleToggleActive = (role) => {
        const action = role.active !== false ? 'vô hiệu hóa' : 'kích hoạt';
        if (window.confirm(`Bạn có chắc chắn muốn ${action} vai trò "${role.roleName}"?`)) {
            roleApi.toggleActive(role.roleId)
                .then(() => refetch())
                .catch(err => alert(err.response?.data?.message || 'Thao tác thất bại.'));
        }
    };

    return (
        <Container fluid className="py-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="fw-bold m-0">Quản lý vai trò</h4>
                <Button variant="success" size="sm" onClick={() => setModalState({ type: 'create', data: null })}>
                    <BsPlusLg className="me-1" /> Thêm vai trò
                </Button>
            </div>

            <RoleFilter filters={filters} onFilterChange={handleFilterChange} onReset={handleReset} />

            <Card>
                <Card.Body className="p-0">
                    <RoleTable
                        roles={roles}
                        loading={loading}
                        error={error}
                        onViewDetail={(role) => setModalState({ type: 'detail', data: role })}
                        onEdit={(role) => setModalState({ type: 'edit', data: role })}
                        onToggleActive={handleToggleActive}
                        onDelete={handleConfirmDelete}
                    />
                </Card.Body>
                {!loading && roles.length > 0 && (
                    <Card.Footer className="d-flex justify-content-center">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </Card.Footer>
                )}
            </Card>

            <RoleCreateModal
                isOpen={modalState.type === 'create'}
                onClose={closeModal}
                onSuccess={refetch}
            />
            <RoleUpdateModal
                isOpen={modalState.type === 'edit'}
                data={modalState.data}
                onClose={closeModal}
                onSuccess={refetch}
            />
            <RoleDetailModal
                isOpen={modalState.type === 'detail'}
                data={modalState.data}
                onClose={closeModal}
            />
        </Container>
    );
}
