import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { Collapse } from 'react-bootstrap';
import {
    BsList, BsGrid1X2, BsBusFront, BsSignpostSplit,
    BsChevronDown, BsChevronRight, BsTags, BsGeoAlt,
    BsBoxSeam, BsCashCoin, BsGift, BsPeopleFill, BsShieldCheck, BsPersonBadge, BsBuilding, BsTicketPerforated, BsInfoCircle
} from 'react-icons/bs';
import { useAuth } from '../../../features/auth';

export default function Sidebar() {
    const [isCollapsed, setIsCollapsed] = useState(false);
    const [openMenu, setOpenMenu] = useState('');

    const { user } = useAuth();
    const userRoles = user?.roles || [];
    const hasAccess = (allowedRoles) => {
        return userRoles.some(role => allowedRoles.includes(role));
    };

    const toggleSidebar = () => {
        setIsCollapsed(!isCollapsed);
        if (!isCollapsed) setOpenMenu('');
    };

    const handleToggleMenu = (menuName) => {
        if (isCollapsed) setIsCollapsed(false);
        setOpenMenu(openMenu === menuName ? '' : menuName);
    };

    const navLinkClass = ({ isActive }) =>
        `d-flex align-items-center gap-3 px-3 py-2 rounded text-decoration-none transition-all ${isActive ? 'custom-btn-general text-white fw-medium' : 'text-light opacity-75 hover-opacity-100'
        }`;

    return (
        <aside
            style={{
                width: isCollapsed ? '75px' : '260px',
                background: '#1a2e26',
                minHeight: '100vh',
                transition: 'width 0.3s ease',
                overflow: 'hidden', // Quan trọng: Chặn mọi thứ trồi ra ngoài
                whiteSpace: 'nowrap' // Quan trọng: Cấm chữ rớt dòng gây vỡ layout
            }}
            className="d-flex flex-column border-end border-secondary border-opacity-25"
        >
            <div
                className={`d-flex align-items-center p-3 border-bottom border-secondary border-opacity-25 mb-3 ${isCollapsed ? 'justify-content-center' : 'justify-content-between'}`}
                style={{ height: '70px', transition: 'all 0.3s' }}
            >
                {!isCollapsed && (
                    <h5 className="m-0 text-white fw-bold">Internal Portal</h5>
                )}
                <button
                    onClick={toggleSidebar}
                    className="btn btn-link text-white p-1"
                    style={{ outline: 'none', boxShadow: 'none' }}
                >
                    <BsList size={26} />
                </button>
            </div>

            <div style={{
                opacity: isCollapsed ? 0 : 1,
                transition: 'opacity 0.2s ease',
                pointerEvents: isCollapsed ? 'none' : 'auto'
            }}>
                <nav className="d-flex flex-column gap-2 px-2">

                    {hasAccess(['ADMIN', 'MANAGER']) && (
                        <NavLink to="/management/dashboard" className={navLinkClass} end>
                            <BsGrid1X2 size={20} className="flex-shrink-0" />
                            <span>Dashboard</span>
                        </NavLink>
                    )}

                    {hasAccess(['ADMIN', 'MANAGER']) && (
                        <div>
                            <div
                                className="d-flex align-items-center justify-content-between px-3 py-2 rounded text-light opacity-75 hover-opacity-100"
                                style={{ cursor: 'pointer' }}
                                onClick={() => handleToggleMenu('coaches')}
                            >
                                <div className="d-flex align-items-center gap-3">
                                    <BsBusFront size={20} className="flex-shrink-0" />
                                    <span>Quản lý xe</span>
                                </div>
                                {openMenu === 'coaches' ? <BsChevronDown size={14} /> : <BsChevronRight size={14} />}
                            </div>

                            <Collapse in={openMenu === 'coaches'}>
                                <div className="ps-4 mt-1 d-flex flex-column gap-1">
                                    <NavLink to="/management/coach-types" className={navLinkClass}>
                                        <BsTags size={16} />
                                        <span style={{ fontSize: '0.9rem' }}>Loại xe</span>
                                    </NavLink>
                                    <NavLink to="/management/coaches" className={navLinkClass}>
                                        <BsBusFront size={16} />
                                        <span style={{ fontSize: '0.9rem' }}>Xe</span>
                                    </NavLink>
                                </div>
                            </Collapse>
                        </div>
                    )}

                    {hasAccess(['ADMIN', 'MANAGER']) && (
                        <div>
                            <div
                                className="d-flex align-items-center justify-content-between px-3 py-2 rounded text-light opacity-75 hover-opacity-100"
                                style={{ cursor: 'pointer' }}
                                onClick={() => handleToggleMenu('routes')}
                            >
                                <div className="d-flex align-items-center gap-3">
                                    <BsSignpostSplit size={20} className="flex-shrink-0" />
                                    <span>Quản lý tuyến</span>
                                </div>
                                {openMenu === 'routes' ? <BsChevronDown size={14} /> : <BsChevronRight size={14} />}
                            </div>

                            <Collapse in={openMenu === 'routes'}>
                                <div className="ps-4 mt-1 d-flex flex-column gap-1">
                                    <NavLink to="/management/routes" className={navLinkClass}>
                                        <BsSignpostSplit size={16} />
                                        <span style={{ fontSize: '0.9rem' }}>Tuyến đường</span>
                                    </NavLink>
                                    <NavLink to="/management/coach-stops" className={navLinkClass}>
                                        <BsGeoAlt size={16} />
                                        <span style={{ fontSize: '0.9rem' }}>Điểm dừng</span>
                                    </NavLink>
                                </div>
                            </Collapse>
                        </div>
                    )}

                    {hasAccess(['ADMIN', 'MANAGER']) && (
                        <div>
                            <div
                                className="d-flex align-items-center justify-content-between px-3 py-2 rounded text-light opacity-75 hover-opacity-100"
                                style={{ cursor: 'pointer' }}
                                onClick={() => handleToggleMenu('cargo')}
                            >
                                <div className="d-flex align-items-center gap-3">
                                    <BsBoxSeam size={20} className="flex-shrink-0" />
                                    <span>Quản lý loại hàng</span>
                                </div>
                                {openMenu === 'cargo' ? <BsChevronDown size={14} /> : <BsChevronRight size={14} />}
                            </div>

                            <Collapse in={openMenu === 'cargo'}>
                                <div className="ps-4 mt-1 d-flex flex-column gap-1">
                                    <NavLink to="/management/cargo-types" className={navLinkClass}>
                                        <BsBoxSeam size={16} />
                                        <span style={{ fontSize: '0.9rem' }}>Loại hàng</span>
                                    </NavLink>
                                    <NavLink to="/management/freight-rates" className={navLinkClass}>
                                        <BsCashCoin size={16} />
                                        <span style={{ fontSize: '0.9rem' }}>Giá cước</span>
                                    </NavLink>
                                </div>
                            </Collapse>
                        </div>
                    )}

                    {hasAccess(['ADMIN', 'MANAGER']) && (
                        <NavLink to="/management/vouchers" className={navLinkClass} end>
                            <BsGift size={20} className="flex-shrink-0" />
                            <span>Quản lý voucher</span>
                        </NavLink>
                    )}

                    {hasAccess(['ADMIN', 'MANAGER']) && (
                        <>
                            <NavLink to="/management/trips" className={navLinkClass} end>
                                <BsBusFront size={20} className="flex-shrink-0" />
                                <span>Quản lý chuyến xe</span>
                            </NavLink>
                        </>
                    )}

                    {hasAccess(['ADMIN']) && (
                        <>
                            <NavLink to="/management/manage-accounts" className={navLinkClass} end>
                                <BsPeopleFill size={20} className="flex-shrink-0" />
                                <span>Quản lý tài khoản</span>
                            </NavLink>
                            <NavLink to="/management/manage-roles" className={navLinkClass} end>
                                <BsShieldCheck size={20} className="flex-shrink-0" />
                                <span>Xem vai trò</span>
                            </NavLink>
                            <NavLink to="/management/manage-customers" className={navLinkClass} end>
                                <BsPersonBadge size={20} className="flex-shrink-0" />
                                <span>Quản lý khách hàng</span>
                            </NavLink>
                            <NavLink to="/management/manage-ticket-agencies" className={navLinkClass} end>
                                <BsBuilding size={20} className="flex-shrink-0" />
                                <span>Đại lý bán vé</span>
                            </NavLink>
                            <NavLink to="/management/manage-staff" className={navLinkClass} end>
                                <BsPersonBadge size={20} className="flex-shrink-0" />
                                <span>Quản lý nhân viên</span>
                            </NavLink>
                        </>
                    )}

                    {hasAccess(['TICKET_STAFF']) && (
                        <>
                            <NavLink to="/staff/passenger-tickets/search" className={navLinkClass} end>
                                <BsTicketPerforated size={20} className="flex-shrink-0" />
                                <span>Vé hành khách</span>
                            </NavLink>
                            <NavLink to="/staff/trips/info" className={navLinkClass} end>
                                <BsInfoCircle size={20} className="flex-shrink-0" />
                                <span>Thông tin chuyến xe</span>
                            </NavLink>
                        </>
                    )}

                </nav>
            </div>
        </aside>
    );
}
