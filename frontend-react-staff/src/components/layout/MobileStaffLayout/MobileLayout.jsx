import { Outlet } from 'react-router-dom';
import Header from './Header';
import './MobileLayout.css';

export default function MobileLayout() {
    return (
        <div className="mobile-staff-shell">
            <Header />
            <main className="mobile-staff-main">
                <Outlet />
            </main>
        </div>
    );
}
