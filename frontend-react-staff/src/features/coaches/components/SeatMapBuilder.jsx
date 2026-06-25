import { useState, useEffect } from 'react';
import SeatIcon from '../../../components/common/SeatIcon';

/**
 * Component dùng chung cho cả Create, Edit và View bên SeatLayout của CoachType + Edit và View bên Seat của Coach
 * @param {string} mode - 'VIEW' | 'EDIT' | 'CREATE' | 'VIEW-SEAT' | 'EDIT-SEAT'
 * @param {number} rows - Tổng số hàng
 * @param {number} cols - Tổng số cột
 * @param {Array} initialMatrix - Ma trận ghế ban đầu (Dành cho View/Edit)
 * @param {Function} onChange - Callback bắn ma trận mới lên form cha
 */
export default function SeatMapBuilder({ mode = 'CREATE', rows, cols, initialMatrix, onChange }) {
    const [matrix, setMatrix] = useState([]);

    useEffect(() => {
        const load = () => {
            if (initialMatrix && initialMatrix.length > 0) {
                setMatrix(initialMatrix);
            } else if (rows > 0 && cols > 0) {
                const newMatrix = Array(rows).fill().map(() => Array(cols).fill("EMPTY"));
                setMatrix(newMatrix);
            }
        };
        load();
    }, [rows, cols, initialMatrix]);

    const objectMode = mode === 'VIEW-SEAT' || mode === 'EDIT-SEAT'

    const handleCellClick = (rowIndex, colIndex) => {
        if (mode === 'VIEW' || mode === 'VIEW-SEAT') return; 

        const newMatrix = [...matrix].map(row => [...row]);
        const currentCell = newMatrix[rowIndex][colIndex];

        if(objectMode) {
            if(currentCell) {
                newMatrix[rowIndex][colIndex] = {...currentCell, isActive: !currentCell.isActive}
            }
        } else {
            newMatrix[rowIndex][colIndex] = currentCell === 'SEAT' ? 'EMPTY' : 'SEAT';
        }
        
        setMatrix(newMatrix);
        
        if (onChange) onChange(newMatrix); 
    };

    const getCellProps = (cell) => {
        const isSeat = objectMode ? cell !== null : cell === 'SEAT';
        
        let bgClass = 'bg-white';
        let borderClass = 'border-secondary';
        let textClass = 'text-secondary';
        let seatCode = '';

        if (isSeat) {
            if (objectMode) {
                seatCode = cell.seatCode || '';
                if (cell.isActive) {
                    bgClass = 'bg-success';
                    borderClass = 'border-success';
                    textClass = 'text-white';
                } else {
                    bgClass = 'bg-danger'; 
                    borderClass = 'border-danger';
                    textClass = 'text-white';
                }
            } else {
                bgClass = 'bg-success';
                borderClass = 'border-success';
                textClass = 'text-white';
            }
        }

        return { isSeat, bgClass, borderClass, textClass, seatCode };
    };

    return (
        <div 
            style={{ 
                display: 'grid', 
                gridTemplateColumns: `repeat(${cols}, 50px)`, 
                gap: '12px' 
            }}
        >
            {matrix.map((row, rIndex) => 
                row.map((cell, cIndex) => {
                    
                    const {isSeat, bgClass, borderClass, textClass, seatCode} = getCellProps(cell);
                    
                    const isWalkway = objectMode && !isSeat;
                    if (isWalkway) {
                        return (
                            <div 
                                key={`${rIndex}-${cIndex}`}
                                style={{ width: '50px', height: '70px' }}
                            />
                        );
                    }
                    
                    const readMode = mode === 'VIEW-SEAT' || mode === 'VIEW' || (mode === 'EDIT-SEAT' && !isSeat)

                    return (
                        <div
                            key={`${rIndex}-${cIndex}`}
                            onClick={() => handleCellClick(rIndex, cIndex)}
                            className={`
                                rounded shadow-sm d-flex flex-column justify-content-center align-items-center
                                ${bgClass} ${borderClass} ${textClass}
                            `}
                            style={{
                                width: '50px', 
                                height: objectMode ? '70px' : '50px',
                                border: '1px solid',
                                cursor: readMode ? 'default' : 'pointer',
                                transition: 'all 0.2s ease-in-out',
                                opacity: isSeat ? 1 : 0.6
                            }}
                            title={seatCode ? `Ghế: ${seatCode}` : ''}
                            onMouseEnter={(e) => {
                                if (!readMode) e.currentTarget.style.transform = 'scale(1.05)';
                            }}
                            onMouseLeave={(e) => {
                                if (!readMode) e.currentTarget.style.transform = 'scale(1)';
                            }}
                        >
                            <SeatIcon />
                            {objectMode && isSeat && seatCode && (
                                <span style={{ fontSize: '0.65rem', fontWeight: 'bold', marginTop: '2px' }}>
                                    {seatCode}
                                </span>
                            )}
                        </div>
                    );
                })
            )}
        </div>
    );
}