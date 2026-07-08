import { useEffect, useState } from 'react';
import { Alert, Button, Form, Modal, Spinner } from 'react-bootstrap';
import {
  BsCalendarCheck,
  BsEnvelope,
  BsKey,
  BsPencilSquare,
  BsPersonBadge,
  BsPersonVcard,
  BsTelephone,
} from 'react-icons/bs';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../features/auth';
import { staffAccountApi } from '../../features/staffAccount/api/staffAccountApi';
import './StaffProfilePage.css';

const EMPTY_EDIT_FORM = {
  staffName: '',
  email: '',
  dob: '',
};

const EMPTY_PASSWORD_FORM = {
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
};

const ROLE_LABELS = {
  ADMIN: 'Quản trị hệ thống',
  MANAGER: 'Quản lý',
  TICKET_STAFF: 'Nhân viên bán vé',
  TRIP_STAFF: 'Nhân viên chuyến xe',
};

const POSITION_LABELS = {
  DRIVER: 'Tài xế',
  ATTENDANT: 'Phụ xe',
  TICKET_SELLER: 'Nhân viên bán vé',
  MANAGER: 'Quản lý',
  ADMIN: 'Quản trị',
};

/**
 * Staff profile page used by internal account self-service routes.
 * It exposes only safe personal fields for editing while keeping operational
 * identity fields read-only.
 */
