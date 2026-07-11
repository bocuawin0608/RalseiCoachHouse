import { Table, Badge, ButtonGroup, Button, Spinner } from 'react-bootstrap';
import { BsEye, BsShieldLock, BsKey, BsToggleOn, BsToggleOff, BsExclamationTriangleFill } from 'react-icons/bs';

const ROLE_BADGE_COLORS = {
    ADMIN: 'danger',
    MANAGER: 'warning',
    TICKET_STAFF: 'info',
    TRIP_STAFF: 'secondary',
    CUSTOMER: 'dark',
};

export default function AccountTable({ accounts, loading, error, onViewDetail, onAssignRoles, onResetPassword, onToggleActive }) {
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

    const isLocal = (acc) => acc.authProvider === 'local';

    return (
        <div className="table-responsive">
            <Table hover className="align-middle mb-0">
                <thead className="table-light">
                    <tr>
                        <th>Username</th>
                        <th>Loại</th>
                        <th>Liên kết tới</th>
                        <th>Vai trò</th>
                        <th>Lần đăng nhập cuối</th>
                        <th>Trạng thái</th>
                        <th className="text-center">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    {accounts.map(acc => {
                        const active = acc.active !== false;
                        const linkedName = acc.staffName || acc.customerName || null;
                        const linkedType = acc.staffName ? 'staff' : acc.customerName ? 'customer' : null;
                        return (
                        <tr key={acc.accountId} className={!active ? 'table-danger' : ''}>
                            <td className="fw-medium">{acc.username}</td>
                            <td>
                                <Badge bg={isLocal(acc) ? 'secondary' : 'primary'} style={{ fontSize: '0.75rem' }}>
                                    {isLocal(acc) ? 'Nội bộ' : 'Firebase'}
                                </Badge>
                            </td>
                            <td style={{ fontSize: '0.85rem' }}>
                                {linkedName ? (
                                    <>{linkedName} <small className="text-muted">({linkedType})</small></>
                                ) : (
                                    <span className="text-muted fst-italic">Không có</span>
                                )}
                            </td>
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
                            <td style={{ fontSize: '0.85rem' }}>
                                {acc.lastLogin ? new Date(acc.lastLogin).toLocaleString('vi-VN') : <span className="text-muted">—</span>}
                            </td>
                            <td>
                                <Badge bg={active ? 'success' : 'danger'}>
                                    {active ? 'Hoạt động' : 'Đã khóa'}
                                </Badge>
                            </td>
                            <td>
                                <ButtonGroup size="sm" className="d-flex justify-content-center">
                                    <Button variant="outline-primary" title="Xem chi tiết" onClick={() => onViewDetail(acc)}>
                                        <BsEye />
                                    </Button>
                                    <Button variant="outline-warning" title="Gán vai trò" onClick={() => onAssignRoles(acc)}>
                                        <BsShieldLock />
                                    </Button>
                                    <Button variant="outline-info" title="Đặt lại mật khẩu"
                                        disabled={!isLocal(acc)}
                                        onClick={() => onResetPassword(acc)}>
                                        <BsKey />
                                    </Button>
                                    <Button
                                        variant={active ? 'outline-danger' : 'outline-success'}
                                        title={active ? 'Vô hiệu hóa' : 'Kích hoạt'}
                                        onClick={() => onToggleActive(acc)}
                                    >
                                        {active ? <BsToggleOff /> : <BsToggleOn />}
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