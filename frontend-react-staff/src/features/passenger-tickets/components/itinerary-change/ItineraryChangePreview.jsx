import { Alert, Badge, Spinner } from 'react-bootstrap';
import { formatCurrency } from '../../utils/passengerTicketFormatters';

export default function ItineraryChangePreview({ workflow }) {
    const {
        preview,
        previewing,
        hasNoChanges,
        pickupStopId,
        dropoffStopId,
    } = workflow;

    return (
        <>
            {previewing && (
                <div className="mt-3 small text-muted">
                    <Spinner animation="border" size="sm" className="me-2" />
                    Đang kiểm tra điều kiện giá...
                </div>
            )}

            {preview && !previewing && (
                <div className="mt-3 p-3 border rounded bg-white">
                    <div className="d-flex flex-wrap justify-content-between gap-2 mb-2">
                        <span>Giá đã thanh toán: <strong>{formatCurrency(preview.originalNetPaid)}</strong></span>
                        <span>Giá mới: <strong>{formatCurrency(preview.newNetPaid)}</strong></span>
                    </div>
                    <Badge bg={preview.eligible ? 'success' : 'danger'}>
                        {preview.eligible
                            ? 'Đủ điều kiện đổi hành trình'
                            : (preview.ineligibleReason || 'Không đủ điều kiện')}
                    </Badge>
                </div>
            )}

            {hasNoChanges && pickupStopId && dropoffStopId && (
                <Alert variant="secondary" className="mt-3 mb-0 py-2 px-3 border-0 small">
                    Hành trình không thay đổi so với vé hiện tại. Không cần lưu.
                </Alert>
            )}
        </>
    );
}
