import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../features/auth';
import './MobileLayout.css';

const PAGE_TITLES = {
    '/staff/trip/list': 'Chuyến của tôi',
};

function resolveTitle(pathname) {
    if (PAGE_TITLES[pathname]) return PAGE_TITLES[pathname];
    if (pathname.includes('/dashboard')) return 'Điều khiển chuyến';
    if (pathname.includes('/scan')) return 'Quét QR';
    return 'Trip Staff';
}

export default function Header() {
    const navigate = useNavigate();
    const location = useLocation();
    const { user, logout } = useAuth();

    const showBack = location.pathname !== '/staff/trip/list' && !location.pathname.endsWith('/staff');

    const handleBack = () => {
        if (location.pathname.includes('/scan')) {
            const tripId = location.pathname.split('/')[3];
            navigate(`/staff/trip/${tripId}/dashboard`);
            return;
        }
        if (location.pathname.includes('/dashboard')) {
            navigate('/staff/trip/list');
            return;
        }
        navigate(-1);
    };

    return (
        <header className="mobile-staff-header">
            {showBack ? (
                <button type="button" className="mobile-staff-header-btn back" onClick={handleBack}>
                    ← Quay lại
                </button>
            ) : (
                <span className="mobile-staff-header-spacer" />
            )}

            <div className="mobile-staff-header-title">{resolveTitle(location.pathname)}</div>

            <button
                type="button"
                className="mobile-staff-header-btn logout"
                onClick={() => {
                    logout();
                    navigate('/staff/login');
                }}
            >
                {user?.username ? user.username.split('@')[0] : 'Logout'}
            </button>
        </header>
    );
}
