import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Form, Button, Alert, Card } from 'react-bootstrap';
import { RecaptchaVerifier, signInWithPhoneNumber } from 'firebase/auth';
import { auth, useAuth, authApi } from '../../features/auth';
import {
  PHONE_MAX_LENGTH,
  PHONE_REGEX,
  trimInput,
} from '../../utils/identityPatterns';

const formatPhoneForFirebase = (phone) => {
  if (phone.startsWith('0')) return '+84' + phone.slice(1);
  return phone;
};

export default function Login() {
  const navigate = useNavigate();
  const { processAuthSuccess } = useAuth();
  
  const [step, setStep] = useState(1);
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [confirmationResult, setConfirmationResult] = useState(null);
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSendOtp = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const normalizedPhone = trimInput(phone);
    setPhone(normalizedPhone);

    if (!PHONE_REGEX.test(normalizedPhone)) {
      setError('SĐT không hợp lệ. Vui lòng nhập 10 chữ số, bắt đầu bằng 03 hoặc 05 hoặc 07 hoặc 08 hoặc 09!');
      setLoading(false);
      return;
    }

    try {
      if (!window.recaptchaVerifier) {
        window.recaptchaVerifier = new RecaptchaVerifier(auth, 'recaptcha-container', {
          size: 'invisible'
        });
      }
      const formattedPhone = formatPhoneForFirebase(normalizedPhone);
      const confirmResult = await signInWithPhoneNumber(auth, formattedPhone, window.recaptchaVerifier);
      
      setConfirmationResult(confirmResult);
      setStep(2);
    } catch (err) {
      console.error(err);
      setError('Không thể gửi mã OTP. Vui lòng thử lại!');
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
      const response = await authApi.customerPhoneLogin(userCredential, trimInput(phone));
      processAuthSuccess(response); 
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Mã OTP không chính xác!');
    } finally {
      setLoading(false);
    }
  };

  const handleSocialLogin = async (providerFunc) => {
    setError('');
    try {
      const response = await providerFunc();
      if (response) {
        processAuthSuccess(response);
        navigate('/');
      }
    } catch (err) {
      console.log(err);
      // setError(err.message || "Đăng nhập thất bại, vui lòng thử lại!");
    }
  };

  return (
    <Card className="custom-card border-0">
      <Card.Body className="p-4">
        <h3 className="mb-1 fw-bold" style={{ color: 'var(--ralsei-black)' }}>Đăng nhập</h3>
        <p className="text-muted mb-4" style={{ fontSize: '14px' }}>Chào mừng bạn quay trở lại với Nhà Xe Ralsei.</p>
        
        {error && <Alert variant="danger" style={{ borderRadius: '8px', fontSize: '14px' }}>{error}</Alert>}

        <div id="recaptcha-container"></div>

        {step === 1 ? (
          <Form onSubmit={handleSendOtp}>
            <Form.Group className="mb-4">
              <Form.Label style={{ fontWeight: '600', color: '#334155', fontSize: '14px' }}>Số điện thoại</Form.Label>
              <Form.Control
                type="tel"
                placeholder="Ví dụ: 0912345678"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                maxLength={PHONE_MAX_LENGTH}
                required
              />
            </Form.Group>

            <Button type="submit" className="w-100 mb-3 custom-btn-primary" disabled={loading}>
              {loading ? 'Đang gửi mã...' : 'Nhận mã OTP'}
            </Button>

            {/* <div className="d-flex align-items-center mb-3">
              <hr className="flex-grow-1" style={{ opacity: 0.1 }} />
              <span className="mx-3 text-muted" style={{ fontSize: '12px', fontWeight: '500', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Hoặc đăng nhập với</span>
              <hr className="flex-grow-1" style={{ opacity: 0.1 }} />
            </div>

            <div className="d-flex gap-2 justify-content-center">
              <Button 
                variant="outline-light" 
                className="flex-grow-1 d-flex justify-content-center align-items-center py-2 shadow-sm" 
                style={{ border: '1px solid #e2e8f0', borderRadius: '8px', backgroundColor: '#fff' }}
                onClick={() => handleSocialLogin(authApi.signInWithGoogle)}
              >
                <svg viewBox="0 0 48 48" width="20" height="20" className="me-2">
                  <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                  <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                  <path fill="#FBBC05" d="M10.53 28.59A14.5 14.5 0 0 1 9.5 24c0-1.59.28-3.14.76-4.59l-7.98-6.19A23.99 23.99 0 0 0 0 24c0 3.77.87 7.35 2.56 10.56l7.97-5.97z"/>
                  <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 5.97C6.51 42.62 14.62 48 24 48z"/>
                </svg>
                <span style={{ color: '#334155', fontWeight: '500', fontSize: '14px' }}>Google</span>
              </Button>
              <Button 
                variant="outline-light" 
                className="flex-grow-1 d-flex justify-content-center align-items-center py-2 shadow-sm" 
                style={{ border: '1px solid #e2e8f0', borderRadius: '8px', backgroundColor: '#fff' }}
                onClick={() => handleSocialLogin(authApi.signInWithFacebook)}
              >
                <svg viewBox="0 0 48 48" width="20" height="20" className="me-2">
                  <path fill="#1877F2" d="M24 0C10.74 0 0 10.74 0 24c0 13.26 10.74 24 24 24s24-10.74 24-24C48 10.74 37.26 0 24 0z"/>
                  <path fill="#fff" d="M30.24 24.94h-4.08v15.56h-5.82V24.94h-2.77v-4.94h2.77v-3.2c0-3.78 1.62-6.02 6.06-6.02h3.73v4.94h-2.33c-1.74 0-1.85.65-1.85 1.86l-.01 2.42h4.2l-.9 4.94z"/>
                </svg>
                <span style={{ color: '#334155', fontWeight: '500', fontSize: '14px' }}>Facebook</span>
              </Button>
            </div> */}
          </Form>
        ) : (
          <Form onSubmit={handleVerifyOtp}>
            <Form.Group className="mb-4">
              <Form.Label style={{ color: '#334155', fontSize: '14px' }}>Mã OTP đã được gửi đến số <strong>{phone}</strong></Form.Label>
              <Form.Control
                type="text"
                maxLength="6"
                placeholder="••••••"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                required
                autoFocus
                style={{ letterSpacing: '8px', textAlign: 'center', fontSize: '22px', fontWeight: 'bold', color: 'var(--ralsei-black)' }}
              />
            </Form.Group>

            <Button type="submit" className="w-100 mb-3 custom-btn-secondary" disabled={loading}>
              {loading ? 'Đang xác thực...' : 'Xác nhận & Đăng nhập'}
            </Button>
            <Button variant="link" className="w-100 text-decoration-none custom-link text-center" style={{ fontSize: '14px' }} onClick={() => setStep(1)}>
              ← Thay đổi số điện thoại
            </Button>
          </Form>
        )}

        <div className="text-center mt-4 pt-2 style={{ fontSize: '14px', color: '#64748b' }}">
          Chưa có tài khoản? <Link to="/register" className="custom-link ms-1" style={{ color: 'var(--ralsei-footer)' }}>Đăng ký ngay</Link>
        </div>
      </Card.Body>
    </Card>
  );
}