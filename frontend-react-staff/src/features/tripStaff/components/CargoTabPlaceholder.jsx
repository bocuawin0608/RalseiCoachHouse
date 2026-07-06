import { useCallback, useEffect, useState } from 'react';
import { Alert, Badge, Button, Spinner } from 'react-bootstrap';
import { tripStaffApi } from '../api/tripStaffApi';
import { formatCurrency } from '../../../utils/formatters';

const STATUS_BADGE_VARIANT = {
    RECEIVED: 'secondary',
    LOADED: 'primary',
    ARRIVED: 'info',
    DELIVERED: 'success',
    CANCELLED: 'danger',
    REJECTED: 'danger',
    ABANDONED: 'dark',
};

const STATUS_LABEL = {
    RECEIVED: 'Mới nhận',
    LOADED: 'Đã lên xe',
    ARRIVED: 'Đã dỡ xuống',
    DELIVERED: 'Đã giao',
    CANCELLED: 'Đã hủy',
    REJECTED: 'Từ chối',
    ABANDONED: 'Bỏ hoang',
};

export default function CargoTabPlaceholder({ tripId }) {
    const [cargoData, setCargoData] = useState({ cargoItems: [] });
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(null);
    const [error, setError] = useState(null);

    const fetchCargo = useCallback(async () => {
        if (!tripId) return;
        setLoading(true);
        setError(null);
        try {
            const data = await tripStaffApi.getCargoList(tripId);
            setCargoData(data || { cargoItems: [] });
        } catch (err) {
            setError(err.response?.data?.message || 'Không thể tải danh sách hàng hóa');
            setCargoData({ cargoItems: [] });
        } finally {
            setLoading(false);
        }
    }, [tripId]);

    useEffect(() => {
        fetchCargo();
    }, [fetchCargo]);

    const handleAction = async (action, cargoTicketId) => {
        setActionLoading(cargoTicketId);
        setError(null);
        try {
            if (action === 'load') await tripStaffApi.loadCargo(tripId, cargoTicketId);
            else if (action === 'unload') await tripStaffApi.unloadCargo(tripId, cargoTicketId);
            else if (action === 'deliver') await tripStaffApi.deliverCargo(tripId, cargoTicketId);
            await fetchCargo();
        } catch (err) {
            setError(err.response?.data?.message || 'Thao tác thất bại');
        } finally {
            setActionLoading(null);
        }
    };

    const items = cargoData.cargoItems || [];

    return (
        <div>
            {error && <Alert variant="danger" dismissible onClose={() => setError(null)}>{error}</Alert>}

            {loading ? (
                <div className="text-center py-5">
                    <Spinner animation="border" />
                </div>
            ) : items.length === 0 ? (
                <div className="text-center py-5 text-muted">
                    <p className="mb-0 fw-semibold">Chưa có đơn hàng nào</p>
                    <p className="mb-0" style={{ fontSize: '14px' }}>Không có hàng hóa cần xử lý cho chuyến này.</p>
                </div>
            ) : (
                items.map((item) => (
                    <div key={item.cargoTicketId} className="passenger-card">
                        <div className="d-flex justify-content-between align-items-start gap-2 mb-2">
                            <div>
                                <div className="fw-bold">{item.ticketCode}</div>
                                <div className="text-muted" style={{ fontSize: '13px' }}>
                                    {item.senderName} ({item.senderPhone}) → {item.receiverName} ({item.receiverPhone})
                                </div>
                            </div>
                            <Badge bg={STATUS_BADGE_VARIANT[item.status] || 'secondary'} className="text-nowrap">
                                {STATUS_LABEL[item.status] || item.status}
                            </Badge>
                        </div>

                        <div className="text-muted mb-1" style={{ fontSize: '13px' }}>
                            {item.pickupStopName} → {item.dropoffStopName}
                        </div>

                        <div className="text-muted mb-2" style={{ fontSize: '13px' }}>
                            Tổng cước: {formatCurrency(item.totalPrice)}
                            {item.feePayer === 'RECEIVER' && ' (người nhận trả)'}
                            {item.feePayer === 'SENDER' && ' (người gửi trả)'}
                            {item.codAmount > 0 && ` · COD: ${formatCurrency(item.codAmount)}`}
                        </div>

                        {item.details?.length > 0 && (
                            <div className="mb-2" style={{ fontSize: '13px' }}>
                                {item.details.map((d, i) => (
                                    <div key={d.cargoTicketDetailId || i} className="text-muted">
                                        {d.quantity}x {d.description || 'Hàng hóa'}
                                        {d.weightKg > 0 && ` · ${d.weightKg}kg`}
                                        {d.unit && ` · Đơn giá: ${formatCurrency(d.calculatedPrice)}/${d.unit}`}
                                    </div>
                                ))}
                            </div>
                        )}

                        {item.description && (
                            <div className="text-muted mb-2" style={{ fontSize: '13px', fontStyle: 'italic' }}>
                                Ghi chú: {item.description}
                            </div>
                        )}

                        <div className="d-flex gap-2 mt-3 flex-wrap">
                            {item.status === 'RECEIVED' && (
                                <Button
                                    size="sm"
                                    className="custom-btn-general"
                                    disabled={actionLoading === item.cargoTicketId}
                                    onClick={() => handleAction('load', item.cargoTicketId)}
                                >
                                    {actionLoading === item.cargoTicketId ? 'Đang xử lý...' : 'Xác nhận lên xe'}
                                </Button>
                            )}
                            {item.status === 'LOADED' && (
                                <Button
                                    size="sm"
                                    variant="warning"
                                    disabled={actionLoading === item.cargoTicketId}
                                    onClick={() => handleAction('unload', item.cargoTicketId)}
                                >
                                    {actionLoading === item.cargoTicketId ? 'Đang xử lý...' : 'Dỡ hàng'}
                                </Button>
                            )}
                            {item.status === 'ARRIVED' && (
                                <Button
                                    size="sm"
                                    variant="success"
                                    disabled={actionLoading === item.cargoTicketId}
                                    onClick={() => handleAction('deliver', item.cargoTicketId)}
                                >
                                    {actionLoading === item.cargoTicketId ? 'Đang xử lý...' : 'Xác nhận đã giao'}
                                </Button>
                            )}
                        </div>
                    </div>
                ))
            )}
        </div>
    );
}
