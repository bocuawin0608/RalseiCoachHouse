import { useContext, useState } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function LoginPage() {
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();
    const [username, setUsername] = useState('');

    const handleLogin = (e) => {
        e.preventDefault();
        login(username);
        navigate('/seat-layouts');
    };

    return (
        <div style={{ padding: '50px', textAlign: 'center' }}>
            <h2>Đăng nhập vào Nhà xe TuanMV</h2>
            <form onSubmit={handleLogin}>
                <input 
                    placeholder="Số điện thoại đăng nhập..." 
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
                <button type="submit">Đăng nhập</button>
            </form>
        </div>
    );
}