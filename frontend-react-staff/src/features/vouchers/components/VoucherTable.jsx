import React from 'react';
import { BsEye, BsPencilFill, BsTrashFill } from 'react-icons/bs';
import { formatCurrency, formatDateTime } from '../../../utils/formatters';

const STUB_CLASS = {
  ACTIVE: 'voucher-stub-active',
  UPCOMING: 'voucher-stub-upcoming',
  EXPIRED: 'voucher-stub-expired',
  EXHAUSTED: 'voucher-stub-exhausted',
};

const BADGE_CLASS = {
  ACTIVE: 'voucher-status-badge--active',
  UPCOMING: 'voucher-status-badge--upcoming',
  EXPIRED: 'voucher-status-badge--expired',
  EXHAUSTED: 'voucher-status-badge--exhausted',
};

const STATUS_LABEL = {
  ACTIVE: 'Đang hoạt động',
  UPCOMING: 'Sắp diễn ra',
  EXPIRED: 'Đã hết hạn',
  EXHAUSTED: 'Đã dùng hết',
};

const VoucherTable = ({ vouchers, loading, onView, onEdit, onDelete }) => {
  if (loading) {
    return (
      <div className="voucher-loading">
        <div className="spinner-border spinner-border-sm me-2" role="status" />
        Đang tải dữ liệu…
      </div>
    );
  }

  if (!vouchers || vouchers.length === 0) {
    return (
      <div className="voucher-empty-state">
        <p>Không tìm thấy dữ liệu</p>
      </div>
    );
  }

  return (
    <div className="voucher-table-wrap">
      <table className="voucher-table">
        <thead>
          <tr>
            <th style={{ width: 4 }} />
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
              <td className="voucher-row-stub">
                <div className={STUB_CLASS[v.status] || 'voucher-stub-expired'} />
              </td>
              <td>
                <span className="voucher-code-tag" title={v.voucherCode}>
                  {v.voucherCode}
                </span>
              </td>
              <td>
                <span className={`voucher-type-badge voucher-type-badge--${v.discountType === 'PERCENT' ? 'percent' : 'fixed'}`}>
                  {v.discountType === 'PERCENT' ? 'PERCENT' : 'FIXED'}
                </span>
              </td>
              <td>
                {v.discountType === 'PERCENT'
                  ? `${v.discountValue}%`
                  : formatCurrency(v.discountValue)}
              </td>
              <td>
                {formatDateTime(v.startEffectiveDate)}
                <br />
                <span className="text-muted" style={{ fontSize: '0.8rem' }}>
                  → {formatDateTime(v.endEffectiveDate)}
                </span>
              </td>
              <td>{v.usedCount} / {v.usageLimit}</td>
              <td>
                <span className={`voucher-status-badge ${BADGE_CLASS[v.status] || ''}`}>
                  {STATUS_LABEL[v.status] || v.status}
                </span>
              </td>
              <td>
                <div className="d-flex gap-1">
                  <button className="voucher-action-btn" title="Xem chi tiết" onClick={() => onView(v)}>
                    <BsEye />
                  </button>
                  <button className="voucher-action-btn" title="Chỉnh sửa" onClick={() => onEdit(v)}>
                    <BsPencilFill />
                  </button>
                  <button className="voucher-action-btn voucher-action-btn--danger" title="Xóa / Vô hiệu hóa" onClick={() => onDelete(v)}>
                    <BsTrashFill />
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default VoucherTable;
