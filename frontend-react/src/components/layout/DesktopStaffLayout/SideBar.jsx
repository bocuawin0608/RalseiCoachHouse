import { NavLink } from 'react-router-dom';

export default function Sidebar() {
    return (
        <aside style={{ width: '250px', background: '#1e293b', color: 'white', minHeight: '100vh', padding: '20px' }}>
            <h2>Manager Portal</h2>
            <nav style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '30px' }}>
                <NavLink to="/manager/dashboard" style={{ color: 'white' }}>Dashboard</NavLink>
                <NavLink to="/manager/coach-types" style={{ color: 'white' }}>Quản lý Loại xe</NavLink>
                <NavLink to="/manager/routes" style={{ color: 'white' }}>Quản lý Tuyến & Điểm dừng</NavLink>
                {/* Các menu khác thêm ở đây */}
            </nav>
        </aside>
    );
}