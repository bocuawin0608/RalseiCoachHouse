import { Table, Badge, ButtonGroup, Button, Spinner } from 'react-bootstrap';
import { BsEye, BsPencilFill, BsToggleOn, BsToggleOff, BsTrash, BsExclamationTriangleFill } from 'react-icons/bs';

export default function CustomerTable({ customers, loading, error, onViewDetail, onEdit, onToggleActive, onDelete }) {
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

    if (!customers || customers.length === 0) {
        return (
            <div className="text-center py-5 text-muted">
                <p>Không tìm thấy khách hàng nào.</p>
            </div>
        );
    }

    return (
        <div className="table-responsive">
            <Table hover className="align-middle mb-0">
                <thead className="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>SĐT</th>
                        <th>Email</th>
                        <th>Trạng thái</th>
                        <th className="text-center">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    {customers.map(c => {
                        const isActive = c.active !== false;
                        return (
                            <tr key={c.customerId} className={!isActive ? 'table-danger' : ''}>
                                <td>{c.customerId}</td>
                                <td className="fw-medium">{c.customerName}</td>
                                <td>{c.phone || <span className="text-muted">—</span>}</td>
                                <td>{c.email || <span className="text-muted">—</span>}</td>
                                <td>
                                    <Badge bg={isActive ? 'success' : 'danger'}>
                                        {isActive ? 'Hoạt động' : 'Đã khóa'}
                                    </Badge>
                                </td>
                                <td>
                                    <ButtonGroup size="sm" className="d-flex justify-content-center">
                                        <Button variant="outline-primary" title="Xem chi tiết" onClick={() => onViewDetail(c)}>
                                            <BsEye />
                                        </Button>
                                        <Button variant="outline-secondary" title="Sửa" onClick={() => onEdit(c)}>
                                            <BsPencilFill />
                                        </Button>
                                        <Button
                                            variant={isActive ? 'outline-danger' : 'outline-success'}
                                            title={isActive ? 'Vô hiệu hóa' : 'Kích hoạt'}
                                            onClick={() => onToggleActive(c)}
                                        >
                                            {isActive ? <BsToggleOff /> : <BsToggleOn />}
                                        </Button>
                                        <Button variant="outline-danger" title="Xóa" onClick={() => onDelete(c)}>
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
