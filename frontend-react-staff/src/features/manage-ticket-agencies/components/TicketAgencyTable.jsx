import { Table, Badge, ButtonGroup, Button, Spinner } from 'react-bootstrap';
import { BsEye, BsPencilFill, BsToggleOn, BsToggleOff, BsExclamationTriangleFill } from 'react-icons/bs';

export default function TicketAgencyTable({ agencies, loading, error, onViewDetail, onEdit, onToggleActive }) {
    if (loading) return (
        <div className="text-center py-5"><Spinner animation="border" variant="primary" /><p className="mt-2 text-muted">Đang tải dữ liệu...</p></div>
    );
    if (error) return (
        <div className="alert alert-danger d-flex align-items-center gap-2"><BsExclamationTriangleFill /><span>{error}</span></div>
    );
    if (!agencies || agencies.length === 0) return (
        <div className="text-center py-5 text-muted"><p>Không tìm thấy bến xe nào.</p></div>
    );

    return (
        <div className="table-responsive">
            <Table hover className="align-middle mb-0">
                <thead className="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Tên bến xe</th>
                        <th>Điểm dừng</th>
                        <th>Thành phố</th>
                        <th>Trạng thái</th>
                        <th className="text-center">NV đang dùng</th>
                        <th className="text-center">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    {agencies.map(a => {
                        const isActive = a.active !== false;
                        return (
                            <tr key={a.ticketAgencyId} className={!isActive ? 'table-danger' : ''}>
                                <td>{a.ticketAgencyId}</td>
                                <td className="fw-medium">{a.ticketAgencyName}</td>
                                <td>{a.stopPointName || <span className="text-muted">—</span>}</td>
                                <td>{a.city || <span className="text-muted">—</span>}</td>
                                <td><Badge bg={isActive ? 'success' : 'danger'}>{isActive ? 'Hoạt động' : 'Đã khóa'}</Badge></td>
                                <td className="text-center"><Badge bg="light" text="dark">{a.staffCount ?? 0}</Badge></td>
                                <td>
                                    <ButtonGroup size="sm" className="d-flex justify-content-center">
                                        <Button variant="outline-primary" title="Xem chi tiết" onClick={() => onViewDetail(a)}><BsEye /></Button>
                                        <Button variant="outline-secondary" title="Sửa" onClick={() => onEdit(a)}><BsPencilFill /></Button>
                                        <Button variant={isActive ? 'outline-danger' : 'outline-success'}
                                            title={isActive ? 'Vô hiệu hóa' : 'Kích hoạt'} onClick={() => onToggleActive(a)}>
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
