import React, { useState, useEffect } from 'react';
import { Alert, Spinner } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router-dom';
import VoucherForm from '../../../features/vouchers/components/VoucherForm';
import { voucherApi } from '../../../features/vouchers/api/voucherApi';
import '../../../features/vouchers/VouchersPage.css';

const EditVoucherPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [voucher, setVoucher] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [hasReferences, setHasReferences] = useState(false);

  useEffect(() => {
    const fetchVoucher = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await voucherApi.getById(id);
        setVoucher(response);
      } catch (err) {
        setError(err.response?.data?.message || 'Không thể tải thông tin voucher');
      } finally {
        setLoading(false);
      }
    };
    if (id) fetchVoucher();
  }, [id]);

  const handleSubmit = async (data) => {
    setIsSubmitting(true);
    setError(null);
    try {
      await voucherApi.update(id, data);
      navigate('/management/vouchers');
    } catch (err) {
      setError(err.response?.data?.message || 'Cập nhật voucher thất bại');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="voucher-form-page">
        <div className="voucher-loading">
          <Spinner animation="border" className="me-2" />
          Đang tải…
        </div>
      </div>
    );
  }

  if (error && !voucher) {
    return (
      <div className="voucher-form-page">
        <a href="/management/vouchers" className="voucher-form-back" onClick={(e) => { e.preventDefault(); navigate('/management/vouchers'); }}>
          ← Quay lại danh sách
        </a>
        <Alert variant="danger">{error}</Alert>
      </div>
    );
  }

  return (
    <div className="voucher-form-page">
      <a href="/management/vouchers" className="voucher-form-back" onClick={(e) => { e.preventDefault(); navigate('/management/vouchers'); }}>
        ← Quay lại danh sách
      </a>
      <div className="voucher-page-header">
        <h2>Chỉnh sửa voucher</h2>
      </div>
      {error && <Alert variant="danger">{error}</Alert>}
      <VoucherForm
        initialData={voucher}
        isSubmitting={isSubmitting}
        hasReferences={hasReferences}
        onSubmit={handleSubmit}
        onBack={() => navigate('/management/vouchers')}
      />
    </div>
  );
};

export default EditVoucherPage;
