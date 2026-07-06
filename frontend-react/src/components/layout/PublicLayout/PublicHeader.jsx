import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Navbar, Container, Nav, Button } from 'react-bootstrap';
import { FiBell, FiBox, FiChevronDown, FiClock, FiLogOut, FiMap, FiUser } from 'react-icons/fi';
import { useAuth } from '../../../features/auth';
import { HistoryNotificationDropdown } from '../../../features/customerHistory';
import './PublicHeader.css';

/**
 * Renders public navigation plus authenticated customer history shortcuts.
 */
export default function PublicHeader() {
    const navigate = useNavigate();
    const { user, logout } = useAuth();
    const [openPanel, setOpenPanel] = useState(null);
    const actionsRef = useRef(null);

    const isCustomer = user?.roles?.includes('CUSTOMER');

    useEffect(() => {
        /** Closes floating header panels when the customer clicks elsewhere. */
        const handleOutsideClick = (event) => {
            if (actionsRef.current && !actionsRef.current.contains(event.target)) setOpenPanel(null);
        };

        document.addEventListener('mousedown', handleOutsideClick);
        return () => document.removeEventListener('mousedown', handleOutsideClick);
    }, []);

    /** Opens a route and closes any active header panel. */
    const navigateFromMenu = (path) => {
        setOpenPanel(null);
        navigate(path);
    };

    /** Clears authentication before returning to the public homepage. */
    const handleLogout = () => {
        setOpenPanel(null);
        logout();
        navigate('/');
    };

    return (
        <Navbar expand="lg" className="public-header shadow-sm sticky-top py-2">
            <Container fluid className="public-header__container">
                <Navbar.Brand role="button" onClick={() => navigate('/')} className="d-flex align-items-center me-4">
                    <img
                        src="/images/ralseiiii.jpg"
                        alt="Logo Ralsei"
                        width="45"
                        height="45"
                        className="rounded shadow-sm"
                        style={{ objectFit: 'cover' }}
                        onError={(e) => { e.target.src = "https://placehold.co/150x150/2ecc71/ffffff?text=Ralsei"; }}
                    />
                </Navbar.Brand>

                <Navbar.Toggle aria-controls="public-navbar-nav" className="border-0 shadow-none" />

                <Navbar.Collapse id="public-navbar-nav">
                    <Nav className="me-auto gap-2">
                        <Button variant="link" className="text-decoration-none fw-bold" style={{ color: 'var(--ralsei-black)' }} onClick={() => navigate('/')}>Trang chủ</Button>
                    </Nav>

                    <Nav className="public-header__actions align-items-lg-center gap-2 mt-3 mt-lg-0" ref={actionsRef}>
                        {user ? (
                            <>
                                {isCustomer && (
                                    <>
                                        <button
                                            className="public-header__bell"
                                            type="button"
                                            aria-label="Mở lịch sử chuyến đi gần đây"
                                            aria-expanded={openPanel === 'history'}
                                            onClick={() => setOpenPanel((current) => current === 'history' ? null : 'history')}
                                        >
                                            <FiBell aria-hidden="true" />
                                            <span className="public-header__bell-dot" aria-hidden="true" />
                                        </button>
                                        {openPanel === 'history' && (
                                            <HistoryNotificationDropdown onClose={() => setOpenPanel(null)} />
                                        )}
                                    </>
                                )}
                                <Button
                                    variant="link"
                                    className="public-header__name d-flex align-items-center gap-2"
                                    onClick={() => setOpenPanel((current) => current === 'account' ? null : 'account')}
                                    aria-expanded={openPanel === 'account'}
                                >
                                    {user.username} <FiChevronDown aria-hidden="true" />
                                </Button>
                                {openPanel === 'account' && (
                                    <div className="public-header__account-menu">
                                        {isCustomer && <button type="button" onClick={() => navigateFromMenu('/profile')}><FiUser /> Thông tin cá nhân</button>}
                                        {isCustomer && (
                                            <div className="public-header__account-history">
                                                <button type="button" className="public-header__account-history-trigger">
                                                    <FiClock /> Lịch sử dịch vụ
                                                    <FiChevronDown className="public-header__account-history-chevron" aria-hidden="true" />
                                                </button>
                                                <div className="public-header__account-history-options">
                                                    <button type="button" onClick={() => navigateFromMenu('/history')}><FiMap /> Chuyến đi</button>
                                                    <button type="button" onClick={() => navigateFromMenu('/cargo-history')}><FiBox /> Đơn hàng</button>
                                                </div>
                                            </div>
                                        )}
                                        <button type="button" onClick={handleLogout}><FiLogOut /> Đăng xuất</button>
                                    </div>
                                )}
                            </>
                        ) : (
                            <>
                                <Button variant="light" className="fw-bold rounded-pill px-4" onClick={() => navigate('/login')}>Đăng nhập</Button>
                                <Button className="fw-bold border-0 rounded-pill px-4" style={{ backgroundColor: 'var(--ralsei-black)', color: 'white' }} onClick={() => navigate('/register')}>Đăng ký</Button>
                            </>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}
