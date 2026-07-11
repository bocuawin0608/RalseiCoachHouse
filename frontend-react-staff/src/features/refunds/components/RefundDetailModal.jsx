import { Alert, Button, Modal } from 'react-bootstrap';
import {
    formatCurrency,
    formatDateTime,
    formatRefundMethod,
    formatRefundStatus,
} from '../utils/refundFormatters';

function DetailRow({ label, value }) {
    return (
        <div className="mb-2">
            <div className="text-muted small">{label}</div>
            <div className="fw-medium">{value || '—'}</div>
        </div>
    );
}

export default function RefundDetailModal({
    isOpen,
    refund,
    loading,
    error,
    onClose,
    onConfirmPayment,
}) {
    if (!isOpen) return null;

    const bankDestination = refund?.bankDestination;
    const canConfirm = refund?.status === 'PENDING';

    return (
        <Modal show={isOpen} onHide={onClose} centered size="lg" backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title>Chi tiết yêu cầu hoàn tiền</Modal.Title>
            </Modal.Header>

            <Modal.Body>
                {loading && <div className="text-center py-4 text-muted">Đang tải chi tiết…</div>}
                {error && <Alert variant="danger">{error}</Alert>}

                {!loading && refund && (
                    <>
                        <div className="row g-4">
                            <div className="col-md-6">
                                <h6 className="fw-bold text-secondary mb-3">Thông tin hoàn tiền</h6>
                                <DetailRow label="Mã vé" value={refund.ticketCode} />
                                <DetailRow label="Khách hàng" value={refund.customerName} />
                                <DetailRow label="SĐT" value={refund.customerPhone} />
                                <DetailRow label="Số tiền hoàn" value={formatCurrency(refund.amount)} />
                                <DetailRow label="Phương thức" value={formatRefundMethod(refund.refundMethod)} />
                                <DetailRow label="Trạng thái" value={formatRefundStatus(refund.status)} />
                                <DetailRow label="Lý do" value={refund.reason} />
                            </div>

                            <div className="col-md-6">
                                <h6 className="fw-bold text-secondary mb-3">Tài khoản nhận hoàn</h6>
                                {bankDestination ? (
                                    <>
                                        <DetailRow label="Ngân hàng" value={bankDestination.bankName} />
                                        <DetailRow label="Chủ tài khoản" value={bankDestination.accountHolder} />
                                        <DetailRow label="Số tài khoản" value={bankDestination.accountNumber} />
                                    </>
                                ) : (
                                    <Alert variant="warning" className="py-2 px-3 border-0">
                                        Không đọc được thông tin tài khoản nhận hoàn.
                                    </Alert>
                                )}

                                <h6 className="fw-bold text-secondary mb-3 mt-4">Theo dõi xử lý</h6>
                                <DetailRow label="Ngày tạo yêu cầu" value={formatDateTime(refund.createdAt)} />
                                <DetailRow label="Người tạo yêu cầu" value={refund.createdByStaffDisplay} />

                                {refund.status === 'COMPLETED' && (
                                    <>
                                        <DetailRow label="Thời gian hoàn" value={formatDateTime(refund.refundTime)} />
                                        <DetailRow label="Mã giao dịch" value={refund.transactionId} />
                                        <DetailRow label="Người xác nhận" value={refund.updatedByStaffDisplay} />
                                    </>
                                )}
                            </div>
                        </div>
                    </>
                )}
            </Modal.Body>

            <Modal.Footer>
                <Button variant="outline-secondary" onClick={onClose}>
                    Đóng
                </Button>
                {canConfirm && refund && (
                    <Button variant="success" onClick={() => onConfirmPayment(refund)}>
                        Xác nhận đã thanh toán
                    </Button>
                )}
            </Modal.Footer>
        </Modal>
    );
}
