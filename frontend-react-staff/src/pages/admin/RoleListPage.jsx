import { useState } from 'react';
import { Container, Card } from 'react-bootstrap';
import { useRoles, RoleFilter, RoleTable } from '../../features/manage-roles';
import Pagination from '../../components/common/Pagination';

export default function RoleListPage() {
    const { roles, loading, error, filters, pageInfo, setPageInfo, handleFilterChange, handleReset } = useRoles();

    return (
        <Container fluid className="py-3">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="fw-bold m-0">Xem vai trò</h4>
            </div>

            <RoleFilter filters={filters} onFilterChange={handleFilterChange} onReset={handleReset} />

            <Card>
                <Card.Body className="p-0">
                    <RoleTable
                        roles={roles}
                        loading={loading}
                        error={error}
                    />
                </Card.Body>
                {!loading && roles.length > 0 && (
                    <Card.Footer className="d-flex justify-content-center">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </Card.Footer>
                )}
            </Card>
        </Container>
    );
}
