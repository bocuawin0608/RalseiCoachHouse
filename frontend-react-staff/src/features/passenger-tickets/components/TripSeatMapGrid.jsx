import SeatIcon from '../../../components/common/SeatIcon';

function resolveDisplayStatus(
    seat,
    currentTripSeatId,
    selectedTripSeatId,
    selectedTripSeatIds,
    blockedTripSeatIds,
    sessionAvailableTripSeatIds
) {
    if (Array.isArray(selectedTripSeatIds) && selectedTripSeatIds.includes(seat.tripSeatId)) {
        return 'SELECTED';
    }
    if (seat.tripSeatId === selectedTripSeatId) return 'SELECTED';
    if (seat.tripSeatId === currentTripSeatId) return 'CURRENT';
    if (Array.isArray(blockedTripSeatIds) && blockedTripSeatIds.includes(seat.tripSeatId)) {
        return 'LOCKED';
    }
    if (
        Array.isArray(sessionAvailableTripSeatIds)
        && sessionAvailableTripSeatIds.includes(seat.tripSeatId)
    ) {
        return 'AVAILABLE';
    }
    return seat.status;
}

export function buildSeatLayout(seatList) {
    if (!seatList?.length) {
        return null;
    }

    let maxFloor = 0;
    let maxRow = 0;
    let maxCol = 0;

    seatList.forEach((seat) => {
        if (seat.floorIndex > maxFloor) maxFloor = seat.floorIndex;
        if (seat.rowIndex > maxRow) maxRow = seat.rowIndex;
        if (seat.colIndex > maxCol) maxCol = seat.colIndex;
    });

    const floorMatrix = Array(maxFloor).fill().map(() =>
        Array(maxRow).fill().map(() => Array(maxCol).fill(null))
    );

    seatList.forEach((seat) => {
        floorMatrix[seat.floorIndex - 1][seat.rowIndex - 1][seat.colIndex - 1] = seat;
    });

    return {
        floors: floorMatrix,
        cols: maxCol,
        rows: maxRow,
    };
}

export default function TripSeatMapGrid({
    layout,
    currentTripSeatId,
    selectedTripSeatId,
    selectedTripSeatIds,
    blockedTripSeatIds,
    sessionAvailableTripSeatIds,
    maxSelectable,
    onSeatClick,
}) {
    const multiSelectMode = Array.isArray(selectedTripSeatIds);
    const blockedIds = Array.isArray(blockedTripSeatIds) ? blockedTripSeatIds : [];
    const sessionAvailableIds = Array.isArray(sessionAvailableTripSeatIds)
        ? sessionAvailableTripSeatIds
        : [];

    if (!layout?.floors?.length) {
        return <div className="text-muted text-center py-3">Không có sơ đồ ghế.</div>;
    }

    return (
        <div className="d-flex flex-wrap justify-content-center gap-4">
            {layout.floors.map((floorMatrix, floorIndex) => (
                <div key={floorIndex} className="text-center">
                    <p className="mb-3 text-muted fw-semibold small">
                        {floorIndex === 0 ? 'Tầng 1' : `Tầng ${floorIndex + 1}`}
                    </p>
                    <div
                        style={{
                            display: 'grid',
                            gridTemplateColumns: `repeat(${layout.cols}, 50px)`,
                            gap: '6px',
                        }}
                    >
                        {floorMatrix.map((row, rowIndex) =>
                            row.map((seat, colIndex) => {
                                if (!seat) {
                                    return <div key={`empty-${floorIndex}-${rowIndex}-${colIndex}`} />;
                                }

                                const displayStatus = resolveDisplayStatus(
                                    seat,
                                    currentTripSeatId,
                                    selectedTripSeatId,
                                    selectedTripSeatIds,
                                    blockedIds,
                                    sessionAvailableIds
                                );
                                const isSelected = multiSelectMode
                                    ? selectedTripSeatIds.includes(seat.tripSeatId)
                                    : seat.tripSeatId === selectedTripSeatId;
                                const isBlocked = blockedIds.includes(seat.tripSeatId) && !isSelected;
                                const isSessionAvailable = sessionAvailableIds.includes(seat.tripSeatId);
                                const selectionFull = multiSelectMode
                                    && maxSelectable != null
                                    && selectedTripSeatIds.length >= maxSelectable
                                    && !isSelected;
                                const isSelectable =
                                    (
                                        seat.status === 'AVAILABLE'
                                        || isSelected
                                        || isSessionAvailable
                                    )
                                    && seat.tripSeatId !== currentTripSeatId
                                    && !isBlocked
                                    && !selectionFull;

                                return (
                                    <div
                                        key={seat.tripSeatId}
                                        onClick={() => isSelectable && onSeatClick?.(seat)}
                                        title={
                                            isBlocked
                                                ? `Ghế ${seat.seatCode} (đã chọn cho hành khách khác)`
                                                : isSessionAvailable
                                                    ? `Ghế ${seat.seatCode} (trống trong phiên đổi vé)`
                                                    : `Ghế ${seat.seatCode}`
                                        }
                                        style={{
                                            cursor: isSelectable ? 'pointer' : 'default',
                                        }}
                                    >
                                        <SeatIcon
                                            status={displayStatus}
                                            code={seat.seatCode}
                                        />
                                    </div>
                                );
                            })
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
}
