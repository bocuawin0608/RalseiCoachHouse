import { useEffect, useState } from 'react';
import { Alert, Button, Form, Modal, Spinner } from 'react-bootstrap';
import {
  FiClock,
  FiEdit3,
  FiFileText,
  FiLogOut,
  FiMail,
  FiPhone,
  FiTrash2,
  FiUser,
} from 'react-icons/fi';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../features/auth';
import { customerAccountApi } from '../../features/customerAccount/api/customerAccountApi';
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
  const [deleteOpen, setDeleteOpen] = useState(false);
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
    setSaving(true);
    setModalError('');
    try {
      const updatedProfile = await customerAccountApi.updateProfile({
        customerName: editForm.customerName.trim(),
        email: editForm.email.trim() || null,
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

  /** Soft-deactivates the account, then clears local authentication state. */
  const handleDeactivateAccount = async () => {
    setSaving(true);
    setModalError('');
    try {
      await customerAccountApi.deactivateAccount();
      logout();
      navigate('/');
    } catch (requestError) {
      setModalError(requestError.response?.data?.message || 'Không thể xóa tài khoản.');
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

          <section className="customer-profile-actions">
            <button type="button" onClick={() => { setModalError(''); setDeleteOpen(true); }}>
              <FiTrash2 />
              <span>Xóa tài khoản</span>
            </button>
          </section>
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
            <Button type="submit" className="booking-btn-general2" disabled={saving}>
              {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>

      <Modal show={deleteOpen} onHide={() => setDeleteOpen(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Xóa tài khoản</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {modalError && <Alert variant="danger">{modalError}</Alert>}
          <p className="mb-0">
            Tài khoản sẽ bị vô hiệu hóa và bạn cần đăng ký lại nếu muốn sử dụng dịch vụ sau này.
          </p>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="light" onClick={() => setDeleteOpen(false)} disabled={saving}>Hủy</Button>
          <Button variant="danger" onClick={handleDeactivateAccount} disabled={saving}>
            {saving ? 'Đang xử lý...' : 'Xóa tài khoản'}
          </Button>
        </Modal.Footer>
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
