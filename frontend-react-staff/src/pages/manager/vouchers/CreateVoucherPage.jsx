import React, { useState } from 'react';
import { Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import VoucherForm from '../../../features/vouchers/components/VoucherForm';
import { voucherApi } from '../../../features/vouchers/api/voucherApi';
import '../../../features/vouchers/VouchersPage.css';

const CreateVoucherPage = () => {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (data) => {
    setIsSubmitting(true);
    setError(null);
    try {
      await voucherApi.create(data);
      navigate('/management/vouchers');
    } catch (err) {
      setError(err.response?.data?.message || 'Tạo voucher thất bại');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="voucher-form-page">
      <a href="/management/vouchers" className="voucher-form-back" onClick={(e) => { e.preventDefault(); navigate('/management/vouchers'); }}>
        ← Quay lại danh sách
      </a>
      <div className="voucher-page-header">
        <h2>Thêm mới voucher</h2>
      </div>
      {error && <Alert variant="danger">{error}</Alert>}
      <VoucherForm isSubmitting={isSubmitting} onSubmit={handleSubmit} onBack={() => navigate('/management/vouchers')} />
    </div>
  );
};

export default CreateVoucherPage;
