import SeatIcon from '../../../components/common/SeatIcon';

export default function SeatMapBuilder({rows, cols, initialMatrix = [], selectedSeatIds = [], onSeatClick }) {

    if (rows === 0 || cols === 0) return <div className="text-muted">Xin lỗi vì sự cố. Vui lòng liên hệ nhà xe!</div>;

    return (
        <div 
            style={{ 
                display: 'grid', 
                gridTemplateColumns: `repeat(${cols}, 50px)`, 
                gap: '6px' 
            }}
        >
            {initialMatrix.map((row, rIndex) => 
                row.map((seat, cIndex) => {
                    
                    if (!seat) return (<div key={`walkway-${rIndex}-${cIndex}`} />);

                    const isAvailable = seat.status === 'AVAILABLE';
                    const isSelected = selectedSeatIds.includes(seat.tripSeatId);

                    return (
                        <div
                            key={seat.tripSeatId}
                            onClick={() => isAvailable && onSeatClick && onSeatClick(seat)}
                            title={`Ghế: ${seat.seatCode}`}
                            onMouseEnter={(e) => {
                                if (isAvailable) e.currentTarget.style.transform = 'scale(1.05)';
                            }}
                            onMouseLeave={(e) => {
                                if (isAvailable) e.currentTarget.style.transform = 'scale(1)';
                            }}
                        >
                            <SeatIcon status={isSelected ? 'SELECTED' : seat.status} code={seat.seatCode} />
                        </div>
                    );
                })
            )}
        </div>
    );
}