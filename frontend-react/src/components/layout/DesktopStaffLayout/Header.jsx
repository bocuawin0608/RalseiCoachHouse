import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../features/auth";

export default function Header() {
    const navigate = useNavigate();
    const {logout, user} = useAuth();
    console.log("User la gi: " + user);
    return (
        <header style={{ height: '60px', background: '#ffffff', borderBottom: '1px solid #e2e8f0', display: 'flex', alignItems: 'center', padding: '0 20px', justifyContent: 'flex-end', gap: '10px' }}>
            <div style={{ fontWeight: 'bold' }}>Xin chào, {user?.username || "User"}!</div>
            <button className="btn-head" onClick={() => {logout(); navigate('/staff/login');}}>Đăng xuất</button>
        </header>
    );
}