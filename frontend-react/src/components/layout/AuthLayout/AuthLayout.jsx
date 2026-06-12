import { Container, Row, Col } from 'react-bootstrap';
import { Outlet, Link } from 'react-router-dom';

export default function AuthLayout() {
  return (
    <Container fluid className="vh-100 p-0" style={{ backgroundColor: '#f8fafc' }}>
      <style>{`
        :root {
          --ralsei-green: #9cf0d3;
          --ralsei-dark: #1a1a1a;
          --ralsei-white: #ffffff;
          --ralsei-black: #0a4030; 
          --ralsei-footer: #51c9a7; 
        }

        .custom-card {
          background: var(--ralsei-white);
          border-top: 5px solid var(--ralsei-black) !important; 
          border-radius: 16px !important;
          box-shadow: 0 20px 40px rgba(10, 64, 48, 0.06) !important;
        }

        .custom-btn-primary {
          background-color: var(--ralsei-black) !important;
          border-color: var(--ralsei-black) !important;
          color: var(--ralsei-white) !important; 
          font-weight: 600;
          border-radius: 8px;
          padding: 12px 20px;
          transition: all 0.2s ease;
        }
        .custom-btn-primary:hover:not(:disabled) {
          background-color: #062c21 !important; 
          transform: translateY(-1px);
        }

        .custom-btn-secondary {
          background-color: var(--ralsei-green) !important;
          border-color: var(--ralsei-green) !important;
          color: var(--ralsei-black) !important;
          font-weight: 600;
          border-radius: 8px;
          padding: 12px 20px;
        }
        .custom-btn-secondary:hover:not(:disabled) {
          background-color: var(--ralsei-footer) !important;
          color: var(--ralsei-white) !important;
        }

        .form-control {
          padding: 11px 15px;
          border-radius: 8px;
          border: 1px solid #e2e8f0;
        }
        .form-control:focus {
          border-color: var(--ralsei-footer) !important;
          box-shadow: 0 0 0 0.25rem rgba(81, 201, 167, 0.2) !important;
        }

        .custom-link {
          color: var(--ralsei-black);
          font-weight: 600;
          text-decoration: none;
        }
        .custom-link:hover {
          color: var(--ralsei-footer);
        }

        .back-pill-btn {
          top: 24px; 
          right: 24px; 
          padding: 8px 18px;
          font-size: 13px;
          font-weight: 600;
          background-color: var(--ralsei-white);
          color: var(--ralsei-black);
          border: 1px solid #e2e8f0;
          transition: all 0.2s ease;
          z-index: 10;
        }
        .back-pill-btn:hover {
          background-color: var(--ralsei-black);
          color: var(--ralsei-white);
        }

        .glass-feature-card {
          background: rgba(255, 255, 255, 0.07);
          backdrop-filter: blur(10px);
          border: 1px solid rgba(255, 255, 255, 0.1);
          border-radius: 12px;
          padding: 16px;
          margin-bottom: 16px;
          display: flex;
          align-items: center;
          gap: 15px;
          color: #ffffff;
        }
      `}</style>

      <Row className="h-100 g-0">
        <Col md={6} className="d-none d-md-flex flex-column justify-content-center p-5 text-white position-relative"
             style={{ background: 'linear-gradient(135deg, #0a4030 0%, #111827 100%)', overflow: 'hidden' }}>
          
          <div className="position-absolute rounded-circle" style={{ width: '400px', height: '400px', background: 'var(--ralsei-footer)', opacity: '0.1', top: '-10%', left: '-10%', filter: 'blur(80px)' }}></div>
          <div className="position-absolute rounded-circle" style={{ width: '300px', height: '300px', background: 'var(--ralsei-green)', opacity: '0.08', bottom: '-5%', right: '-5%', filter: 'blur(60px)' }}></div>

          <div className="position-relative" style={{ zIndex: 2, maxWidth: '480px', margin: '0 auto' }}>
            <span className="badge px-3 py-2 mb-3" style={{ backgroundColor: 'rgba(156, 240, 211, 0.15)', color: 'var(--ralsei-green)', fontWeight: '600', letterSpacing: '1px' }}>
              NỀN TẢNG ĐẶT VÉ THẾ HỆ MỚI
            </span>
            <h1 className="fw-bold mb-4" style={{ fontSize: '2.5rem', lineHeight: '1.3' }}>
              Trải nghiệm hành trình <span style={{ color: 'var(--ralsei-green)' }}>An toàn & Nhanh chóng</span> cùng Nhà Xe Ralsei
            </h1>
            <p className="text-white-50 mb-5">Chỉ với vài thao tác xác thực, hệ thống sẽ tự động tối ưu hóa lộ trình và giữ chỗ tốt nhất dành cho bạn.</p>
          </div>
        </Col>

        <Col md={6} xs={12} className="d-flex justify-content-center align-items-center position-relative px-3">
          <Link to="/" className="btn rounded-pill position-absolute d-flex align-items-center gap-2 shadow-sm back-pill-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="currentColor" viewBox="0 0 16 16">
              <path fillRule="evenodd" d="M15 8a.5.5 0 0 0-.5-.5H2.707l3.147-3.146a.5.5 0 1 0-.708-.708l-4 4a.5.5 0 0 0 0 .708l4 4a.5.5 0 0 0 .708-.708L2.707 8.5H14.5A.5.5 0 0 0 15 8z"/>
            </svg>
            <span>Quay lại trang chủ</span>
          </Link>

          <div className="w-100" style={{ maxWidth: '420px', padding: '40px 0' }}>
            <Outlet />
          </div>
        </Col>
      </Row>
    </Container>
  );
}