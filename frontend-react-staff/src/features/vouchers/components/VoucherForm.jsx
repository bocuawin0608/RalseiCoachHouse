import React, { useState, useEffect } from 'react';
import { Form, Row, Col, Alert } from 'react-bootstrap';
import { formatCurrency } from '../../../utils/formatters';

const VoucherForm = ({ initialData, isSubmitting, hasReferences, onSubmit, onBack }) => {
  const [formData, setFormData] = useState({
    voucherCode: '',
    discountType: 'PERCENT',
    discountValue: '',
    maxDiscountValue: '',
    minOrderValue: '',
    usageLimit: 0,
    startEffectiveDate: '',
    endEffectiveDate: '',
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (initialData) {
      setFormData({
        voucherCode: initialData.voucherCode || '',
        discountType: initialData.discountType || 'PERCENT',
        discountValue: initialData.discountValue ?? '',
        maxDiscountValue: initialData.maxDiscountValue ?? '',
        minOrderValue: initialData.minOrderValue ?? '',
        usageLimit: initialData.usageLimit ?? 0,
        startEffectiveDate: initialData.startEffectiveDate
          ? initialData.startEffectiveDate.substring(0, 16)
          : '',
        endEffectiveDate: initialData.endEffectiveDate
          ? initialData.endEffectiveDate.substring(0, 16)
          : '',
      });
    }
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      const updated = { ...prev, [name]: value };
      if (name === 'discountType' && value === 'FIXED') {
        updated.maxDiscountValue = prev.discountValue;
      }
      if (name === 'discountValue' && formData.discountType === 'FIXED') {
        updated.maxDiscountValue = value;
      }
      return updated;
    });
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.voucherCode.trim()) {
      newErrors.voucherCode = 'Vui lòng nhập mã voucher';
    } else if (!/^[a-zA-Z0-9]+$/.test(formData.voucherCode.trim())) {
      newErrors.voucherCode = 'Mã voucher chỉ được chứa chữ và số';
    } else if (formData.voucherCode.trim().length > 50) {
      newErrors.voucherCode = 'Mã voucher không được vượt quá 50 ký tự';
    }
    if (!formData.discountValue || parseFloat(formData.discountValue) <= 0) {
      newErrors.discountValue = 'Vui lòng nhập giá trị giảm hợp lệ';
    } else if (formData.discountType === 'PERCENT' && parseFloat(formData.discountValue) > 100) {
      newErrors.discountValue = 'Giảm theo phần trăm không thể vượt quá 100%';
    }
    if (!formData.startEffectiveDate) {
      newErrors.startEffectiveDate = 'Vui lòng chọn ngày bắt đầu';
    } else if (!initialData) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const start = new Date(formData.startEffectiveDate);
      start.setHours(0, 0, 0, 0);
      if (start < today) {
        newErrors.startEffectiveDate = 'Ngày bắt đầu phải là hôm nay hoặc trong tương lai';
      }
    }
    if (!formData.endEffectiveDate) {
      newErrors.endEffectiveDate = 'Vui lòng chọn ngày kết thúc';
    } else if (formData.startEffectiveDate && formData.endEffectiveDate) {
      if (new Date(formData.startEffectiveDate) >= new Date(formData.endEffectiveDate)) {
        newErrors.endEffectiveDate = 'Ngày kết thúc phải sau ngày bắt đầu';
      }
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;
    onSubmit({
      ...formData,
      discountValue: parseFloat(formData.discountValue),
      maxDiscountValue: formData.maxDiscountValue ? parseFloat(formData.maxDiscountValue) : null,
      minOrderValue: formData.minOrderValue ? parseFloat(formData.minOrderValue) : null,
      usageLimit: parseInt(formData.usageLimit, 10) || 0,
    });
  };

  const discountValueLabel = formData.discountType === 'PERCENT' ? 'Giá trị giảm (%)' : 'Giá trị giảm (VND)';

  const previewDiscount =
    formData.discountType === 'PERCENT'
      ? `${formData.discountValue || '—'}%`
      : formatCurrency(formData.discountValue || 0);

  const displayCode = formData.voucherCode || 'MAVOUCHER';
  const displayStart = formData.startEffectiveDate
    ? new Date(formData.startEffectiveDate).toLocaleDateString('vi-VN')
    : '—';
  const displayEnd = formData.endEffectiveDate
    ? new Date(formData.endEffectiveDate).toLocaleDateString('vi-VN')
    : '—';

  return (
    <Form onSubmit={handleSubmit}>
      <Row>
        <Col md={7}>
          <div className="voucher-form-card">
            <h5>Thông tin cơ bản</h5>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Mã voucher <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="text"
                    name="voucherCode"
                    value={formData.voucherCode}
                    onChange={handleChange}
                    disabled={hasReferences}
                    isInvalid={!!errors.voucherCode}
                  />
                  <Form.Control.Feedback type="invalid">{errors.voucherCode}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Loại giảm <span className="text-danger">*</span></Form.Label>
                  <Form.Select
                    name="discountType"
                    value={formData.discountType}
                    onChange={handleChange}
                    disabled={hasReferences}
                  >
                    <option value="PERCENT">Phần trăm (%)</option>
                    <option value="FIXED">Cố định (VND)</option>
                  </Form.Select>
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>{discountValueLabel} <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="number"
                    step="0.01"
                    name="discountValue"
                    value={formData.discountValue}
                    onChange={handleChange}
                    disabled={hasReferences}
                    isInvalid={!!errors.discountValue}
                  />
                  <Form.Control.Feedback type="invalid">{errors.discountValue}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Giá trị giảm tối đa</Form.Label>
                  <Form.Control
                    type="number"
                    step="0.01"
                    name="maxDiscountValue"
                    value={formData.maxDiscountValue}
                    onChange={handleChange}
                    readOnly={formData.discountType === 'FIXED'}
                  />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Giá trị đơn hàng tối thiểu</Form.Label>
                  <Form.Control
                    type="number"
                    step="0.01"
                    name="minOrderValue"
                    value={formData.minOrderValue}
                    onChange={handleChange}
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Giới hạn sử dụng</Form.Label>
                  <Form.Control
                    type="number"
                    name="usageLimit"
                    value={formData.usageLimit}
                    onChange={handleChange}
                  />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Ngày bắt đầu <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="datetime-local"
                    name="startEffectiveDate"
                    value={formData.startEffectiveDate}
                    onChange={handleChange}
                    isInvalid={!!errors.startEffectiveDate}
                    min={initialData ? undefined : new Date().toISOString().slice(0, 16)}
                  />
                  <Form.Control.Feedback type="invalid">{errors.startEffectiveDate}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Ngày kết thúc <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="datetime-local"
                    name="endEffectiveDate"
                    value={formData.endEffectiveDate}
                    onChange={handleChange}
                    isInvalid={!!errors.endEffectiveDate}
                  />
                  <Form.Control.Feedback type="invalid">{errors.endEffectiveDate}</Form.Control.Feedback>
                </Form.Group>
              </Col>
            </Row>
          </div>

          {initialData && (
            <div className="voucher-form-card">
              <h5>Thông tin voucher</h5>
              <Row>
                <Col md={3}>
                  <small className="text-muted">Đã dùng:</small>
                  <p className="mb-0 fw-bold">{initialData.usedCount ?? 0}</p>
                </Col>
                <Col md={3}>
                  <small className="text-muted">Ngày tạo:</small>
                  <p className="mb-0 fw-bold">{initialData.createdAt ? new Date(initialData.createdAt).toLocaleString('vi-VN') : '---'}</p>
                </Col>
                <Col md={3}>
                  <small className="text-muted">Trạng thái:</small>
                  <p className="mb-0 fw-bold">{initialData.status || '---'}</p>
                </Col>
                <Col md={3}>
                  <small className="text-muted">Đã tham chiếu:</small>
                  <p className="mb-0 fw-bold">{hasReferences ? 'Có' : 'Không'}</p>
                </Col>
              </Row>
            </div>
          )}

          <div className="d-flex gap-2">
            <button type="button" className="voucher-btn-secondary" onClick={onBack}>
              ← Quay lại
            </button>
            <button type="submit" className="voucher-btn-primary" disabled={isSubmitting}>
              {isSubmitting ? 'Đang xử lý…' : initialData ? 'Cập nhật' : 'Tạo mới'}
            </button>
          </div>
        </Col>

        <Col md={5}>
          <div className="voucher-receipt-card">
            <div className="voucher-receipt-header">Phiếu giảm giá</div>
            <div className="voucher-receipt-body">
              <div className="voucher-receipt-row">
                <span className="voucher-receipt-label">Mã</span>
                <span className="voucher-receipt-value voucher-code-tag">{displayCode}</span>
              </div>
              <div className="voucher-receipt-row">
                <span className="voucher-receipt-label">Loại</span>
                <span className="voucher-receipt-value">
                  {formData.discountType === 'PERCENT' ? 'Phần trăm' : 'Cố định'}
                </span>
              </div>
              <div className="voucher-receipt-row">
                <span className="voucher-receipt-label">Giảm</span>
                <span className="voucher-receipt-value">{previewDiscount}</span>
              </div>
              <div className="voucher-receipt-row">
                <span className="voucher-receipt-label">Giảm tối đa</span>
                <span className="voucher-receipt-value">
                  {formData.maxDiscountValue ? formatCurrency(formData.maxDiscountValue) : '—'}
                </span>
              </div>
              <div className="voucher-receipt-row">
                <span className="voucher-receipt-label">Đơn tối thiểu</span>
                <span className="voucher-receipt-value">
                  {formData.minOrderValue ? formatCurrency(formData.minOrderValue) : '—'}
                </span>
              </div>
              <div className="voucher-receipt-row">
                <span className="voucher-receipt-label">Bắt đầu</span>
                <span className="voucher-receipt-value">{displayStart}</span>
              </div>
              <div className="voucher-receipt-row">
                <span className="voucher-receipt-label">Kết thúc</span>
                <span className="voucher-receipt-value">{displayEnd}</span>
              </div>
            </div>
          </div>
        </Col>
      </Row>
    </Form>
  );
};

export default VoucherForm;
