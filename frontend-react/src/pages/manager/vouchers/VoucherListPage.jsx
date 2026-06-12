import React, { useState, useCallback } from 'react';
import { Container, Button, Alert, Pagination } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useVouchers } from '../../../features/vouchers';
import VoucherFilter from '../../../features/vouchers/components/VoucherFilter';
import VoucherTable from '../../../features/vouchers/components/VoucherTable';
import VoucherDetailModal from '../../../features/vouchers/components/VoucherDetailModal';
import VoucherDeleteModal from '../../../features/vouchers/components/VoucherDeleteModal';
import { voucherApi } from '../../../features/vouchers/api/voucherApi';

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
                <Pagination.Item key={i} active={i === pageInfo.pageNumber} onClick={() => handlePageChange(i)}>
                    {i + 1}
                </Pagination.Item>
            );
        }
        return <Pagination className="justify-content-center">{items}</Pagination>;
    };

    return (
        <Container fluid className="py-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="mb-0">Quản lý voucher</h4>
                <Button variant="primary" onClick={() => navigate('/management/vouchers/create')}>
                    + Thêm voucher mới
                </Button>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            <VoucherFilter
                filters={filters}
                onFilterChange={handleFilterChange}
                onReset={handleReset}
            />

            <VoucherTable
                vouchers={vouchers}
                loading={loading}
                onView={handleView}
                onEdit={handleEdit}
                onDelete={handleDeleteClick}
            />

            {renderPagination()}

            <VoucherDetailModal
                isOpen={showDetail}
                data={selectedVoucher}
                onClose={() => { setShowDetail(false); setSelectedVoucher(null); }}
            />

            <VoucherDeleteModal
                isOpen={showDelete}
                data={selectedVoucher}
                isSubmitting={deleteLoading}
                error={deleteError}
                onConfirm={handleDeleteConfirm}
                onClose={() => { setShowDelete(false); setSelectedVoucher(null); setDeleteError(null); }}
            />
        </Container>
    );
};

export default VoucherListPage;
