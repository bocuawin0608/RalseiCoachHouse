import { useState } from 'react';
import { Modal, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import accountApi from '../api/accountApi';

export default function AccountResetPasswordModal({ isOpen, data, onClose, onSuccess }) {
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        if (newPassword.length < 6) {
            setErrorMsg('Mật khẩu phải có ít nhất 6 ký tự.');
            return;
        }
        if (newPassword !== confirmPassword) {
            setErrorMsg('Mật khẩu xác nhận không khớp.');
            return;
        }

        setIsSubmitting(true);
        try {
            await accountApi.resetPassword(data.accountId, { newPassword });
            onSuccess();
            onClose();
        } catch (err) {
            setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} centered>
            <Modal.Header closeButton>
                <Modal.Title>Đặt lại mật khẩu — <span className="text-muted">{data?.username}</span></Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {errorMsg && (
                        <Alert variant="danger" className="d-flex align-items-center gap-2 py-2">
                            <BsExclamationTriangleFill /><span>{errorMsg}</span>
                        </Alert>
                    )}
                    <Form.Group className="mb-2">
                        <Form.Label className="small">Mật khẩu mới</Form.Label>
                        <Form.Control type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} required minLength={6} />
                    </Form.Group>
                    <Form.Group>
                        <Form.Label className="small">Xác nhận mật khẩu</Form.Label>
                        <Form.Control type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Hủy</Button>
                    <Button variant="warning" type="submit" disabled={isSubmitting}>
                        {isSubmitting ? <><Spinner size="sm" /> Đang xử lý...</> : 'Đặt lại mật khẩu'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
