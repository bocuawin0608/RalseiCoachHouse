import { Table, Badge, ButtonGroup, Button, Spinner } from 'react-bootstrap';
import { BsEye, BsPencilFill, BsToggleOn, BsToggleOff, BsExclamationTriangleFill } from 'react-icons/bs';

const ROLE_BADGE_COLORS = {
    ADMIN: 'danger',
    MANAGER: 'warning',
    TICKET_STAFF: 'info',
    TRIP_STAFF: 'secondary',
    CUSTOMER: 'dark',
};

const maskCccd = (v) => {
    if (!v || v.length < 6) return v || '—';
    return v.slice(0, 3) + '••••' + v.slice(-2);
};

export default function StaffTable({ staffList, loading, error, onViewDetail, onEdit, onToggleActive }) {
    if (loading) return (
        <div className="text-center py-5"><Spinner animation="border" variant="primary" /><p className="mt-2 text-muted">Đang tải dữ liệu...</p></div>
    );
    if (error) return (
        <div className="alert alert-danger d-flex align-items-center gap-2"><BsExclamationTriangleFill /><span>{error}</span></div>
    );
    if (!staffList || staffList.length === 0) return (
        <div className="text-center py-5 text-muted"><p>Không tìm thấy nhân viên nào.</p></div>
    );

    return (
        <div className="table-responsive">
            <Table hover className="align-middle mb-0">
                <thead className="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>SĐT</th>
                        {/* <th>CCCD</th> */}
                        <th>Chức vụ</th>
                        <th>Vai trò</th>
                        <th>Bến xe / Đại lý</th>
                        <th>Ngày vào làm</th>
                        <th>Trạng thái</th>
                        <th className="text-center">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    {staffList.map(s => {
                        const isActive = s.active !== false;
                        return (
                            <tr key={s.staffId} className={!isActive ? 'table-danger' : ''}>
                                <td>{s.staffId}</td>
                                <td className="fw-medium">{s.staffName}</td>
                                <td>{s.phone || <span className="text-muted">—</span>}</td>
                                {/* <td style={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>{maskCccd(s.cccd)}</td> */}
                                <td><Badge bg="info">{s.staffPosition}</Badge></td>
                                <td>
                                    {s.roleName
                                        ? <Badge bg={ROLE_BADGE_COLORS[s.roleName] || 'light'}>{s.roleName}</Badge>
                                        : <span className="text-muted">—</span>
                                    }
                                </td>
                                <td>{s.ticketAgencyName || <span className="text-muted">—</span>}</td>
                                <td style={{ fontSize: '0.85rem' }}>{s.hireDate ? new Date(s.hireDate + 'T00:00:00').toLocaleDateString('vi-VN') : <span className="text-muted">—</span>}</td>
                                <td><Badge bg={isActive ? 'success' : 'danger'}>{isActive ? 'Hoạt động' : 'Đã khóa'}</Badge></td>
                                <td>
                                    <ButtonGroup size="sm" className="d-flex justify-content-center">
                                        <Button variant="outline-primary" title="Xem chi tiết" onClick={() => onViewDetail(s)}><BsEye /></Button>
                                        <Button variant="outline-secondary" title="Sửa" onClick={() => onEdit(s)}><BsPencilFill /></Button>
                                        <Button variant={isActive ? 'outline-danger' : 'outline-success'}
                                            title={isActive ? 'Vô hiệu hóa' : 'Kích hoạt'} onClick={() => onToggleActive(s)}>
                                            {isActive ? <BsToggleOff /> : <BsToggleOn />}
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