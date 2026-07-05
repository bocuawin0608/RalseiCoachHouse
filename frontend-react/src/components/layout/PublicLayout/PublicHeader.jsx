import { useNavigate } from 'react-router-dom';
import { Navbar, Container, Nav, Button } from 'react-bootstrap';
import { useAuth } from '../../../features/auth';

export default function PublicHeader() {
    const navigate = useNavigate();
    const { user, logout } = useAuth();

    const isCustomer = user?.roles?.includes('CUSTOMER');

    return (
        <Navbar expand="lg" style={{ backgroundColor: 'var(--ralsei-green)' }} className="shadow-sm sticky-top py-2">
            <Container>
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

                    <Nav className="align-items-lg-center gap-2 mt-3 mt-lg-0">
                        {user ? (
                            <>
                                <span className="fw-bold me-lg-3 mb-2 mb-lg-0" style={{ color: 'var(--ralsei-black)' }}>
                                    Xin chào {user.username}!
                                </span>
                                
                                {isCustomer && (
                                    <>
                                        <Button variant="light" size="sm" className="fw-bold rounded-pill px-3" onClick={() => navigate('/profile')}>Tài khoản</Button>
                                        <Button variant="light" size="sm" className="fw-bold rounded-pill px-3" onClick={() => navigate('/booking-history')}>Lịch sử vé</Button>
                                        <Button variant="light" size="sm" className="fw-bold rounded-pill px-3" onClick={() => navigate('/cargo-history')}>Hàng hóa</Button>
                                    </>
                                )}
                                
                                <Button size="sm" className="fw-bold border-0 rounded-pill px-3" style={{ backgroundColor: 'var(--ralsei-black)', color: 'white' }} onClick={() => { logout(); navigate('/'); }}>Đăng xuất</Button>
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