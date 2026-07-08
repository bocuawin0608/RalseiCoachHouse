import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../features/auth";
import {Button} from 'react-bootstrap'

export default function Header() {
    const navigate = useNavigate();
    const {logout, user} = useAuth();
    const profilePath = user?.roles?.some((role) => ['ADMIN', 'MANAGER'].includes(role))
        ? '/management/profile'
        : '/staff/profile';
    
    return (
        <header style={{ height: '60px', background: '#ffffff', borderBottom: '1px solid #e2e8f0', display: 'flex', alignItems: 'center', padding: '0 20px', justifyContent: 'flex-end', gap: '10px' }}>
            <div style={{ fontWeight: 'bold' }}>Xin chào, {user?.username || "User"}!</div>
            <Button variant="outline-secondary" onClick={() => navigate(profilePath)}>Hồ sơ</Button>
            <Button className="custom-btn-general" onClick={() => {logout(); navigate('/staff/login');}}>Đăng xuất</Button>
        </header>
    );
}
