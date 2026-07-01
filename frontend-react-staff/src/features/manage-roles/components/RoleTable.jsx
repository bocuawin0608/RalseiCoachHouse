import { Table, Badge, ButtonGroup, Button, Spinner } from 'react-bootstrap';
import { BsEye, BsPencilFill, BsToggleOn, BsToggleOff, BsTrash, BsExclamationTriangleFill } from 'react-icons/bs';

export default function RoleTable({ roles, loading, error, onViewDetail, onEdit, onToggleActive, onDelete }) {
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
                        <th className="text-center">Thao tác</th>
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
                                <td>
                                    <ButtonGroup size="sm" className="d-flex justify-content-center">
                                        <Button variant="outline-primary" title="Xem chi tiết" onClick={() => onViewDetail(role)}>
                                            <BsEye />
                                        </Button>
                                        <Button variant="outline-secondary" title="Sửa" onClick={() => onEdit(role)}>
                                            <BsPencilFill />
                                        </Button>
                                        <Button
                                            variant={isActive ? 'outline-danger' : 'outline-success'}
                                            title={isActive ? 'Vô hiệu hóa' : 'Kích hoạt'}
                                            onClick={() => onToggleActive(role)}
                                        >
                                            {isActive ? <BsToggleOff /> : <BsToggleOn />}
                                        </Button>
                                        <Button variant="outline-danger" title="Xóa" onClick={() => onDelete(role)}>
                                            <BsTrash />
                                        </Button>
                                    </ButtonGroup>
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </Table>
        </div>
    );
}
