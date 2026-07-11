import { useEffect, useState } from 'react';
import { Alert, Button, Form, Modal, Spinner } from 'react-bootstrap';
import {
  FiClock,
  FiEdit3,
  FiFileText,
  FiLogOut,
  FiMail,
  FiPhone,
  FiUser,
} from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../features/auth';
import { customerAccountApi } from '../../features/customerAccount/api/customerAccountApi';
import {
  EMAIL_MAX_LENGTH,
  EMAIL_REGEX,
  FULL_NAME_MAX_LENGTH,
  FULL_NAME_REGEX,
  trimInput,
} from '../../utils/identityPatterns';
import './CustomerProfilePage.css';

const EMPTY_EDIT_FORM = {
  customerName: '',
  email: '',
  dob: '',
};

/**
 * Customer profile page used by `/profile`.
 * It follows the sample customer-account layout while keeping phone and
 * credential changes controlled by the authentication provider.
 */
export default function CustomerProfilePage() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [profile, setProfile] = useState(null);
  const [editForm, setEditForm] = useState(EMPTY_EDIT_FORM);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [modalError, setModalError] = useState('');

  useEffect(() => {
    let isMounted = true;
    customerAccountApi.getProfile()
      .then((data) => {
        if (isMounted) {
          setProfile(data);
          setLoading(false);
        }
      })
      .catch((requestError) => {
        if (isMounted) {
          setError(requestError.response?.data?.message || 'Không thể tải thông tin cá nhân.');
          setLoading(false);
        }
      });

    return () => { isMounted = false; };
  }, []);

  /** Opens the edit dialog with the latest profile values. */
  const openEditModal = () => {
    setModalError('');
    setEditForm({
      customerName: profile?.customerName || '',
      email: profile?.email || '',
      dob: profile?.dob || '',
    });
    setEditOpen(true);
  };

  /** Updates a field in the edit form draft. */
  const handleEditChange = (event) => {
    const { name, value } = event.target;
    setEditForm((current) => ({ ...current, [name]: value }));
  };

  /** Persists editable profile fields to the backend. */
  const handleUpdateProfile = async (event) => {
    event.preventDefault();
    setModalError('');

    const customerName = trimInput(editForm.customerName);
    const email = trimInput(editForm.email) || null;

    if (!FULL_NAME_REGEX.test(customerName)) {
      setModalError('Họ tên không hợp lệ. Vui lòng nhập ít nhất 2 ký tự, chỉ gồm chữ cái và khoảng trắng!');
      return;
    }
    if (email && (!EMAIL_REGEX.test(email) || email.length > EMAIL_MAX_LENGTH)) {
      setModalError('Email không hợp lệ! Ví dụ hợp lệ: name.hehe@example.com');
      return;
    }

    setEditForm((current) => ({ ...current, customerName, email: email || '' }));
    setSaving(true);
    try {
      const updatedProfile = await customerAccountApi.updateProfile({
        customerName,
        email,
        dob: editForm.dob || null,
      });
      setProfile(updatedProfile);
      setEditOpen(false);
      setMessage('Cập nhật thông tin cá nhân thành công.');
    } catch (requestError) {
      setModalError(requestError.response?.data?.message || 'Không thể cập nhật thông tin.');
    } finally {
      setSaving(false);
    }
  };

  /** Returns a display-safe fallback for optional profile values. */
  const displayValue = (value) => value || 'Chưa thiết lập';

  if (loading) {
    return (
      <div className="customer-profile-loading">
        <Spinner animation="border" />
      </div>
    );
  }

  return (
    <div className="customer-profile-page">
      <section className="customer-profile-shell">
        <aside className="customer-profile-menu" aria-label="Tài khoản khách hàng">
          <button className="customer-profile-menu__item is-active" type="button">
            <FiUser />
            <span>Thông tin cá nhân</span>
          </button>
          <button className="customer-profile-menu__item" type="button" onClick={() => navigate('/history')}>
            <FiClock />
            <span>Lịch sử dịch vụ</span>
          </button>
          <button className="customer-profile-menu__item" type="button" onClick={() => navigate('/cargo-history')}>
            <FiFileText />
            <span>Đơn hàng</span>
          </button>
          <button className="customer-profile-menu__item" type="button" onClick={() => { logout(); navigate('/'); }}>
            <FiLogOut />
            <span>Đăng xuất</span>
          </button>
        </aside>

        <div className="customer-profile-main">
          {error && <Alert variant="danger">{error}</Alert>}
          {message && <Alert variant="success" onClose={() => setMessage('')} dismissible>{message}</Alert>}

          <article className="customer-profile-card">
            <div className="customer-profile-card__header">
              <span>Thông tin cá nhân</span>
            </div>

            <div className="customer-profile-fields">
              <ProfileField label="Họ và tên" value={displayValue(profile?.customerName)} strong />
              <ProfileField label="Ngày sinh" value={displayValue(profile?.dob)} />
              <ProfileField label="Số điện thoại" value={displayValue(profile?.phone || profile?.username)} icon={<FiPhone />} />
              <ProfileField label="Email" value={displayValue(profile?.email)} icon={<FiMail />} />
            </div>

            <Button className="customer-profile-edit" type="button" onClick={openEditModal}>
              <FiEdit3 />
              <span>Chỉnh sửa</span>
            </Button>
          </article>
        </div>
      </section>

      <Modal
        show={editOpen}
        onHide={() => setEditOpen(false)}
        centered
        contentClassName="customer-profile-update-modal"
      >
        <Form onSubmit={handleUpdateProfile}>
          <Modal.Header closeButton>
            <Modal.Title>Cập nhật thông tin cá nhân</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {modalError && <Alert variant="danger">{modalError}</Alert>}
            <Form.Group className="mb-3">
              <Form.Label>Họ và tên</Form.Label>
              <Form.Control
                name="customerName"
                value={editForm.customerName}
                onChange={handleEditChange}
                maxLength={FULL_NAME_MAX_LENGTH}
                disabled={saving}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Email</Form.Label>
              <Form.Control
                type="email"
                name="email"
                value={editForm.email}
                onChange={handleEditChange}
                maxLength={EMAIL_MAX_LENGTH}
                disabled={saving}
              />
            </Form.Group>
            <Form.Group>
              <Form.Label>Ngày sinh</Form.Label>
              <Form.Control
                type="date"
                name="dob"
                value={editForm.dob}
                onChange={handleEditChange}
                disabled={saving}
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="light" onClick={() => setEditOpen(false)} disabled={saving}>Hủy</Button>
            <Button type="submit" className="booking-btn-general2" disabled={saving}>
              {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
}

/**
 * Displays one customer profile field with consistent spacing.
 */
function ProfileField({ label, value, icon = null, strong = false }) {
  return (
    <div className="customer-profile-field">
      <span className="customer-profile-field__label">{label}</span>
      <span className={strong ? 'customer-profile-field__value is-strong' : 'customer-profile-field__value'}>
        {icon}
        {value}
      </span>
    </div>
  );
}
