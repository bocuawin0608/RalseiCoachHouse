import React from 'react';
import { Modal, Table, Badge } from 'react-bootstrap';
import { formatCurrency, formatDateTime } from '../../../utils/formatters';

const STATUS_CONFIG = {
    ACTIVE: { label: 'Đang hoạt động', variant: 'success' },
    UPCOMING: { label: 'Sắp diễn ra', variant: 'info' },
    EXPIRED: { label: 'Đã hết hạn', variant: 'secondary' },
    EXHAUSTED: { label: 'Đã dùng hết', variant: 'warning' },
};

const VoucherDetailModal = ({ isOpen, data, onClose }) => {
    if (!data) return null;

    return (
        <Modal show={isOpen} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>Chi tiết voucher</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Table bordered>
                    <tbody>
                        <tr>
                            <td className="fw-bold" style={{ width: '200px' }}>Mã voucher</td>
                            <td>{data.voucherCode}</td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Loại giảm</td>
                            <td>
                                <Badge bg={data.discountType === 'PERCENT' ? 'primary' : 'success'}>
                                    {data.discountType === 'PERCENT' ? 'Phần trăm' : 'Cố định'}
                                </Badge>
                            </td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Giá trị giảm</td>
                            <td>
                                {data.discountType === 'PERCENT'
                                    ? `${data.discountValue}%`
                                    : formatCurrency(data.discountValue)}
                            </td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Giá trị giảm tối đa</td>
                            <td>{data.maxDiscountValue ? formatCurrency(data.maxDiscountValue) : '---'}</td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Đơn hàng tối thiểu</td>
                            <td>{data.minOrderValue ? formatCurrency(data.minOrderValue) : '---'}</td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Ngày bắt đầu</td>
                            <td>{formatDateTime(data.startEffectiveDate)}</td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Ngày kết thúc</td>
                            <td>{formatDateTime(data.endEffectiveDate)}</td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Giới hạn sử dụng</td>
                            <td>{data.usageLimit}</td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Đã sử dụng</td>
                            <td>{data.usedCount}</td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Trạng thái</td>
                            <td>
                                <Badge bg={STATUS_CONFIG[data.status]?.variant || 'secondary'}>
                                    {STATUS_CONFIG[data.status]?.label || data.status}
                                </Badge>
                            </td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Ngày tạo</td>
                            <td>{formatDateTime(data.createdAt)}</td>
                        </tr>
                        <tr>
                            <td className="fw-bold">Ngày cập nhật</td>
                            <td>{formatDateTime(data.updatedAt)}</td>
                        </tr>
                    </tbody>
                </Table>
            </Modal.Body>
        </Modal>
    );
};

export default VoucherDetailModal;
