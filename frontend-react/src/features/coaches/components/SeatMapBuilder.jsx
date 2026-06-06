import { useState, useEffect } from 'react';
import SeatIcon from '../../../components/common/SeatIcon';

/**
 * Component dùng chung cho cả Create, Edit và View Sơ đồ ghế
 * @param {string} mode - 'VIEW' | 'EDIT' | 'CREATE'
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
                const newMatrix = Array(rows).fill().map(() => Array(cols).fill("empty"));
                setMatrix(newMatrix);
            }
        };
        load();
    }, [rows, cols, initialMatrix]);

    const handleCellClick = (rowIndex, colIndex) => {
        if (mode === 'VIEW') return; 

        const newMatrix = [...matrix];
        newMatrix[rowIndex][colIndex] = newMatrix[rowIndex][colIndex] === 'seat' ? 'empty' : 'seat';
        
        setMatrix(newMatrix);
        
        if (onChange) onChange(newMatrix); 
    };

    return (
        <div 
            className="seat-map-grid" 
            style={{ 
                display: 'grid', 
                gridTemplateColumns: `repeat(${cols}, 50px)`, 
                gap: '12px' 
            }}
        >
            {matrix.map((row, rIndex) => 
                row.map((cell, cIndex) => {
                    const isSeat = cell === 'seat';
                    
                    return (
                        <div
                            key={`${rIndex}-${cIndex}`}
                            onClick={() => handleCellClick(rIndex, cIndex)}
                            className={`
                                rounded shadow-sm d-flex justify-content-center align-items-center
                                ${isSeat ? 'bg-success text-white border-success' : 'bg-white border-secondary'}
                                ${mode !== 'VIEW' ? 'user-select-none' : ''}
                            `}
                            style={{
                                width: '50px', 
                                height: '50px',
                                border: '1px solid',
                                cursor: mode === 'VIEW' ? 'default' : 'pointer',
                                transition: 'all 0.2s ease-in-out',
                                opacity: isSeat ? 1 : 0.6
                            }}
                            onMouseEnter={(e) => {
                                if (mode !== 'VIEW') e.currentTarget.style.transform = 'scale(1.05)';
                            }}
                            onMouseLeave={(e) => {
                                if (mode !== 'VIEW') e.currentTarget.style.transform = 'scale(1)';
                            }}
                        >
                            <SeatIcon />
                        </div>
                    );
                })
            )}
        </div>
    );
}