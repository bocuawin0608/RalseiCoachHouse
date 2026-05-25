export default function SeatLayoutFilter({ filters, onFilterChange, onSearch }) {
    return (
        <div style={{ display: 'flex', gap: '15px', marginBottom: '20px', flexWrap: 'wrap', alignItems: 'center' }}>
            
            <input 
                name="seatLayoutName" 
                placeholder="Tên sơ đồ..." 
                value={filters.seatLayoutName || ''}
                onChange={onFilterChange}
                style={{ padding: '5px' }}
            />
            
            <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                <input 
                    type="number"
                    name="minPrice" 
                    placeholder="Giá từ..." 
                    value={filters.minPrice || ''}
                    onChange={onFilterChange}
                    style={{ padding: '5px', width: '100px' }}
                />
                <span>-</span>
                <input 
                    type="number"
                    name="maxPrice" 
                    placeholder="Đến giá..." 
                    value={filters.maxPrice || ''}
                    onChange={onFilterChange}
                    style={{ padding: '5px', width: '100px' }}
                />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                <input 
                    type="number"
                    name="minSeats" 
                    placeholder="Số ghế từ..." 
                    value={filters.minSeats || ''}
                    onChange={onFilterChange}
                    style={{ padding: '5px', width: '100px' }}
                />
                <span>-</span>
                <input 
                    type="number"
                    name="maxSeats" 
                    placeholder="Đến..." 
                    value={filters.maxSeats || ''}
                    onChange={onFilterChange}
                    style={{ padding: '5px', width: '100px' }}
                />
            </div>

            <select 
                name="isActive" 
                value={filters.isActive || ''} 
                onChange={onFilterChange}
                style={{ padding: '5px' }}
            >
                <option value="">Tất cả trạng thái</option>
                <option value="true">Đang hoạt động</option>
                <option value="false">Ngừng hoạt động</option>
            </select>

            <button 
                onClick={onSearch} 
                style={{ padding: '5px 15px', cursor: 'pointer', fontWeight: 'bold' }}
            >
                Tìm kiếm
            </button>
        </div>
    );
}