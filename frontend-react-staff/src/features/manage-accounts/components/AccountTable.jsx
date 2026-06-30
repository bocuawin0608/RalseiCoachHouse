import { Table, Badge, ButtonGroup, Button, Spinner } from 'react-bootstrap';
import { BsEye, BsPencilFill, BsShieldLock, BsKey, BsToggleOn, BsToggleOff, BsTrash, BsExclamationTriangleFill } from 'react-icons/bs';

const ROLE_BADGE_COLORS = {
    ADMIN: 'danger',
    MANAGER: 'warning',
    TICKET_STAFF: 'info',
    TRIP_STAFF: 'secondary',
};

export default function AccountTable({ accounts, loading, error, onViewDetail, onEdit, onAssignRoles, onResetPassword, onToggleActive, onDelete }) {
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

    if (!accounts || accounts.length === 0) {
        return (
            <div className="text-center py-5 text-muted">
                <p>Không tìm thấy tài khoản nào.</p>
            </div>
        );
    }

    return (
        <div className="table-responsive">
            <Table hover className="align-middle mb-0">
                <thead className="table-light">
                    <tr>
                        <th>Username</th>
                        <th>Tên nhân viên</th>
                        <th>Chức vụ</th>
                        <th>Vai trò</th>
                        <th>Loại</th>
                        <th>Trạng thái</th>
                        <th>Lần cuối</th>
                        <th className="text-center">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    {accounts.map(acc => (
                        <tr key={acc.accountId}>
                            <td className="fw-medium">{acc.username}</td>
                            <td>{acc.staffName || <span className="text-muted fst-italic">Chưa có</span>}</td>
                            <td>{acc.staffPosition || <span className="text-muted">—</span>}</td>
                            <td>
                                {acc.roles && acc.roles.length > 0
                                    ? acc.roles.map(role => (
                                        <Badge key={role} bg={ROLE_BADGE_COLORS[role] || 'light'} className="me-1">
                                            {role}
                                        </Badge>
                                    ))
                                    : <Badge bg="light" text="dark">Chưa gán</Badge>
                                }
                            </td>
                            <td><Badge bg="light" text="dark">{acc.authProvider}</Badge></td>
                            <td>
                                <Badge bg={acc.active !== false ? 'success' : 'secondary'}>
                                    {acc.active !== false ? 'Hoạt động' : 'Đã khóa'}
                                </Badge>
                            </td>
                            <td className="small text-muted">
                                {acc.lastLogin ? new Date(acc.lastLogin).toLocaleString('vi-VN') : '—'}
                            </td>
                            <td>
                                <ButtonGroup size="sm" className="d-flex justify-content-center">
                                    <Button variant="outline-primary" title="Xem chi tiết" onClick={() => onViewDetail(acc)}>
                                        <BsEye />
                                    </Button>
                                    <Button variant="outline-secondary" title="Sửa thông tin" onClick={() => onEdit(acc)}>
                                        <BsPencilFill />
                                    </Button>
                                    <Button variant="outline-warning" title="Phân quyền" onClick={() => onAssignRoles(acc)}>
                                        <BsShieldLock />
                                    </Button>
                                    <Button variant="outline-info" title="Đặt lại mật khẩu" onClick={() => onResetPassword(acc)}>
                                        <BsKey />
                                    </Button>
                                    <Button
                                        variant={acc.active !== false ? 'outline-danger' : 'outline-success'}
                                        title={acc.active !== false ? 'Vô hiệu hóa' : 'Kích hoạt'}
                                        onClick={() => onToggleActive(acc)}
                                    >
                                        {acc.active !== false ? <BsToggleOff /> : <BsToggleOn />}
                                    </Button>
                                    <Button variant="outline-danger" title="Xóa" onClick={() => onDelete(acc)}>
                                        <BsTrash />
                                    </Button>
                                </ButtonGroup>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </Table>
        </div>
    );
}
