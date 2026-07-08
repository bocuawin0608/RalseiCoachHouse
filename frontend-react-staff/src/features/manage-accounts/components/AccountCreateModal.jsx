import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import accountApi from '../api/accountApi';

const STAFF_POSITIONS = ['DRIVER', 'ATTENDANT', 'TICKET_STAFF', 'MANAGER'];

export default function AccountCreateModal({ isOpen, onClose, onSuccess }) {
    const [form, setForm] = useState({
        username: '', password: '', confirmPassword: '',
        staffName: '', phone: '', email: '', cccd: '', dob: '',
        staffPosition: 'TICKET_STAFF', ticketAgencyId: '', hireDate: '',
    });
    const [roles, setRoles] = useState([]);
    const [selectedRoles, setSelectedRoles] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (isOpen) {
            setForm({
                username: '', password: '', confirmPassword: '',
                staffName: '', phone: '', email: '', cccd: '', dob: '',
                staffPosition: 'TICKET_STAFF', ticketAgencyId: '', hireDate: '',
            });
            setSelectedRoles([]);
            setErrorMsg('');
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

        if (form.password !== form.confirmPassword) {
            setErrorMsg('Mật khẩu xác nhận không khớp.');
            return;
        }

        // Validate hireDate >= 1900
        if (form.hireDate && new Date(form.hireDate) < new Date('1900-01-01')) {
            setErrorMsg('Ngày vào làm không hợp lệ.');
            return;
        }
        // Validate DOB >= 16
        if (form.dob) {
            const birth = new Date(form.dob);
            const cutoff = new Date();
            cutoff.setFullYear(cutoff.getFullYear() - 16);
            if (birth > cutoff) {
                setErrorMsg('Nhân viên phải từ đủ 16 tuổi trở lên.');
                return;
            }
        }

        setIsSubmitting(true);
        try {
            const payload = {
                username: form.username,
                password: form.password,
                staffName: form.staffName,
                phone: form.phone,
                email: form.email || null,
                cccd: form.cccd || null,
                dob: form.dob || null,
                staffPosition: form.staffPosition,
                ticketAgencyId: form.ticketAgencyId ? parseInt(form.ticketAgencyId) : null,
                hireDate: form.hireDate,
                roleIds: selectedRoles,
            };
            await accountApi.createAccount(payload);
            onSuccess();
            onClose();
        } catch (err) {
            const data = err.response?.data;
            if (data?.fieldErrors) {
                const msgs = Object.entries(data.fieldErrors).map(([field, msg]) => `- ${field}: ${msg}`);
                setErrorMsg(['Dữ liệu đầu vào không hợp lệ:', ...msgs].join('\n'));
            } else {
                setErrorMsg(data?.message || 'Có lỗi xảy ra khi tạo tài khoản.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} size="lg" centered>
            <Modal.Header closeButton>
                <Modal.Title>Thêm tài khoản nhân viên</Modal.Title>
            </Modal.Header>
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
                    <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin tài khoản</h6>
                    <Row className="g-2 mb-3">
                        <Col md={6}>
                            <Form.Label className="small">Username (SĐT) <span className="text-danger">*</span></Form.Label>
                            <Form.Control name="username" value={form.username} onChange={handleChange} required size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Mật khẩu <span className="text-danger">*</span></Form.Label>
                            <Form.Control type="password" name="password" value={form.password} onChange={handleChange} required size="sm" minLength={6} />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Xác nhận MK <span className="text-danger">*</span></Form.Label>
                            <Form.Control type="password" name="confirmPassword" value={form.confirmPassword} onChange={handleChange} required size="sm" />
                        </Col>
                    </Row>

                    <h6 className="fw-bold text-secondary border-bottom pb-2">Thông tin nhân sự</h6>
                    <Row className="g-2 mb-3">
                        <Col md={4}>
                            <Form.Label className="small">Họ tên <span className="text-danger">*</span></Form.Label>
                            <Form.Control name="staffName" value={form.staffName} onChange={handleChange} required size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">SĐT <span className="text-danger">*</span></Form.Label>
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
                            <Form.Label className="small">ID bến xe</Form.Label>
                            <Form.Control type="number" name="ticketAgencyId" value={form.ticketAgencyId} onChange={handleChange} size="sm" placeholder="Để trống nếu không có" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Ngày vào làm <span className="text-danger">*</span></Form.Label>
                            <Form.Control type="date" name="hireDate" value={form.hireDate} onChange={handleChange} required size="sm" />
                        </Col>
                    </Row>

                    <h6 className="fw-bold text-secondary border-bottom pb-2">Phân quyền</h6>
                    <Row className="g-2">
                        {roles.length === 0 ? (
                            <p className="text-muted small">Đang tải danh sách vai trò...</p>
                        ) : (
                            roles.map(role => (
                                <Col md={3} key={role.roleId}>
                                    <Form.Check
                                        type="checkbox"
                                        label={role.roleName}
                                        checked={selectedRoles.includes(role.roleId)}
                                        onChange={() => handleRoleToggle(role.roleId)}
                                    />
                                </Col>
                            ))
                        )}
                    </Row>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Hủy</Button>
                    <Button variant="primary" type="submit" disabled={isSubmitting}>
                        {isSubmitting ? <><Spinner size="sm" /> Đang tạo...</> : 'Tạo tài khoản'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
