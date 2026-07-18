import { Spinner } from 'react-bootstrap';
import SeatIcon from '../../../../components/common/SeatIcon';
import TripSeatMapGrid from '../TripSeatMapGrid';

export default function TransferSeatSelection({ workflow }) {
    const {
        confirmedSeatCount,
        selectedTripSeatIds,
        loadingSeats,
        layout,
        handleSeatClick,
    } = workflow;

    return (
        <>
            <div className="mb-2 small text-muted">
                Chọn {confirmedSeatCount} ghế trên chuyến mới ({selectedTripSeatIds.length}/{confirmedSeatCount})
            </div>

            <div className="d-flex flex-wrap gap-3 mb-3 small text-muted justify-content-center">
                <span className="d-inline-flex align-items-center gap-2">
                    <SeatIcon status="SELECTED" code="" /> Đã chọn
                </span>
                <span className="d-inline-flex align-items-center gap-2">
                    <SeatIcon status="AVAILABLE" code="" /> Còn trống
                </span>
                <span className="d-inline-flex align-items-center gap-2">
                    <SeatIcon status="SOLD" code="" /> Đã đặt
                </span>
            </div>

            {loadingSeats ? (
                <div className="py-3 text-center">
                    <Spinner animation="border" size="sm" className="me-2" />
                    Đang tải sơ đồ ghế...
                </div>
            ) : (
                <TripSeatMapGrid
                    layout={layout}
                    selectedTripSeatIds={selectedTripSeatIds}
                    maxSelectable={confirmedSeatCount}
                    onSeatClick={handleSeatClick}
                />
            )}
        </>
    );
}
