import React from 'react';
import { Table, Badge } from 'react-bootstrap';
import { BsEye, BsPencilFill, BsTrashFill } from 'react-icons/bs';
import { formatCurrency, formatDateTime } from '../../../utils/formatters';

const STATUS_CONFIG = {
    ACTIVE: { label: 'Đang hoạt động', variant: 'success' },
    UPCOMING: { label: 'Sắp diễn ra', variant: 'info' },
    EXPIRED: { label: 'Đã hết hạn', variant: 'secondary' },
    EXHAUSTED: { label: 'Đã dùng hết', variant: 'warning' },
};

const VoucherTable = ({ vouchers, loading, onView, onEdit, onDelete }) => {
    if (loading) {
        return <div className="text-center py-4">Loading...</div>;
    }

    if (!vouchers || vouchers.length === 0) {
        return <div className="text-center py-4">Không tìm thấy dữ liệu</div>;
    }

    return (
        <Table striped bordered hover responsive>
            <thead className="table-dark">
                <tr>
                    <th>Mã voucher</th>
                    <th>Loại giảm</th>
                    <th>Giá trị</th>
                    <th>Hiệu lực</th>
                    <th>Đã dùng / Giới hạn</th>
                    <th>Trạng thái</th>
                    <th>Hành động</th>
                </tr>
            </thead>
            <tbody>
                {vouchers.map((v) => (
                    <tr key={v.voucherId}>
                        <td>{v.voucherCode}</td>
                        <td>
                            <Badge bg={v.discountType === 'PERCENT' ? 'primary' : 'success'}>
                                {v.discountType === 'PERCENT' ? 'PERCENT' : 'FIXED'}
                            </Badge>
                        </td>
                        <td>
                            {v.discountType === 'PERCENT'
                                ? `${v.discountValue}%`
                                : formatCurrency(v.discountValue)}
                        </td>
                        <td>
                            {formatDateTime(v.startEffectiveDate)} - {formatDateTime(v.endEffectiveDate)}
                        </td>
                        <td>{v.usedCount} / {v.usageLimit}</td>
                        <td>
                            <Badge bg={STATUS_CONFIG[v.status]?.variant || 'secondary'}>
                                {STATUS_CONFIG[v.status]?.label || v.status}
                            </Badge>
                        </td>
                        <td>
                            <div className="d-flex gap-2">
                                <button className="btn btn-sm btn-outline-info" title="View" onClick={() => onView(v)}>
                                    <BsEye />
                                </button>
                                <button className="btn btn-sm btn-outline-primary" title="Edit" onClick={() => onEdit(v)}>
                                    <BsPencilFill />
                                </button>
                                <button className="btn btn-sm btn-outline-danger" title="Delete" onClick={() => onDelete(v)}>
                                    <BsTrashFill />
                                </button>
                            </div>
                        </td>
                    </tr>
                ))}
            </tbody>
        </Table>
    );
};

export default VoucherTable;
