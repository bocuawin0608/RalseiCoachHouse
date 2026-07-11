import React from 'react';
import { Modal, Alert } from 'react-bootstrap';

const VoucherDeleteModal = ({ isOpen, data, isSubmitting, error, onConfirm, onClose }) => {
  if (!data) return null;

  return (
    <Modal show={isOpen} onHide={onClose} centered dialogClassName="voucher-modal">
      <Modal.Header closeButton>
        <Modal.Title>Vô hiệu hóa voucher</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {error && <Alert variant="danger">{error}</Alert>}
        <p>
          Bạn có chắc chắn muốn vô hiệu hóa voucher <strong>"{data.voucherCode}"</strong>?
          Voucher sẽ không còn hiệu lực và không thể sử dụng cho các đơn hàng mới.
        </p>
      </Modal.Body>
      <Modal.Footer>
        <button type="button" className="voucher-btn-secondary" onClick={onClose} disabled={isSubmitting}>
          Hủy
        </button>
        <button
          type="button"
          className="voucher-btn-primary"
          style={{ background: '#e67e22' }}
          onClick={onConfirm}
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Đang xử lý…' : 'Vô hiệu hóa'}
        </button>
      </Modal.Footer>
    </Modal>
  );
};

export default VoucherDeleteModal;