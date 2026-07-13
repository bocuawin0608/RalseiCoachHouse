import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './SideBar';
import Header from './Header';
import { SidebarProvider } from './SidebarContext';
import './DesktopLayout.css';

const SIDEBAR_WIDTH = 260;

/**
 * manualMode:
 * - null     → auto (hide when content overflows; restore only if sidebar would still fit)
 * - 'open'   → user forced open; never auto-hide
 * - 'closed' → user forced closed; never auto-show
 */
export default function DesktopLayout() {
    const [isCollapsed, setIsCollapsed] = useState(false);
    const mainRef = useRef(null);
    const manualModeRef = useRef(null);
    const location = useLocation();

    const toggleSidebar = useCallback(() => {
        setIsCollapsed((prev) => {
            const next = !prev;
            manualModeRef.current = next ? 'closed' : 'open';
            return next;
        });
    }, []);

    const setCollapsed = useCallback((collapsed) => {
        manualModeRef.current = collapsed ? 'closed' : 'open';
        setIsCollapsed(collapsed);
    }, []);

    useEffect(() => {
        manualModeRef.current = null;
        setIsCollapsed(false);
    }, [location.pathname]);

    useEffect(() => {
        const el = mainRef.current;
        if (!el) return;

        const isHorizOverflow = (node, slack = 0) =>
            node.scrollWidth > node.clientWidth - slack + 2;

        const hasOverflowNow = () => {
            if (isHorizOverflow(el)) return true;

            const scrollers = el.querySelectorAll(
                '.table-responsive, [class*="table-responsive"], .voucher-table-wrap'
            );
            for (const node of scrollers) {
                if (isHorizOverflow(node)) return true;
            }
            return false;
        };

        // Would content still fit after giving SIDEBAR_WIDTH back to the sidebar?
        const wouldFitWithSidebarOpen = () => {
            if (isHorizOverflow(el, SIDEBAR_WIDTH)) return false;

            const scrollers = el.querySelectorAll(
                '.table-responsive, [class*="table-responsive"], .voucher-table-wrap'
            );
            for (const node of scrollers) {
                if (isHorizOverflow(node, SIDEBAR_WIDTH)) return false;
            }
            return true;
        };

        const syncSidebarToContent = () => {
            if (manualModeRef.current === 'open') {
                setIsCollapsed(false);
                return;
            }
            if (manualModeRef.current === 'closed') {
                setIsCollapsed(true);
                return;
            }

            setIsCollapsed((prevCollapsed) => {
                if (hasOverflowNow()) return true;
                // No overflow: only auto-expand when sidebar can stay open without re-overflow
                if (prevCollapsed && wouldFitWithSidebarOpen()) return false;
                return prevCollapsed;
            });
        };

        const resizeObserver = new ResizeObserver(syncSidebarToContent);
        resizeObserver.observe(el);

        const mutationObserver = new MutationObserver(syncSidebarToContent);
        mutationObserver.observe(el, { childList: true, subtree: true });

        const raf = requestAnimationFrame(syncSidebarToContent);

        return () => {
            cancelAnimationFrame(raf);
            resizeObserver.disconnect();
            mutationObserver.disconnect();
        };
    }, [location.pathname]);

    const sidebarValue = useMemo(
        () => ({ isCollapsed, setCollapsed, toggleSidebar }),
        [isCollapsed, setCollapsed, toggleSidebar]
    );

    return (
        <SidebarProvider value={sidebarValue}>
            <div className="desktop-staff-shell">
                <Sidebar />

                <div className="desktop-staff-content">
                    <Header />

                    <main ref={mainRef} className="desktop-staff-main">
                        <Outlet />
                    </main>
                </div>
            </div>
        </SidebarProvider>
    );
}
