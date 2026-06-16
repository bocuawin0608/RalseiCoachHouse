import React, { useState, useCallback, useMemo } from 'react';
import { Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useVouchers } from '../../../features/vouchers';
import VoucherFilter from '../../../features/vouchers/components/VoucherFilter';
import VoucherTable from '../../../features/vouchers/components/VoucherTable';
import VoucherDetailModal from '../../../features/vouchers/components/VoucherDetailModal';
import VoucherDeleteModal from '../../../features/vouchers/components/VoucherDeleteModal';
import { voucherApi } from '../../../features/vouchers/api/voucherApi';
import '../../../features/vouchers/VouchersPage.css';

const VoucherListPage = () => {
  const navigate = useNavigate();
  const {
    vouchers,
    loading,
    error,
    pageInfo,
    filters,
    handleFilterChange,
    handleReset,
    handlePageChange,
    fetchVouchers,
  } = useVouchers();

  const [selectedVoucher, setSelectedVoucher] = useState(null);
  const [showDetail, setShowDetail] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [deleteError, setDeleteError] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const metrics = useMemo(() => {
    const active = vouchers.filter((v) => v.status === 'ACTIVE').length;
    const upcoming = vouchers.filter((v) => v.status === 'UPCOMING').length;
    const expired = vouchers.filter((v) => v.status === 'EXPIRED').length;
    const exhausted = vouchers.filter((v) => v.status === 'EXHAUSTED').length;
    return {
      total: vouchers.length,
      active,
      expiredOrExhausted: expired + exhausted,
      upcoming,
    };
  }, [vouchers]);

  const handleView = (voucher) => {
    setSelectedVoucher(voucher);
    setShowDetail(true);
  };

  const handleEdit = (voucher) => {
    navigate(`/management/vouchers/${voucher.voucherId}/edit`);
  };

  const handleDeleteClick = (voucher) => {
    setSelectedVoucher(voucher);
    setDeleteError(null);
    setShowDelete(true);
  };

  const handleDeleteConfirm = useCallback(async () => {
    if (!selectedVoucher) return;
    setDeleteLoading(true);
    setDeleteError(null);
    try {
      await voucherApi.delete(selectedVoucher.voucherId);
      setShowDelete(false);
      setSelectedVoucher(null);
      fetchVouchers(filters);
    } catch (err) {
      setDeleteError(err.response?.data?.message || 'Xóa voucher thất bại');
    } finally {
      setDeleteLoading(false);
    }
  }, [selectedVoucher, fetchVouchers, filters]);

  const renderPagination = () => {
    if (pageInfo.totalPages <= 1) return null;
    const items = [];
    for (let i = 0; i < pageInfo.totalPages; i++) {
      items.push(
        <button
          key={i}
          className={i === pageInfo.pageNumber ? 'active' : ''}
          onClick={() => handlePageChange(i)}
        >
          {i + 1}
        </button>
      );
    }
    return (
      <div className="voucher-pagination">
        <button disabled={pageInfo.pageNumber === 0} onClick={() => handlePageChange(pageInfo.pageNumber - 1)}>
          ‹
        </button>
        {items}
        <button disabled={pageInfo.pageNumber >= pageInfo.totalPages - 1} onClick={() => handlePageChange(pageInfo.pageNumber + 1)}>
          ›
        </button>
      </div>
    );
  };

  return (
    <div className="voucher-page">
      <div className="voucher-page-header">
        <h2>Quản lý voucher</h2>
        <button className="voucher-btn-primary" onClick={() => navigate('/management/vouchers/create')}>
          + Thêm voucher
        </button>
      </div>

      <div className="voucher-metrics">
        <div className="voucher-metric-card">
          <div className="voucher-metric-value">{metrics.active}</div>
          <div className="voucher-metric-label">Đang hoạt động</div>
        </div>
        <div className="voucher-metric-card">
          <div className="voucher-metric-value">{metrics.upcoming}</div>
          <div className="voucher-metric-label">Sắp diễn ra</div>
        </div>
        <div className="voucher-metric-card">
          <div className="voucher-metric-value" style={{ color: '#e67e22' }}>{metrics.expiredOrExhausted}</div>
          <div className="voucher-metric-label">Hết hạn / Hết lượt</div>
        </div>
        <div className="voucher-metric-card">
          <div className="voucher-metric-value">{metrics.total}</div>
          <div className="voucher-metric-label">Tổng cộng</div>
        </div>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      <VoucherFilter filters={filters} onFilterChange={handleFilterChange} onReset={handleReset} />

      <VoucherTable vouchers={vouchers} loading={loading} onView={handleView} onEdit={handleEdit} onDelete={handleDeleteClick} />

      {renderPagination()}

      <VoucherDetailModal isOpen={showDetail} data={selectedVoucher} onClose={() => { setShowDetail(false); setSelectedVoucher(null); }} />

      <VoucherDeleteModal
        isOpen={showDelete}
        data={selectedVoucher}
        isSubmitting={deleteLoading}
        error={deleteError}
        onConfirm={handleDeleteConfirm}
        onClose={() => { setShowDelete(false); setSelectedVoucher(null); setDeleteError(null); }}
      />
    </div>
  );
};

export default VoucherListPage;
