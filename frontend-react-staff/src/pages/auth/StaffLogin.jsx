import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Alert, Card, Modal } from 'react-bootstrap';
import { useAuth } from '../../features/auth';
import { authApi } from '../../features/auth/api/authApi';
import './StaffLogin.css';

const EMPTY_FORGOT_FORM = {
  username: '',
  email: '',
};

export default function StaffLogin() {
  const navigate = useNavigate();
  const { loginStaff } = useAuth();
  
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [forgotForm, setForgotForm] = useState(EMPTY_FORGOT_FORM);
  const [loading, setLoading] = useState(false);
  const [forgotLoading, setForgotLoading] = useState(false);
  const [error, setError] = useState('');
  const [forgotMessage, setForgotMessage] = useState('');
  const [forgotError, setForgotError] = useState('');
  const [forgotOpen, setForgotOpen] = useState(false);

  /** Keeps the staff login draft in sync with the form controls. */
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  /** Keeps the forgot-password draft separate from the active login form. */
  const handleForgotChange = (e) => {
    setForgotForm({
      ...forgotForm,
      [e.target.name]: e.target.value
    });
  };

  /** Opens the forgot-password modal using the current username if available. */
  const openForgotModal = () => {
    setForgotError('');
    setForgotMessage('');
    setForgotForm({
      username: formData.username,
      email: '',
    });
    setForgotOpen(true);
  };

  /** Sends the staff forgot-password command to the public auth endpoint. */
  const handleForgotPassword = async (e) => {
    e.preventDefault();
    setForgotError('');
    setForgotMessage('');

    if (!forgotForm.username.trim() || !forgotForm.email.trim()) {
      setForgotError('Vui lòng nhập tên đăng nhập và email nhân viên.');
      return;
    }

    setForgotLoading(true);
    try {
      const response = await authApi.staffForgotPassword({
        username: forgotForm.username.trim(),
        email: forgotForm.email.trim(),
      });
      setForgotMessage(response.message || 'Nếu thông tin khớp, mật khẩu tạm thời sẽ được gửi qua email.');
      setForgotForm(EMPTY_FORGOT_FORM);
    } catch (err) {
      setForgotError(err.response?.data?.message || 'Không thể gửi yêu cầu đặt lại mật khẩu.');
    } finally {
      setForgotLoading(false);
    }
  };

  /** Authenticates a local staff account and redirects by assigned role. */
  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!formData.username.trim() || !formData.password.trim()) {
      setError('Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!');
      return;
    }

    setLoading(true);
    try {
      const response = await loginStaff(formData);
      const roles = response.roles || [];

      if (roles.includes('MANAGER') || roles.includes('ADMIN')) {
        navigate('/management/dashboard');
      } else if (roles.includes('TICKET_STAFF')) {
        navigate('/staff/passenger-tickets/search');
      } else if (roles.includes('TRIP_STAFF')) {
        navigate('/staff');
      }

    } catch (err) {
      setError(err.response?.data?.message || 'Không thể kết nối đến máy chủ!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card className="staff-login-card border-0">
      <Card.Body className="p-4">
        <h4 className="mb-4 fw-bold text-center" style={{ color: 'var(--ralsei-black)' }}>
          Đăng Nhập
        </h4>
        
        {error && (
          <Alert variant="danger" className="py-2" style={{ borderRadius: '6px', fontSize: '14px' }}>
            {error}
          </Alert>
        )}

        <Form onSubmit={handleLogin}>
          <Form.Group className="mb-3">
            <Form.Label style={{ fontWeight: '600', color: '#334155', fontSize: '13px' }}>
              Tên đăng nhập
            </Form.Label>
            <Form.Control
              type="text"
              name="username"
              placeholder="Nhập tên đăng nhập"
              value={formData.username}
              onChange={handleChange}
              disabled={loading}
              autoFocus
            />
          </Form.Group>

          <Form.Group className="mb-4">
            <Form.Label style={{ fontWeight: '600', color: '#334155', fontSize: '13px' }}>
              Mật khẩu
            </Form.Label>
            <Form.Control
              type="password"
              name="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              disabled={loading}
            />
          </Form.Group>

          <Button type="submit" className="w-100 btn-staff-primary" disabled={loading}>
            {loading ? 'Đang xác thực...' : 'Đăng nhập vào hệ thống'}
          </Button>
          <button
            type="button"
            className="staff-forgot-password-link"
            onClick={openForgotModal}
            disabled={loading}
          >
            Quên mật khẩu?
          </button>
        </Form>
      </Card.Body>

      <Modal show={forgotOpen} onHide={() => setForgotOpen(false)} centered contentClassName="staff-forgot-modal">
        <Form onSubmit={handleForgotPassword}>
          <Modal.Header closeButton>
            <Modal.Title>Quên mật khẩu nhân viên</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {forgotError && <Alert variant="danger">{forgotError}</Alert>}
            {forgotMessage && <Alert variant="success">{forgotMessage}</Alert>}
            <Form.Group className="mb-3">
              <Form.Label>Tên đăng nhập</Form.Label>
              <Form.Control
                type="text"
                name="username"
                value={forgotForm.username}
                onChange={handleForgotChange}
                disabled={forgotLoading}
                required
              />
            </Form.Group>
            <Form.Group>
              <Form.Label>Email nhân viên</Form.Label>
              <Form.Control
                type="email"
                name="email"
                value={forgotForm.email}
                onChange={handleForgotChange}
                disabled={forgotLoading}
                required
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="light" onClick={() => setForgotOpen(false)} disabled={forgotLoading}>Hủy</Button>
            <Button type="submit" className="btn-staff-primary" disabled={forgotLoading}>
              {forgotLoading ? 'Đang gửi...' : 'Gửi mật khẩu tạm thời'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </Card>
  );
}
