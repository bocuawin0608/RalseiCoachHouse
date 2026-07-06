import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Alert, Card } from 'react-bootstrap';
import { useAuth } from '../../features/auth';

export default function StaffLogin() {
  const navigate = useNavigate();
  const { loginStaff } = useAuth();
  
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

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
        navigate('/staff/trip/scan');
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
        </Form>
      </Card.Body>
    </Card>
  );
}