import { useState, useEffect, useMemo } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner, Badge } from 'react-bootstrap';
import { BsExclamationTriangleFill, BsInfoCircle } from 'react-icons/bs';
import accountApi from '../api/accountApi';

const STAFF_ROLES = ['ADMIN', 'MANAGER', 'TICKET_STAFF', 'TRIP_STAFF'];
const CUSTOMER_ROLE = 'CUSTOMER';

function resolveAccountSide(data, roleNames) {
    if (data?.customerName) return 'customer';
    if (data?.staffName || data?.staffId) return 'staff';

    const hasStaffRole = roleNames.some((name) => STAFF_ROLES.includes(name));
    const hasCustomerRole = roleNames.includes(CUSTOMER_ROLE);

    if (hasStaffRole && !hasCustomerRole) return 'staff';
    if (hasCustomerRole && !hasStaffRole) return 'customer';
    return 'unknown';
}

export default function AccountRoleModal({ isOpen, data, onClose, onSuccess }) {
    const [allRoles, setAllRoles] = useState([]);
    const [selectedRoles, setSelectedRoles] = useState([]);
    const [currentRoles, setCurrentRoles] = useState([]);
    const [currentRoleNames, setCurrentRoleNames] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (isOpen && data) {
            setErrorMsg('');
            setIsSubmitting(false);
            accountApi.getAccountDetail(data.accountId)
                .then((detail) => {
                    const roles = detail.roles || [];
                    const roleIds = roles.map((r) => r.roleId);
                    setCurrentRoles(roleIds);
                    setSelectedRoles([...roleIds]);
                    setCurrentRoleNames(roles.map((r) => r.roleName));
                })
                .catch(() => {
                    const fallbackNames = data.roles || [];
                    setCurrentRoleNames(fallbackNames);
                });
            accountApi.getAllRoles()
                .then((res) => setAllRoles(res || []))
                .catch(() => {});
        }
    }, [isOpen, data]);

    const accountSide = useMemo(
        () => resolveAccountSide(data, currentRoleNames),
        [data, currentRoleNames]
    );

    const allowedRoles = useMemo(() => {
        if (accountSide === 'customer') {
            return allRoles.filter((r) => r.roleName === CUSTOMER_ROLE);
        }
        if (accountSide === 'staff') {
            return allRoles.filter((r) => STAFF_ROLES.includes(r.roleName));
        }
        return allRoles;
    }, [allRoles, accountSide]);

    const sideHint = accountSide === 'customer'
        ? 'Tài khoản khách hàng chỉ được giữ role CUSTOMER — không đổi sang role nhân viên.'
        : accountSide === 'staff'
            ? 'Tài khoản nhân viên chỉ đổi trong ADMIN / MANAGER / TICKET_STAFF / TRIP_STAFF — không đổi sang CUSTOMER.'
            : null;

    const handleToggle = (roleId) => {
        setSelectedRoles((prev) => (prev.includes(roleId) ? [] : [roleId]));
    };

    const hasChanges = JSON.stringify([...selectedRoles].sort()) !== JSON.stringify([...currentRoles].sort());

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setErrorMsg('');

        const selectedNames = allowedRoles
            .filter((r) => selectedRoles.includes(r.roleId))
            .map((r) => r.roleName);

        if (accountSide === 'customer' && selectedNames.some((n) => STAFF_ROLES.includes(n))) {
            setErrorMsg('Tài khoản khách hàng không được đổi sang role nhân viên.');
            setIsSubmitting(false);
            return;
        }
        if (accountSide === 'staff' && selectedNames.includes(CUSTOMER_ROLE)) {
            setErrorMsg('Tài khoản nhân viên không được đổi sang role CUSTOMER.');
            setIsSubmitting(false);
            return;
        }

        const roleIdsToSave = selectedRoles.filter((id) =>
            allowedRoles.some((r) => r.roleId === id)
        );

        try {
            await accountApi.assignRoles(data.accountId, { roleIds: roleIdsToSave });
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
                <Modal.Title>Phân quyền — <span className="text-muted">{data?.username}</span></Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    {errorMsg && (
                        <Alert variant="danger" className="d-flex align-items-center gap-2 py-2">
                            <BsExclamationTriangleFill /><span>{errorMsg}</span>
                        </Alert>
                    )}
                    {sideHint && (
                        <Alert variant="info" className="d-flex align-items-start gap-2 py-2">
                            <BsInfoCircle className="mt-1 flex-shrink-0" />
                            <span>{sideHint}</span>
                        </Alert>
                    )}
                    {allowedRoles.length === 0 ? (
                        <p className="text-muted">Đang tải...</p>
                    ) : (
                        <Row className="g-2">
                            {allowedRoles.map((role) => (
                                <Col md={6} key={role.roleId}>
                                    <Form.Check
                                        type="switch"
                                        label={
                                            <span>
                                                {role.roleName}{' '}
                                                <Badge bg="light" text="dark" className="ms-1">ID: {role.roleId}</Badge>
                                            </span>
                                        }
                                        checked={selectedRoles.includes(role.roleId)}
                                        onChange={() => handleToggle(role.roleId)}
                                    />
                                </Col>
                            ))}
                        </Row>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>Hủy</Button>
                    <Button variant="primary" type="submit" disabled={isSubmitting || !hasChanges}>
                        {isSubmitting ? <><Spinner size="sm" /> Đang lưu...</> : 'Lưu phân quyền'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
