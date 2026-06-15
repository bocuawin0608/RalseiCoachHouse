import React from 'react';
import { Modal, Alert } from 'react-bootstrap';

const VoucherDeleteModal = ({ isOpen, data, isSubmitting, error, onConfirm, onClose }) => {
  if (!data) return null;

  const hasReferences = data.hasReferences;

  return (
    <Modal show={isOpen} onHide={onClose} centered dialogClassName="voucher-modal">
      <Modal.Header closeButton>
        <Modal.Title>{hasReferences ? 'Vô hiệu hóa voucher' : 'Xóa voucher'}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {error && <Alert variant="danger">{error}</Alert>}
        {hasReferences ? (
          <p>
            Voucher <strong>"{data.voucherCode}"</strong> đã được sử dụng trong các vé.
            Không thể xóa vĩnh viễn. Thao tác này sẽ đặt ngày kết thúc (endEffectiveDate) về thời điểm hiện tại để vô hiệu hóa voucher.
          </p>
        ) : (
          <p>
            Bạn có chắc chắn muốn xóa voucher <strong>"{data.voucherCode}"</strong>?
            Hành động này không thể hoàn tác.
          </p>
        )}
      </Modal.Body>
      <Modal.Footer>
        <button type="button" className="voucher-btn-secondary" onClick={onClose} disabled={isSubmitting}>
          Hủy
        </button>
        <button
          type="button"
          className="voucher-btn-primary"
          style={{ background: hasReferences ? '#e67e22' : undefined }}
          onClick={onConfirm}
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Đang xử lý…' : hasReferences ? 'Vô hiệu hóa' : 'Xóa'}
        </button>
      </Modal.Footer>
    </Modal>
  );
};

export default VoucherDeleteModal;
