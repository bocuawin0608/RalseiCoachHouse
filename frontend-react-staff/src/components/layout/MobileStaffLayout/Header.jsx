import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../../features/auth";

export default function Header() {
    const navigate = useNavigate();
    const location = useLocation();
    const { user, logout } = useAuth();

    const isOnList = location.pathname === '/staff/trip/list';
    const showBack = !isOnList;

    const handleBack = () => {
        navigate('/staff/trip/list');
    };

    return (
        <header style={{
            height: '56px',
            background: 'var(--ralsei-primary)',
            color: '#fff',
            display: 'flex',
            alignItems: 'center',
            padding: '0 12px',
            justifyContent: 'space-between',
            gap: '8px',
            position: 'sticky',
            top: 0,
            zIndex: 40,
        }}>
            {showBack ? (
                <button
                    type="button"
                    onClick={handleBack}
                    style={{
                        background: 'transparent',
                        border: 'none',
                        color: '#fff',
                        fontSize: '14px',
                        padding: '6px 8px',
                        minWidth: '72px',
                        textAlign: 'left',
                    }}
                >
                    &larr; Danh sách
                </button>
            ) : (
                <span style={{ minWidth: '72px' }} />
            )}

            <div style={{ fontWeight: 700, fontSize: '15px', flex: 1, textAlign: 'center' }}>
                {isOnList ? 'Chuyến của tôi' : 'Điều khiển chuyến'}
            </div>

            <div style={{ display: 'flex', gap: '4px', minWidth: '72px', justifyContent: 'flex-end' }}>
                <button
                    type="button"
                    onClick={() => navigate('/staff/profile')}
                    style={{
                        background: 'transparent',
                        border: 'none',
                        color: '#fff',
                        fontSize: '14px',
                        padding: '6px 8px',
                    }}
                >
                    Hồ sơ
                </button>
                <button
                    type="button"
                    onClick={() => { logout(); navigate('/staff/login'); }}
                    style={{
                        background: 'transparent',
                        border: 'none',
                        color: '#fff',
                        fontSize: '14px',
                        padding: '6px 8px',
                    }}
                >
                    {user?.username ? user.username.split('@')[0] : 'Logout'}
                </button>
            </div>
        </header>
    );
}
