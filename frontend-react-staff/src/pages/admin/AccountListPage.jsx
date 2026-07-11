import { useState } from 'react';
import { Container, Card } from 'react-bootstrap';
import { useAccounts, AccountFilter, AccountTable, AccountDetailModal, AccountRoleModal, AccountResetPasswordModal } from '../../features/manage-accounts';
import Pagination from '../../components/common/Pagination';

export default function AccountListPage() {
    const { accounts, loading, error, filters, pageInfo, setPageInfo, handleFilterChange, handleReset, refetch } = useAccounts();

    const [modalState, setModalState] = useState({ type: null, data: null });

    const closeModal = () => setModalState({ type: null, data: null });

    const handleToggleActive = (acc) => {
        const action = acc.active !== false ? 'vô hiệu hóa' : 'kích hoạt';
        if (window.confirm(`Bạn có chắc chắn muốn ${action} tài khoản "${acc.username}"?`)) {
            import('../../features/manage-accounts/api/accountApi').then(({ default: api }) => {
                api.toggleActive(acc.accountId).then(() => refetch()).catch(() => {});
            });
        }
    };

    return (
        <Container fluid className="py-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="fw-bold m-0">Quản lý tài khoản</h4>
            </div>

            <AccountFilter filters={filters} onFilterChange={handleFilterChange} onReset={handleReset} />

            <Card>
                <Card.Body className="p-0">
                    <AccountTable
                        accounts={accounts}
                        loading={loading}
                        error={error}
                        onViewDetail={(acc) => setModalState({ type: 'detail', data: acc })}
                        onAssignRoles={(acc) => setModalState({ type: 'roles', data: acc })}
                        onResetPassword={(acc) => setModalState({ type: 'reset-password', data: acc })}
                        onToggleActive={handleToggleActive}
                    />
                </Card.Body>
                {!loading && accounts.length > 0 && (
                    <Card.Footer className="d-flex justify-content-center">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </Card.Footer>
                )}
            </Card>

            <AccountDetailModal
                isOpen={modalState.type === 'detail'}
                data={modalState.data}
                onClose={closeModal}
            />
            <AccountRoleModal
                isOpen={modalState.type === 'roles'}
                data={modalState.data}
                onClose={closeModal}
                onSuccess={refetch}
            />
            <AccountResetPasswordModal
                isOpen={modalState.type === 'reset-password'}
                data={modalState.data}
                onClose={closeModal}
                onSuccess={refetch}
            />
        </Container>
    );
}