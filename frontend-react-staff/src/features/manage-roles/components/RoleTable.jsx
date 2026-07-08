import { Table, Badge, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';

export default function RoleTable({ roles, loading, error }) {
    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" variant="primary" />
                <p className="mt-2 text-muted">Đang tải dữ liệu...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="alert alert-danger d-flex align-items-center gap-2">
                <BsExclamationTriangleFill />
                <span>{error}</span>
            </div>
        );
    }

    if (!roles || roles.length === 0) {
        return (
            <div className="text-center py-5 text-muted">
                <p>Không tìm thấy vai trò nào.</p>
            </div>
        );
    }

    return (
        <div className="table-responsive">
            <Table hover className="align-middle mb-0">
                <thead className="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Tên vai trò</th>
                        <th>Trạng thái</th>
                        <th className="text-center">Số TK đang dùng</th>
                    </tr>
                </thead>
                <tbody>
                    {roles.map(role => {
                        const isActive = role.active !== false;
                        return (
                            <tr key={role.roleId} className={!isActive ? 'table-danger' : ''}>
                                <td>{role.roleId}</td>
                                <td className="fw-medium">{role.roleName}</td>
                                <td>
                                    <Badge bg={isActive ? 'success' : 'danger'}>
                                        {isActive ? 'Hoạt động' : 'Đã khóa'}
                                    </Badge>
                                </td>
                                <td className="text-center">
                                    <Badge bg="light" text="dark">{role.assignedCount ?? 0}</Badge>
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </Table>
        </div>
    );
}
