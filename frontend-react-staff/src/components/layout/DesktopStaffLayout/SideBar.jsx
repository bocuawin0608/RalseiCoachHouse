import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { Collapse } from 'react-bootstrap';
import {
    BsGrid1X2, BsBusFront, BsSignpostSplit,
    BsChevronDown, BsChevronRight, BsTags, BsGeoAlt,
    BsBoxSeam, BsGift, BsTicketPerforated, BsInfoCircle, BsCashCoin, BsReceipt,
    BsPeopleFill, BsShieldCheck, BsPersonBadge, BsBuilding
} from 'react-icons/bs';
import { useAuth } from '../../../features/auth';
import { useSidebar } from './SidebarContext';

export default function Sidebar() {
    const { isCollapsed, setCollapsed } = useSidebar();
    const [openMenu, setOpenMenu] = useState('');

    const { user } = useAuth();
    const userRoles = user?.roles || [];
    const hasAccess = (allowedRoles) => {
        return userRoles.some(role => allowedRoles.includes(role));
    };

    const handleToggleMenu = (menuName) => {
        if (isCollapsed) setCollapsed(false);
        setOpenMenu(openMenu === menuName ? '' : menuName);
    };

    const navLinkClass = ({ isActive }) =>
        `d-flex align-items-center gap-2 px-2 py-2 rounded text-decoration-none transition-all ${isActive ? 'custom-btn-general text-white fw-medium' : 'text-light opacity-75 hover-opacity-100'
        }`;

    return (
        <aside
            className={`desktop-staff-sidebar d-flex flex-column border-end border-secondary border-opacity-25 ${isCollapsed ? 'is-collapsed' : ''}`}
            aria-hidden={isCollapsed}
        >
            <div
                className="d-flex align-items-center p-3 border-bottom border-secondary border-opacity-25 mb-3"
                style={{ height: '70px', flexShrink: 0 }}
            >
                <h5 className="m-0 text-white fw-bold">Internal Portal</h5>
            </div>

            <div
                className="desktop-staff-sidebar-nav"
                style={{
                    opacity: isCollapsed ? 0 : 1,
                    pointerEvents: isCollapsed ? 'none' : 'auto'
                }}
            >
                <nav className="d-flex flex-column gap-2 px-2 pb-3">

                    {hasAccess(['ADMIN', 'MANAGER']) && (
                        <NavLink to="/management/dashboard" className={navLinkClass} end>
                            <BsGrid1X2 size={20} className="flex-shrink-0" />
                            <span>Dashboard</span>
                        </NavLink>
                    )}

                    {hasAccess(['MANAGER']) && (
                        <div>
                            <div
                                className="d-flex align-items-center justify-content-between px-2 py-2 rounded text-light opacity-75 hover-opacity-100"
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

                    {hasAccess(['MANAGER']) && (
                        <div>
                            <div
                                className="d-flex align-items-center justify-content-between px-2 py-2 rounded text-light opacity-75 hover-opacity-100"
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
                                </div>
                            </Collapse>
                        </div>
                    )}

                    {hasAccess(['MANAGER']) && (
                        <NavLink to="/management/cargo-types" className={navLinkClass}>
                            <BsBoxSeam size={20} className="flex-shrink-0" />
                            <span>Quản lý loại hàng</span>
                        </NavLink>
                    )}

                    {hasAccess(['MANAGER']) && (
                        <NavLink to="/management/vouchers" className={navLinkClass} end>
                            <BsGift size={20} className="flex-shrink-0" />
                            <span>Quản lý voucher</span>
                        </NavLink>
                    )}

                    {hasAccess(['MANAGER']) && (
                        <>
                            <NavLink to="/management/trips" className={navLinkClass} end>
                                <BsBusFront size={20} className="flex-shrink-0" />
                                <span>Quản lý chuyến xe</span>
                            </NavLink>
                            <NavLink to="/management/refunds?status=PENDING&tab=passenger" className={navLinkClass} end>
                                <BsCashCoin size={20} className="flex-shrink-0" />
                                <span>Xử lý hoàn tiền</span>
                            </NavLink>
                        </>
                    )}

                    {hasAccess(['ADMIN']) && (
                        <>
                            <NavLink to="/management/manage-ticket-agencies" className={navLinkClass} end>
                                <BsBuilding size={20} className="flex-shrink-0" />
                                <span>Đại lý bán vé</span>
                            </NavLink>

                            <div>
                                <div
                                    className="d-flex align-items-center justify-content-between px-2 py-2 rounded text-light opacity-75 hover-opacity-100"
                                    style={{ cursor: 'pointer' }}
                                    onClick={() => handleToggleMenu('staffing')}
                                >
                                    <div className="d-flex align-items-center gap-2">
                                        <BsPeopleFill size={20} className="flex-shrink-0" />
                                        <span>Quản lý nhân sự</span>
                                    </div>
                                    {openMenu === 'staffing' ? <BsChevronDown size={14} /> : <BsChevronRight size={14} />}
                                </div>

                                <Collapse in={openMenu === 'staffing'}>
                                    <div className="ps-4 mt-1 d-flex flex-column gap-1">
                                        <NavLink to="/management/manage-staff" className={navLinkClass}>
                                            <BsPersonBadge size={16} />
                                            <span style={{ fontSize: '0.9rem' }}>Quản lý nhân viên</span>
                                        </NavLink>
                                        <NavLink to="/management/manage-accounts" className={navLinkClass}>
                                            <BsPeopleFill size={16} />
                                            <span style={{ fontSize: '0.9rem' }}>Quản lý tài khoản</span>
                                        </NavLink>
                                        <NavLink to="/management/manage-roles" className={navLinkClass} end>
                                            <BsShieldCheck size={16} className="flex-shrink-0" />
                                            <span style={{ fontSize: '0.9rem' }}>Xem vai trò</span>
                                        </NavLink>
                                    </div>
                                </Collapse>
                            </div>
                            <NavLink to="/management/manage-customers" className={navLinkClass} end>
                                <BsPersonBadge size={20} className="flex-shrink-0" />
                                <span>Xem khách hàng</span>
                            </NavLink>
                        </>
                    )}

                    {hasAccess(['TICKET_STAFF']) && (
                        <>
                            <NavLink to="/staff/trips/info" className={navLinkClass} end>
                                <BsInfoCircle size={20} className="flex-shrink-0" />
                                <span>Thông tin chuyến xe</span>
                            </NavLink>
                            <NavLink to="/staff/passenger-tickets/search" className={navLinkClass} end>
                                <BsTicketPerforated size={20} className="flex-shrink-0" />
                                <span>Vé hành khách</span>
                            </NavLink>
                            <NavLink to="/staff/cargo-tickets" className={navLinkClass} end>
                                <BsReceipt size={20} className="flex-shrink-0" />
                                <span>Đơn gửi hàng</span>
                            </NavLink>
                        </>
                    )}

                </nav>
            </div>
        </aside>
    );
}
