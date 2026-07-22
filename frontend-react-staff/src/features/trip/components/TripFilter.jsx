import { BsArrowClockwise, BsCalendar3, BsFilter, BsSignpostSplit } from 'react-icons/bs';
import './TripFilter.css';

const STATUS_OPTIONS = [
    ['', 'Tất cả'],
    ['SCHEDULED', 'Đã lên lịch'],
    ['IN_PROGRESS', 'Đang chạy'],
    ['COMPLETED', 'Hoàn tất'],
    ['CANCELLED', 'Đã hủy']
];

export default function TripFilter({ filters, routes, onFilterChange, onReset }) {
    const emit = (name, value) => onFilterChange({ target: { name, value } });

    return (
        <section className="trip-filter-panel" aria-label="Bộ lọc chuyến xe">
            <div className="trip-filter-fields">
                <label className="trip-filter-field">
                    <span><BsCalendar3 /> Ngày vận hành</span>
                    <input type="date" name="date" value={filters.date || ''} onChange={onFilterChange} />
                </label>

                <label className="trip-filter-field trip-filter-field--route">
                    <span><BsSignpostSplit /> Tuyến đường</span>
                    <select name="routeId" value={filters.routeId || ''} onChange={onFilterChange}>
                        <option value="">Tất cả tuyến đường</option>
                        {routes.map((route) => (
                            <option key={route.routeId} value={route.routeId}>
                                {route.routeName.replace(/\s*-\s*/, ' → ')}
                            </option>
                        ))}
                    </select>
                </label>

                <label className="trip-filter-field">
                    <span><BsFilter /> Thời gian</span>
                    <select name="period" value={filters.period || ''} onChange={onFilterChange}>
                        <option value="">Cả ngày</option>
                        <option value="MORNING">Buổi sáng</option>
                        <option value="EVENING">Buổi chiều / tối</option>
                    </select>
                </label>

                <button type="button" className="trip-filter-reset" onClick={onReset} title="Đặt lại bộ lọc">
                    <BsArrowClockwise />
                    <span>Đặt lại</span>
                </button>
            </div>

            <div className="trip-status-filters" aria-label="Lọc trạng thái">
                {STATUS_OPTIONS.map(([value, label]) => (
                    <button
                        key={value || 'all'}
                        type="button"
                        className={filters.status === value ? 'is-active' : ''}
                        onClick={() => emit('status', value)}
                    >
                        {label}
                    </button>
                ))}
            </div>
        </section>
    );
}
