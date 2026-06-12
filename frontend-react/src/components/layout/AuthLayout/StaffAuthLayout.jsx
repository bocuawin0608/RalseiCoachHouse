import { Container } from 'react-bootstrap';
import { Outlet } from 'react-router-dom';
import './StaffAuthLayout.css';

export default function StaffAuthLayout() {
  return (
    <Container fluid className="staff-auth-theme p-3">
      <div className="w-100 d-flex flex-column align-items-center">
        <div className="text-center mb-4">
          <h2 className="fw-bold text-white mb-0" style={{ letterSpacing: '2px' }}>
            RALSEI <span style={{ color: 'var(--ralsei-green)' }}>STAFF</span>
          </h2>
          <p className="text-white-50 mt-1 mb-0" style={{ fontSize: '14px' }}>
            Hệ thống quản trị vận tải nội bộ
          </p>
        </div>

        <Outlet />

        <div className="text-center mt-4 text-white-50" style={{ fontSize: '12px' }}>
          &copy; {new Date().getFullYear()} Nhà Xe Ralsei.
        </div>
      </div>
    </Container>
  );
}