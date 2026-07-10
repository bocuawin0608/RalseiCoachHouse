import { useCallback, useState } from 'react';
import { Alert, Card, Container } from 'react-bootstrap';
import Pagination from '../../../components/common/Pagination';
import {
    useRefundList,
    RefundTabs,
    RefundSearchFilters,
    RefundListTable,
    RefundDetailModal,
    RefundConfirmPaymentModal,
} from '../../../features/refunds';
import { refundApi } from '../../../features/refunds/api/refundApi';
import { isPositiveInteger } from '../../../features/refunds/utils/refundFormatters';

export default function RefundListPage() {
    const {
        activeTab,
        filters,
        data,
        loading,
        error,
        pageInfo,
        setPageInfo,
        handleFilterChange,
        handleReset,
        handleSearch,
        handleTabChange,
        refreshList,
    } = useRefundList();

    const [selectedRefundId, setSelectedRefundId] = useState(null);
    const [detailRefund, setDetailRefund] = useState(null);
    const [detailLoading, setDetailLoading] = useState(false);
    const [detailError, setDetailError] = useState(null);
    const [showDetail, setShowDetail] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [confirmLoading, setConfirmLoading] = useState(false);
    const [confirmError, setConfirmError] = useState(null);

    const loadDetail = useCallback(async (refundId) => {
        if (!isPositiveInteger(refundId)) {
            setDetailError('Yêu cầu hoàn tiền không hợp lệ.');
            return;
        }

        setDetailLoading(true);
        setDetailError(null);

        try {
            const response = await refundApi.getPassengerDetail(refundId);
            setDetailRefund(response);
        } catch (requestError) {
            setDetailRefund(null);
            setDetailError(requestError.response?.data?.message || 'Không thể tải chi tiết hoàn tiền.');
        } finally {
            setDetailLoading(false);
        }
    }, []);

    const handleSelectRefund = (refund) => {
        if (!isPositiveInteger(refund?.refundId)) return;

        setSelectedRefundId(refund.refundId);
        setShowDetail(true);
        setShowConfirm(false);
        setConfirmError(null);
        loadDetail(refund.refundId);
    };

    const handleCloseDetail = () => {
        setShowDetail(false);
        setShowConfirm(false);
        setSelectedRefundId(null);
        setDetailRefund(null);
        setDetailError(null);
        setConfirmError(null);
    };

    const handleOpenConfirm = (refund) => {
        setDetailRefund(refund);
        setShowConfirm(true);
        setConfirmError(null);
    };

    const handleConfirmPayment = async (payload) => {
        if (!isPositiveInteger(selectedRefundId)) return;

        setConfirmLoading(true);
        setConfirmError(null);

        try {
            const updatedRefund = await refundApi.completePassenger(selectedRefundId, payload);
            setDetailRefund(updatedRefund);
            setShowConfirm(false);
            refreshList();
        } catch (requestError) {
            setConfirmError(requestError.response?.data?.message || 'Không thể xác nhận hoàn tiền.');
        } finally {
            setConfirmLoading(false);
        }
    };

    return (
        <Container fluid className="py-2" style={{ maxWidth: '1400px' }}>
            <div className="mb-4">
                <h2 className="fw-bold text-dark mb-1">Xử lý hoàn tiền</h2>
                <p className="text-muted mb-0">
                    Theo dõi và xác nhận các yêu cầu hoàn tiền hành khách.
                </p>
            </div>

            <RefundTabs activeTab={activeTab} onTabChange={handleTabChange} />

            {activeTab === 'passenger' ? (
                <>
                    <RefundSearchFilters
                        filters={filters}
                        onFilterChange={handleFilterChange}
                        onReset={handleReset}
                        onSearch={handleSearch}
                        searching={loading}
                    />

                    {error && <Alert variant="danger">{error}</Alert>}

                    <Card className="shadow-sm border-0">
                        <Card.Body className="p-0">
                            <RefundListTable
                                data={data}
                                loading={loading}
                                onSelectRefund={handleSelectRefund}
                            />

                            <div className="d-flex justify-content-center py-4 border-top">
                                <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                            </div>
                        </Card.Body>
                    </Card>
                </>
            ) : (
                <Alert variant="info" className="shadow-sm border-0">
                    Chức năng hoàn tiền hàng hóa đang được phát triển.
                </Alert>
            )}

            <RefundDetailModal
                isOpen={showDetail}
                refund={detailRefund}
                loading={detailLoading}
                error={detailError}
                onClose={handleCloseDetail}
                onConfirmPayment={handleOpenConfirm}
            />

            <RefundConfirmPaymentModal
                isOpen={showConfirm}
                refund={detailRefund}
                isSubmitting={confirmLoading}
                error={confirmError}
                onClose={() => {
                    setShowConfirm(false);
                    setConfirmError(null);
                }}
                onConfirm={handleConfirmPayment}
            />
        </Container>
    );
}
