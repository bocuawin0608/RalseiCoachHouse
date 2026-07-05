import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill, BsCheckCircle } from 'react-icons/bs';
import staffApi from '../api/staffApi';
import accountApi from '../../manage-accounts/api/accountApi';

const STAFF_POSITIONS = ['DRIVER', 'ATTENDANT', 'TICKET_STAFF', 'MANAGER'];

export default function StaffOnboardModal({ isOpen, onClose, onSuccess, ticketAgencies }) {
    const [form, setForm] = useState({
        staffName: '', phone: '', email: '', cccd: '', dob: '',
        staffPosition: 'TICKET_STAFF', hireDate: '', ticketAgencyId: '',
    });
    const [roles, setRoles] = useState([]);
    const [selectedRoles, setSelectedRoles] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');
    const [result, setResult] = useState(null);

    useEffect(() => {
        if (isOpen) {
            setForm({
                staffName: '', phone: '', email: '', cccd: '', dob: '',
                staffPosition: 'TICKET_STAFF', hireDate: '', ticketAgencyId: '',
            });
            setSelectedRoles([]);
            setErrorMsg('');
            setResult(null);
            accountApi.getAllRoles().then(res => setRoles(res || [])).catch(() => {});
        }
    }, [isOpen]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
    };

    const handleRoleToggle = (roleId) => {
        setSelectedRoles(prev =>
            prev.includes(roleId) ? prev.filter(id => id !== roleId) : [...prev, roleId]
        );
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');
        setIsSubmitting(true);
        setResult(null);
        try {
            const payload = {
                staffName: form.staffName.trim(),
                phone: form.phone.trim(),
                email: form.email.trim() || null,
                cccd: form.cccd.trim() || null,
                dob: form.dob || null,
                staffPosition: form.staffPosition,
                hireDate: form.hireDate,
                ticketAgencyId: parseInt(form.ticketAgencyId, 10),
                roleIds: selectedRoles,
            };
            const res = await staffApi.onboard(payload);
            setResult(res);
        } catch (err) {
            const data = err.response?.data;
            if (data?.fieldErrors) {
                const msgs = Object.entries(data.fieldErrors).map(([f, m]) => `- ${f}: ${m}`);
                setErrorMsg(['Dữ liệu không hợp lệ:', ...msgs].join('\n'));
            } else {
                setErrorMsg(data?.message || 'Có lỗi xảy ra.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleClose = () => {
        if (result) onSuccess();
        onClose();
    };

    return (
        <Modal show={isOpen} onHide={handleClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>{result ? 'Onboard thành công' : 'Onboard nhân viên mới'}</Modal.Title>
            </Modal.Header>
            {result ? (
                <Modal.Body>
                    <Alert variant="success" className="d-flex align-items-center gap-2">
                        <BsCheckCircle size={24} />
                        <div>
                            <strong>Đã tạo nhân viên và tài khoản!</strong><br />
                            Username: <code>{result.username}</code><br />
                            Mật khẩu: <code>123456</code>
                        </div>
                    </Alert>
                </Modal.Body>
            ) : (
                <Form onSubmit={handleSubmit}>
                    <Modal.Body>
                        {errorMsg && (
                            <Alert variant="danger" className="py-2 mb-3">
                                <div className="d-flex align-items-start gap-2">
                                    <BsExclamationTriangleFill className="mt-1 flex-shrink-0" />
                                    <span style={{whiteSpace: 'pre-line'}}>{errorMsg}</span>
                                </div>
                            </Alert>
                        )}
                        <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin nhân sự</h6>
                        <Row className="g-2 mb-3">
                            <Col md={4}>
                                <Form.Label className="small">Họ tên <span className="text-danger">*</span></Form.Label>
                                <Form.Control name="staffName" value={form.staffName} onChange={handleChange} required maxLength={100} size="sm" />
                            </Col>
                            <Col md={3}>
                                <Form.Label className="small">SĐT (username) <span className="text-danger">*</span></Form.Label>
                                <Form.Control name="phone" value={form.phone} onChange={handleChange} required size="sm" />
                            </Col>
                            <Col md={3}>
                                <Form.Label className="small">Email</Form.Label>
                                <Form.Control type="email" name="email" value={form.email} onChange={handleChange} size="sm" />
                            </Col>
                            <Col md={2}>
                                <Form.Label className="small">CCCD</Form.Label>
                                <Form.Control name="cccd" value={form.cccd} onChange={handleChange} size="sm" />
                            </Col>
                            <Col md={3}>
                                <Form.Label className="small">Ngày sinh</Form.Label>
                                <Form.Control type="date" name="dob" value={form.dob} onChange={handleChange} size="sm" />
                            </Col>
                            <Col md={3}>
                                <Form.Label className="small">Chức vụ <span className="text-danger">*</span></Form.Label>
                                <Form.Select name="staffPosition" value={form.staffPosition} onChange={handleChange} size="sm">
                                    {STAFF_POSITIONS.map(pos => (
                                        <option key={pos} value={pos}>{pos}</option>
                                    ))}
                                </Form.Select>
                            </Col>
                            <Col md={3}>
                                <Form.Label className="small">Ngày vào làm <span className="text-danger">*</span></Form.Label>
                                <Form.Control type="date" name="hireDate" value={form.hireDate} onChange={handleChange} required size="sm" />
                            </Col>
                            <Col md={3}>
                                <Form.Label className="small">Bến xe <span className="text-danger">*</span></Form.Label>
                                <Form.Select name="ticketAgencyId" value={form.ticketAgencyId} onChange={handleChange} required size="sm">
                                    <option value="">-- Chọn bến xe --</option>
                                    {ticketAgencies.map(ta => (
                                        <option key={ta.ticketAgencyId} value={ta.ticketAgencyId}>{ta.ticketAgencyName}</option>
                                    ))}
                                </Form.Select>
                            </Col>
                        </Row>
                        <h6 className="fw-bold text-secondary border-bottom pb-2">Phân quyền</h6>
                        <Row className="g-2">
                            {roles.length === 0 ? (
                                <p className="text-muted small">Đang tải danh sách vai trò...</p>
                            ) : (
                                roles.map(role => (
                                    <Col md={3} key={role.roleId}>
                                        <Form.Check type="checkbox" label={role.roleName}
                                            checked={selectedRoles.includes(role.roleId)}
                                            onChange={() => handleRoleToggle(role.roleId)} />
                                    </Col>
                                ))
                            )}
                        </Row>
                        <small className="text-muted">Mật khẩu mặc định: <code>123456</code></small>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Hủy</Button>
                        <Button variant="primary" type="submit" disabled={isSubmitting || selectedRoles.length === 0}>
                            {isSubmitting ? <><Spinner size="sm" /> Đang xử lý...</> : 'Onboard'}
                        </Button>
                    </Modal.Footer>
                </Form>
            )}
        </Modal>
    );
}
