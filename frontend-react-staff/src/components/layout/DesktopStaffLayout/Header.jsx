import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../features/auth';
import { Button } from 'react-bootstrap';
import { BsList } from 'react-icons/bs';
import { useSidebar } from './SidebarContext';

export default function Header() {
    const navigate = useNavigate();
    const { logout, user } = useAuth();
    const { isCollapsed, toggleSidebar } = useSidebar();
    const profilePath = user?.roles?.some((role) => ['ADMIN', 'MANAGER'].includes(role))
        ? '/management/profile'
        : '/staff/profile';

    return (
        <header className="desktop-staff-header">
            <div className="desktop-staff-header-left">
                <button
                    type="button"
                    onClick={toggleSidebar}
                    className="btn btn-link text-dark p-1"
                    style={{ outline: 'none', boxShadow: 'none' }}
                    aria-label={isCollapsed ? 'Mở sidebar' : 'Ẩn sidebar'}
                    title={isCollapsed ? 'Mở menu' : 'Ẩn menu'}
                >
                    <BsList size={24} />
                </button>
            </div>

            <div className="desktop-staff-header-right">
                <div style={{ fontWeight: 'bold' }}>Xin chào, {user?.username || 'User'}!</div>
                <Button variant="outline-secondary" onClick={() => navigate(profilePath)}>Hồ sơ</Button>
                <Button className="custom-btn-general" onClick={() => { logout(); navigate('/staff/login'); }}>Đăng xuất</Button>
            </div>
        </header>
    );
}
