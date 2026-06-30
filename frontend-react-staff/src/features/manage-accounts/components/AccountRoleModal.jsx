import { useState, useEffect } from 'react';
import { Modal, Form, Row, Col, Button, Alert, Spinner, Badge } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import accountApi from '../api/accountApi';

export default function AccountRoleModal({ isOpen, data, onClose, onSuccess }) {
    const [allRoles, setAllRoles] = useState([]);
    const [selectedRoles, setSelectedRoles] = useState([]);
    const [currentRoles, setCurrentRoles] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        if (isOpen && data) {
            setErrorMsg('');
            setIsSubmitting(false);
            accountApi.getAccountDetail(data.accountId)
                .then(res => {
                    const detail = res;
                    const roleIds = (detail.roles || []).map(r => r.roleId);
                    setCurrentRoles(roleIds);
                    setSelectedRoles([...roleIds]);
                })
                .catch(() => {});
            accountApi.getAllRoles()
                .then(res => setAllRoles(res || []))
                .catch(() => {});
        }
    }, [isOpen, data]);

    const handleToggle = (roleId) => {
        setSelectedRoles(prev =>
            prev.includes(roleId) ? prev.filter(id => id !== roleId) : [...prev, roleId]
        );
    };

    const hasChanges = JSON.stringify([...selectedRoles].sort()) !== JSON.stringify([...currentRoles].sort());

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setErrorMsg('');
        try {
            await accountApi.assignRoles(data.accountId, { roleIds: selectedRoles });
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
                    {allRoles.length === 0 ? (
                        <p className="text-muted">Đang tải...</p>
                    ) : (
                        <Row className="g-2">
                            {allRoles.map(role => (
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
