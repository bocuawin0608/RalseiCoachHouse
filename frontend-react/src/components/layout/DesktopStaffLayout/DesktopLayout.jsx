import { Outlet } from 'react-router-dom';
import Sidebar from './SideBar';
import Header from './Header';
import './DesktopLayout.css'

export default function DesktopLayout() {
    return (
        <div style={{ display: 'flex', minHeight: '100vh' }}>
            <Sidebar />
            
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                <Header />
                
                <main style={{ flex: 1, padding: '24px', background: '#f8fafc', overflowY: 'auto' }}>
                    <Outlet />
                </main>
            </div>
        </div>
    );
}