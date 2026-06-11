import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Form, Button, Alert, Card } from 'react-bootstrap';
import { RecaptchaVerifier, signInWithPhoneNumber } from 'firebase/auth';
import { auth, useAuth, authApi } from '../../features/auth';

const formatPhoneForFirebase = (phone) => phone.startsWith('0') ? '+84' + phone.slice(1) : phone;

export default function Register() {
  const navigate = useNavigate();
  const { processAuthSuccess } = useAuth();
  
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({ customerName: '', username: '', email: '' });
  const [otp, setOtp] = useState('');
  const [confirmationResult, setConfirmationResult] = useState(null);
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSendOtp = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!formData.username.match(/^0[0-9]{9}$/)) {
      setError('Số điện thoại không hợp lệ!');
      return;
    }
    
    setLoading(true);
    try {
      
      if (window.recaptchaVerifier) {
        try {
          window.recaptchaVerifier.clear(); 
        } catch (e) {
          console.error("Clear recaptcha lỗi:", e);
        }
        window.recaptchaVerifier = null; 
      }

        window.recaptchaVerifierRegister = new RecaptchaVerifier(auth, 'recaptcha-container-reg', {
          size: 'invisible'
        });
      
      const formattedPhone = formatPhoneForFirebase(formData.username);
      const confirmResult = await signInWithPhoneNumber(auth, formattedPhone, window.recaptchaVerifierRegister);
      
      setConfirmationResult(confirmResult);
      setStep(2);
    } catch (err) {
      console.error(err);
      setError('Lỗi kết nối Firebase hoặc số điện thoại đã bị chặn OTP do spam.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const userCredential = await confirmationResult.confirm(otp);
      const response = await authApi.customerPhoneRegister(userCredential, formData);
      processAuthSuccess(response); 
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Xác thực OTP thất bại!');
    } finally {
      setLoading(false);
      setOtp('');
    }
  };

  return (
    <Card className="custom-card border-0">
      <Card.Body className="p-4">
        <h3 className="mb-1 fw-bold" style={{ color: 'var(--ralsei-black)' }}>Đăng ký</h3>
        <p className="text-muted mb-4" style={{ fontSize: '14px' }}>Tạo tài khoản đặt xe chỉ trong vài bước.</p>
        
        {error && <Alert variant="danger" style={{ borderRadius: '8px', fontSize: '14px' }}>{error}</Alert>}

        <div id="recaptcha-container-reg"></div>

        {step === 1 ? (
          <Form onSubmit={handleSendOtp}>
            <Form.Group className="mb-3">
              <Form.Label style={{ fontWeight: '600', color: '#334155', fontSize: '14px' }}>Họ và tên <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                name="customerName"
                placeholder="Nhập họ tên của bạn"
                value={formData.customerName}
                onChange={handleChange}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label style={{ fontWeight: '600', color: '#334155', fontSize: '14px' }}>Số điện thoại <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="tel"
                name="username"
                placeholder="Ví dụ: 0912345678"
                value={formData.username}
                onChange={handleChange}
                required
              />
            </Form.Group>

            <Form.Group className="mb-4">
              <Form.Label style={{ fontWeight: '600', color: '#334155', fontSize: '14px' }}>Email <span className="text-muted" style={{ fontWeight: '400' }}>(tùy chọn)</span></Form.Label>
              <Form.Control
                type="email"
                name="email"
                placeholder="name@example.com"
                value={formData.email}
                onChange={handleChange}
              />
            </Form.Group>

            <Button type="submit" className="w-100 mb-3 custom-btn-primary" disabled={loading}>
              {loading ? 'Đang gửi mã...' : 'Tiếp tục nhận OTP'}
            </Button>
          </Form>
        ) : (
          <Form onSubmit={handleVerifyOtp}>
            <Form.Group className="mb-4">
              <Form.Label style={{ color: '#334155', fontSize: '14px' }}>Nhập mã OTP gửi đến số <strong>{formData.username}</strong></Form.Label>
              <Form.Control
                type="text"
                maxLength="6"
                placeholder="•••••"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                required
                autoFocus
                style={{ letterSpacing: '8px', textAlign: 'center', fontSize: '22px', fontWeight: 'bold', color: 'var(--ralsei-black)' }}
              />
            </Form.Group>

            <Button type="submit" className="w-100 mb-3 custom-btn-secondary" disabled={loading}>
              {loading ? 'Đang xác thực...' : 'Hoàn tất đăng ký'}
            </Button>
            <Button variant="link" className="w-100 text-decoration-none custom-link text-center" style={{ fontSize: '14px' }} onClick={() => setStep(1)}>
              ← Quay lại chỉnh sửa thông tin
            </Button>
          </Form>
        )}

        <div className="text-center mt-3" style={{ fontSize: '14px', color: '#64748b' }}>
          Đã có tài khoản? <Link to="/login" className="custom-link ms-1" style={{ color: 'var(--ralsei-footer)' }}>Đăng nhập</Link>
        </div>
      </Card.Body>
    </Card>
  );
}