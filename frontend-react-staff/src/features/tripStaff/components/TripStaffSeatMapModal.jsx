import { useMemo } from 'react';
import { Modal } from 'react-bootstrap';
import TripStaffSeatIcon from './TripStaffSeatIcon';
import './TripStaff.css';

function buildMatrix(seats) {
    if (!seats?.length) return { floors: [], rows: 0, cols: 0 };

    let maxFloor = 0;
    let maxRow = 0;
    let maxCol = 0;

    seats.forEach((seat) => {
        if (seat.floorIndex > maxFloor) maxFloor = seat.floorIndex;
        if (seat.rowIndex > maxRow) maxRow = seat.rowIndex;
        if (seat.colIndex > maxCol) maxCol = seat.colIndex;
    });

    const floors = Array.from({ length: maxFloor }, () =>
        Array.from({ length: maxRow }, () => Array(maxCol).fill(null))
    );

    seats.forEach((seat) => {
        floors[seat.floorIndex - 1][seat.rowIndex - 1][seat.colIndex - 1] = seat;
    });

    return { floors, rows: maxRow, cols: maxCol };
}

export default function TripStaffSeatMapModal({ show, onHide, seats }) {
    const parsed = useMemo(() => buildMatrix(seats), [seats]);

    return (
        <Modal show={show} onHide={onHide} fullscreen="sm-down" centered>
            <Modal.Header closeButton>
                <Modal.Title>Sơ đồ ghế</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <div className="d-flex gap-3 mb-3 flex-wrap" style={{ fontSize: '13px' }}>
                    <span><TripStaffSeatIcon status={null} /> Trống</span>
                    <span><TripStaffSeatIcon status="CONFIRMED" /> Chưa lên xe</span>
                    <span><TripStaffSeatIcon status="CHECKED_IN" /> Đã lên xe</span>
                    <span><TripStaffSeatIcon noShow /> Vắng mặt</span>
                </div>
                {parsed.floors.length === 0 ? (
                    <p className="text-muted text-center">Không có dữ liệu ghế</p>
                ) : (
                    parsed.floors.map((floor, index) => (
                        <div key={index} className="mb-4 text-center">
                            <p className="text-muted fw-semibold mb-2">{index === 0 ? 'Tầng 1' : `Tầng ${index + 1}`}</p>
                            <div
                                style={{
                                    display: 'grid',
                                    gridTemplateColumns: `repeat(${parsed.cols}, 36px)`,
                                    gap: '6px',
                                    justifyContent: 'center',
                                }}
                            >
                                {floor.flatMap((row, rIndex) =>
                                    row.map((seat, cIndex) => {
                                        if (!seat) {
                                            return <div key={`empty-${rIndex}-${cIndex}`} />;
                                        }
                                        return (
                                            <div key={seat.tripSeatId} title={seat.fullName || seat.seatCode}>
                                                <TripStaffSeatIcon
                                                    status={seat.passengerDetailStatus}
                                                    code={seat.seatCode}
                                                    noShow={seat.noShow}
                                                />
                                            </div>
                                        );
                                    })
                                )}
                            </div>
                        </div>
                    ))
                )}
            </Modal.Body>
        </Modal>
    );
}