export default function StaffProfilePage() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [profile, setProfile] = useState(null);
  const [editForm, setEditForm] = useState(EMPTY_EDIT_FORM);
  const [passwordForm, setPasswordForm] = useState(EMPTY_PASSWORD_FORM);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [passwordOpen, setPasswordOpen] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [modalError, setModalError] = useState('');

  useEffect(() => {
    let isMounted = true;
    staffAccountApi.getProfile()
      .then((data) => {
        if (isMounted) {
          setProfile(data);
          setLoading(false);
        }
      })
      .catch((requestError) => {
        if (isMounted) {
          setError(requestError.response?.data?.message || 'Không thể tải thông tin nhân viên.');
          setLoading(false);
        }
      });

    return () => { isMounted = false; };
  }, []);

  /** Opens the edit dialog with the latest profile values. */
  const openEditModal = () => {
    setModalError('');
    setEditForm({
      staffName: profile?.staffName || '',
      email: profile?.email || '',
      dob: profile?.dob || '',
    });
    setEditOpen(true);
  };

  /** Opens the password dialog with an empty password draft. */
  const openPasswordModal = () => {
    setModalError('');
    setPasswordForm(EMPTY_PASSWORD_FORM);
    setPasswordOpen(true);
  };

  /** Updates one field in the profile edit draft. */
  const handleEditChange = (event) => {
    const { name, value } = event.target;
    setEditForm((current) => ({ ...current, [name]: value }));
  };

  /** Updates one field in the password change draft. */
  const handlePasswordChange = (event) => {
    const { name, value } = event.target;
    setPasswordForm((current) => ({ ...current, [name]: value }));
  };

  /** Persists editable staff profile fields to the backend. */
  const handleUpdateProfile = async (event) => {
    event.preventDefault();
    setSaving(true);
    setModalError('');
    try {
      const updatedProfile = await staffAccountApi.updateProfile({
        staffName: editForm.staffName.trim(),
        email: editForm.email.trim() || null,
        dob: editForm.dob || null,
      });
      setProfile(updatedProfile);
      setEditOpen(false);
      setMessage('Cập nhật thông tin nhân viên thành công.');
    } catch (requestError) {
      setModalError(requestError.response?.data?.message || 'Không thể cập nhật thông tin.');
    } finally {
      setSaving(false);
    }
  };

  /** Changes the staff password, then forces a fresh login. */
  const handleChangePassword = async (event) => {
    event.preventDefault();
    setSaving(true);
    setModalError('');
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setModalError('Mật khẩu xác nhận không khớp.');
      setSaving(false);
      return;
    }

    try {
      await staffAccountApi.changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordOpen(false);
      logout();
      navigate('/staff/login');
    } catch (requestError) {
      setModalError(requestError.response?.data?.message || 'Không thể đổi mật khẩu.');
    } finally {
      setSaving(false);
    }
  };

  /** Returns a display-safe fallback for optional profile values. */
  const displayValue = (value) => value || 'Chưa thiết lập';

  /** Converts raw role codes into compact staff-facing labels. */
  const displayRoles = (roles = []) => roles.map((role) => ROLE_LABELS[role] || role).join(', ');

  if (loading) {
    return (
      <div className="staff-profile-loading">
        <Spinner animation="border" />
      </div>
    );
  }

  return (
    <main className="staff-profile-page">
      <section className="staff-profile-shell">
        <aside className="staff-profile-menu" aria-label="Tài khoản nội bộ">
          <button className="staff-profile-menu__item is-active" type="button">
            <BsPersonVcard />
            <span>Thông tin cá nhân</span>
          </button>
          <button className="staff-profile-menu__item" type="button" onClick={openPasswordModal}>
            <BsKey />
            <span>Đổi mật khẩu</span>
          </button>
        </aside>

        <div className="staff-profile-main">
          {error && <Alert variant="danger">{error}</Alert>}
          {message && <Alert variant="success" onClose={() => setMessage('')} dismissible>{message}</Alert>}

          <article className="staff-profile-card">
            <div className="staff-profile-card__header">
              <span>Thông tin cá nhân</span>
              <strong>{displayRoles(profile?.roles)}</strong>
            </div>

            <div className="staff-profile-fields">
              <ProfileField label="Họ và tên" value={displayValue(profile?.staffName)} strong />
              <ProfileField label="Tên đăng nhập" value={displayValue(profile?.username)} icon={<BsPersonBadge />} />
              <ProfileField label="Ngày sinh" value={displayValue(profile?.dob)} />
              <ProfileField label="Số điện thoại" value={displayValue(profile?.phone)} icon={<BsTelephone />} />
              <ProfileField label="Email" value={displayValue(profile?.email)} icon={<BsEnvelope />} />
              <ProfileField
                label="Vị trí"
                value={displayValue(POSITION_LABELS[profile?.staffPosition] || profile?.staffPosition)}
              />
              <ProfileField label="Ngày vào làm" value={displayValue(profile?.hireDate)} icon={<BsCalendarCheck />} />
            </div>

            <Button className="staff-profile-edit" type="button" onClick={openEditModal}>
              <BsPencilSquare />
              <span>Chỉnh sửa</span>
            </Button>
          </article>
        </div>
      </section>

      <Modal
        show={editOpen}
        onHide={() => setEditOpen(false)}
        centered
        contentClassName="staff-profile-modal"
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
                name="staffName"
                value={editForm.staffName}
                onChange={handleEditChange}
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
            <Button type="submit" className="custom-btn-general" disabled={saving}>
              {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>

      <Modal
        show={passwordOpen}
        onHide={() => setPasswordOpen(false)}
        centered
        contentClassName="staff-profile-modal"
      >
        <Form onSubmit={handleChangePassword}>
          <Modal.Header closeButton>
            <Modal.Title>Đổi mật khẩu</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {modalError && <Alert variant="danger">{modalError}</Alert>}
            <Form.Group className="mb-3">
              <Form.Label>Mật khẩu hiện tại</Form.Label>
              <Form.Control
                type="password"
                name="currentPassword"
                value={passwordForm.currentPassword}
                onChange={handlePasswordChange}
                disabled={saving}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Mật khẩu mới</Form.Label>
              <Form.Control
                type="password"
                name="newPassword"
                value={passwordForm.newPassword}
                onChange={handlePasswordChange}
                disabled={saving}
                required
                minLength={8}
              />
            </Form.Group>
            <Form.Group>
              <Form.Label>Xác nhận mật khẩu mới</Form.Label>
              <Form.Control
                type="password"
                name="confirmPassword"
                value={passwordForm.confirmPassword}
                onChange={handlePasswordChange}
                disabled={saving}
                required
                minLength={8}
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="light" onClick={() => setPasswordOpen(false)} disabled={saving}>Hủy</Button>
            <Button type="submit" className="custom-btn-general" disabled={saving}>
              {saving ? 'Đang lưu...' : 'Đổi mật khẩu'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </main>
  );
}

/**
 * Displays one staff profile field with consistent spacing.
 */
function ProfileField({ label, value, icon = null, strong = false }) {
  return (
    <div className="staff-profile-field">
      <span className="staff-profile-field__label">{label}</span>
      <span className={strong ? 'staff-profile-field__value is-strong' : 'staff-profile-field__value'}>
        {icon}
        {value}
      </span>
    </div>
  );
}
