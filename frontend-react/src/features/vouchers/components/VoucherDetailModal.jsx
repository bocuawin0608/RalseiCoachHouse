import React from 'react';
import { Modal } from 'react-bootstrap';
import { formatCurrency, formatDateTime } from '../../../utils/formatters';

const STATUS_LABEL = {
  ACTIVE: 'Đang hoạt động',
  UPCOMING: 'Sắp diễn ra',
  EXPIRED: 'Đã hết hạn',
  EXHAUSTED: 'Đã dùng hết',
};

const BADGE_CLASS = {
  ACTIVE: 'voucher-status-badge--active',
  UPCOMING: 'voucher-status-badge--upcoming',
  EXPIRED: 'voucher-status-badge--expired',
  EXHAUSTED: 'voucher-status-badge--exhausted',
};

const VoucherDetailModal = ({ isOpen, data, onClose }) => {
  if (!data) return null;

  return (
    <Modal show={isOpen} onHide={onClose} size="lg" centered dialogClassName="voucher-modal">
      <Modal.Header closeButton>
        <Modal.Title>Chi tiết voucher</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <table className="table">
          <tbody>
            <tr>
              <td className="fw-bold" style={{ width: '180px' }}>Mã voucher</td>
              <td><span className="voucher-code-tag">{data.voucherCode}</span></td>
            </tr>
            <tr>
              <td className="fw-bold">Loại giảm</td>
              <td>
                <span className={`voucher-type-badge voucher-type-badge--${data.discountType === 'PERCENT' ? 'percent' : 'fixed'}`}>
                  {data.discountType === 'PERCENT' ? 'Phần trăm' : 'Cố định'}
                </span>
              </td>
            </tr>
            <tr>
              <td className="fw-bold">Giá trị giảm</td>
              <td>
                {data.discountType === 'PERCENT' ? `${data.discountValue}%` : formatCurrency(data.discountValue)}
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
                <span className={`voucher-status-badge ${BADGE_CLASS[data.status] || ''}`}>
                  {STATUS_LABEL[data.status] || data.status}
                </span>
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
        </table>
      </Modal.Body>
    </Modal>
  );
};

export default VoucherDetailModal;
