import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner } from 'react-bootstrap';
import { BsExclamationTriangleFill, BsInfoCircle } from 'react-icons/bs';
import staffApi from '../api/staffApi';
import accountApi from '../../manage-accounts/api/accountApi';

const STAFF_ROLES = ['ADMIN', 'MANAGER', 'TICKET_STAFF', 'TRIP_STAFF'];

export default function StaffUpdateModal({ isOpen, data, onClose, onSuccess, ticketAgencies }) {
    const [form, setForm] = useState({
        staffName: '', phone: '', email: '', dob: '', cccd: '',
        hireDate: '', ticketAgencyId: '', isActive: true,
    });
    const [staffPosition, setStaffPosition] = useState('');
    const [roles, setRoles] = useState([]);
    const [selectedRoles, setSelectedRoles] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');
    const [warnMsg, setWarnMsg] = useState('');
    const [loading, setLoading] = useState(false);

    const roleIdByName = {};
    roles.forEach(r => { roleIdByName[r.roleName] = r.roleId; });

    useEffect(() => {
        if (isOpen && data) {
            setLoading(true);
            setErrorMsg('');
            setWarnMsg('');
            staffApi.getDetail(data.staffId)
                .then(detail => {
                    const accountId = detail.accountId;
                    const detailData = detail;
                    return Promise.all([
                        Promise.resolve(detailData),
                        accountApi.getAllRoles().catch(() => []),
                        accountId ? accountApi.getAccountDetail(accountId).catch(() => null) : Promise.resolve(null),
                    ]);
                })
                .catch(() => {
                    return Promise.all([
                        Promise.resolve(data),
                        accountApi.getAllRoles().catch(() => []),
                        Promise.resolve(null),
                    ]);
                })
                .then(([detail, allRoles, accountDetail]) => {
                    const rolesList = (allRoles || []).filter(r => STAFF_ROLES.includes(r.roleName));
                    const nameToId = {};
                    rolesList.forEach(r => { nameToId[r.roleName] = r.roleId; });
                    setRoles(rolesList);

                    setForm({
                        staffName: detail.staffName || '',
                        phone: detail.phone || '',
                        email: detail.email || '',
                        dob: detail.dob || '',
                        cccd: detail.cccd || '',
                        hireDate: detail.hireDate || '',
                        ticketAgencyId: detail.ticketAgencyId ?? '',
                        isActive: detail.active !== false,
                    });

                    const pos = detail.staffPosition || '';
                    setStaffPosition(pos);

                    const actualIds = (accountDetail?.roles || [])
                        .map(r => r.roleId)
                        .filter(id => rolesList.some(r => r.roleId === id));

                    if (actualIds.length > 0) {
                        setSelectedRoles([actualIds[0]]);
                    } else {
                        let fallbackId;
                        if (pos === 'DRIVER' || pos === 'ATTENDANT') {
                            fallbackId = nameToId['TRIP_STAFF'];
                        } else if (pos === 'TICKET_STAFF') {
                            fallbackId = nameToId['TICKET_STAFF'];
                        } else if (pos === 'MANAGER') {
                            fallbackId = nameToId['MANAGER'];
                        }
                        setSelectedRoles(fallbackId != null ? [fallbackId] : []);
                    }
                })
                .finally(() => setLoading(false));
        }
    }, [isOpen, data]);

    const selectedRoleNames = roles
        .filter(r => selectedRoles.includes(r.roleId))
        .map(r => r.roleName);

    const isAdminOrManager = selectedRoleNames.some(n => n === 'ADMIN' || n === 'MANAGER');

    const positionOptions = [];
    if (!isAdminOrManager) {
        if (selectedRoleNames.includes('TRIP_STAFF')) {
            positionOptions.push('DRIVER', 'ATTENDANT');
        }
        if (selectedRoleNames.includes('TICKET_STAFF')) {
            positionOptions.push('TICKET_STAFF');
        }
        if (staffPosition && !positionOptions.includes(staffPosition)) {
            positionOptions.push(staffPosition);
        }
    }

    const agencyEnabled = staffPosition === 'TICKET_STAFF' || staffPosition === 'MANAGER';
    const agencyRequired = staffPosition === 'TICKET_STAFF';

    useEffect(() => {
        if (isAdminOrManager) {
            setStaffPosition('MANAGER');
        }
    }, [isAdminOrManager]);

    useEffect(() => {
        if (!isAdminOrManager && staffPosition && !agencyEnabled) {
            setForm(prev => ({ ...prev, ticketAgencyId: '' }));
        }
    }, [staffPosition, isAdminOrManager]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    };

    const handlePositionChange = (pos) => {
        setStaffPosition(pos);
        const tripId = roleIdByName['TRIP_STAFF'];
        const ticketId = roleIdByName['TICKET_STAFF'];
        if (pos === 'DRIVER' || pos === 'ATTENDANT') {
            if (tripId != null) setSelectedRoles([tripId]);
        } else if (pos === 'TICKET_STAFF') {
            if (ticketId != null) setSelectedRoles([ticketId]);
        }
    };

    const handleRoleToggle = (roleId) => {
        if (selectedRoles.includes(roleId)) {
            setSelectedRoles([]);
            return;
        }
        setSelectedRoles([roleId]);
        const name = roles.find(r => r.roleId === roleId)?.roleName;
        if (name === 'TRIP_STAFF' && (staffPosition !== 'DRIVER' && staffPosition !== 'ATTENDANT')) {
            setStaffPosition('DRIVER');
        } else if (name === 'TICKET_STAFF') {
            setStaffPosition('TICKET_STAFF');
        } else if (name === 'MANAGER' || name === 'ADMIN') {
            setStaffPosition('MANAGER');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');
        setWarnMsg('');

        if (!staffPosition) {
            setErrorMsg('Vui lòng chọn chức vụ.');
            return;
        }
        if (selectedRoles.length === 0) {
            setErrorMsg('Vui lọn chọn ít nhất một vai trò trong Phân quyền.');
            return;
        }

        if (staffPosition === 'TICKET_STAFF') {
            if (!selectedRoleNames.includes('TICKET_STAFF')) {
                setErrorMsg('Nhân viên bán vé phải có vai trò TICKET_STAFF.');
                return;
            }
        } else if (staffPosition === 'DRIVER' || staffPosition === 'ATTENDANT') {
            if (!selectedRoleNames.includes('TRIP_STAFF')) {
                setErrorMsg('Tài xế / Phụ xe phải có vai trò TRIP_STAFF.');
                return;
            }
        }

        if (agencyRequired && !form.ticketAgencyId) {
            setErrorMsg('Nhân viên bán vé phải trực thuộc một bến xe / đại lý.');
            return;
        }

        if (form.hireDate && new Date(form.hireDate) < new Date('1900-01-01')) {
            setErrorMsg('Ngày vào làm không hợp lệ.');
            return;
        }
        const cccd = form.cccd.trim();
        if (cccd && !/^[0-9]{9,12}$/.test(cccd)) {
            setErrorMsg('CCCD không hợp lệ (9-12 chữ số).');
            return;
        }
        if (form.dob) {
            const birth = new Date(form.dob);
            const cutoff = new Date();
            cutoff.setFullYear(cutoff.getFullYear() - 16);
            if (birth > cutoff) {
                setErrorMsg('Nhân viên phải từ đủ 16 tuổi trở lên.');
                return;
            }
        }

        if (staffPosition === 'DRIVER' || staffPosition === 'ATTENDANT') {
            if (!selectedRoleNames.includes('TRIP_STAFF')) {
                setWarnMsg('Chức vụ là tài xế/phụ xe nhưng không có vai trò TRIP_STAFF.');
            }
        } else if (staffPosition === 'TICKET_STAFF' && !selectedRoleNames.includes('TICKET_STAFF')) {
            setWarnMsg('Chức vụ là bán vé nhưng không có vai trò TICKET_STAFF.');
        }

        setIsSubmitting(true);
        try {
            await staffApi.update(data.staffId, {
                staffName: form.staffName.trim(),
                phone: form.phone.trim() || null,
                email: form.email.trim() || null,
                dob: form.dob || null,
                cccd: form.cccd.trim() || null,
                staffPosition,
                hireDate: form.hireDate || null,
                ticketAgencyId: agencyEnabled && form.ticketAgencyId
                    ? parseInt(form.ticketAgencyId, 10) : null,
                isActive: form.isActive,
                roleIds: selectedRoles,
            });
            onSuccess();
            onClose();
        } catch (err) {
            const respData = err.response?.data;
            if (respData?.fieldErrors) {
                const msgs = Object.entries(respData.fieldErrors).map(([f, m]) => `- ${f}: ${m}`);
                setErrorMsg(['Dữ liệu không hợp lệ:', ...msgs].join('\n'));
            } else {
                setErrorMsg(respData?.message || 'Có lỗi xảy ra.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) {
        return (
            <Modal show={isOpen} onHide={onClose} centered>
                <Modal.Body className="text-center py-5"><Spinner animation="border" /></Modal.Body>
            </Modal>
        );
    }

    return (
        <Modal show={isOpen} onHide={onClose} centered size="lg">
            <Modal.Header closeButton><Modal.Title>Cập nhật nhân viên</Modal.Title></Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {errorMsg && (
                        <Alert variant="danger" className="d-flex align-items-center gap-2 py-2">
                            <BsExclamationTriangleFill /><span style={{whiteSpace: 'pre-line'}}>{errorMsg}</span>
                        </Alert>
                    )}
                    {warnMsg && (
                        <Alert variant="warning" className="d-flex align-items-center gap-2 py-2">
                            <BsInfoCircle /><span>{warnMsg}</span>
                        </Alert>
                    )}
                    <Row className="g-2">
                        <Col md={6}>
                            <Form.Label className="small">Họ tên <span className="text-danger">*</span></Form.Label>
                            <Form.Control name="staffName" value={form.staffName} onChange={handleChange} required maxLength={200} size="sm" />
                        </Col>
                        <Col md={6}>
                            <Form.Label className="small">SĐT</Form.Label>
                            <Form.Control name="phone" value={form.phone} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={6}>
                            <Form.Label className="small">Email</Form.Label>
                            <Form.Control type="email" name="email" value={form.email} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Ngày sinh</Form.Label>
                            <Form.Control type="date" name="dob" value={form.dob} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">CCCD</Form.Label>
                            <Form.Control name="cccd" value={form.cccd} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Chức vụ <span className="text-danger">*</span></Form.Label>
                            {isAdminOrManager ? (
                                <div>
                                    <Form.Control type="text" value="MANAGER" size="sm" disabled />
                                    <small className="text-muted">Admin/Manager không có chức vụ riêng</small>
                                </div>
                            ) : (
                                <Form.Select value={staffPosition} onChange={e => handlePositionChange(e.target.value)} size="sm">
                                    <option value="">-- Chọn chức vụ --</option>
                                    {positionOptions.map(p => (
                                        <option key={p} value={p}>{p}</option>
                                    ))}
                                </Form.Select>
                            )}
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Ngày vào làm</Form.Label>
                            <Form.Control type="date" name="hireDate" value={form.hireDate} onChange={handleChange} size="sm" />
                        </Col>
                        <Col md={3}>
                            <Form.Label className="small">Bến xe / Đại lý {agencyRequired && <span className="text-danger">*</span>}</Form.Label>
                            <Form.Select name="ticketAgencyId" value={form.ticketAgencyId} onChange={handleChange} size="sm" disabled={!agencyEnabled}>
                                <option value="">-- Không trực thuộc --</option>
                                {(ticketAgencies || []).map(ta => (
                                    <option key={ta.ticketAgencyId} value={ta.ticketAgencyId}>
                                        {ta.ticketAgencyName}  ({ta.stopPointName}{ta.city ? `, ${ta.city}` : ''})
                                    </option>
                                ))}
                            </Form.Select>
                        </Col>
                        {/* <Col md={3} className="d-flex align-items-end">
                            <Form.Check type="switch" id="s-active-switch" label="Kích hoạt"
                                name="isActive" checked={form.isActive} onChange={handleChange} />
                        </Col> */}
                    </Row>
                    <h6 className="fw-bold text-secondary border-bottom pb-2 mt-3">Phân quyền</h6>
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
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Hủy</Button>
                    <Button variant="primary" type="submit" disabled={isSubmitting}>
                        {isSubmitting ? <><Spinner size="sm" /> Đang lưu...</> : 'Lưu thay đổi'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}