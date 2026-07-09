import { useState, useEffect } from 'react';
import { Form, Row, Col, Button } from 'react-bootstrap';
import { BsSearch, BsArrowCounterclockwise } from 'react-icons/bs';
import accountApi from '../api/accountApi';

const STAFF_POSITIONS = ['DRIVER', 'ATTENDANT', 'TICKET_STAFF', 'MANAGER'];
const AUTH_PROVIDERS = ['local', 'firebase', 'google', 'facebook'];

export default function AccountFilter({ filters, onFilterChange, onReset }) {
    const [roles, setRoles] = useState([]);

    useEffect(() => {
        accountApi.getAllRoles().then(res => setRoles(res || [])).catch(() => {});
    }, []);

    return (
        <div className="card mb-3">
            <div className="card-body">
                <Row className="g-2 align-items-end">
                    <Col md={3}>
                        <Form.Label className="fw-semibold small">Tìm kiếm</Form.Label>
                        <div className="position-relative">
                            <Form.Control
                                type="text"
                                name="search"
                                value={filters.search}
                                onChange={onFilterChange}
                                placeholder="Username, tên NV, SĐT..."
                                size="sm"
                            />
                            <BsSearch className="position-absolute top-50 end-0 translate-middle-y me-2 text-muted" />
                        </div>
                    </Col>
                    <Col md={2}>
                        <Form.Label className="fw-semibold small">Vai trò</Form.Label>
                        <Form.Select name="role" value={filters.role} onChange={onFilterChange} size="sm">
                            <option value="">Tất cả</option>
                            {roles.map(r => (
                                <option key={r.roleId} value={r.roleName}>{r.roleName}</option>
                            ))}
                        </Form.Select>
                    </Col>
                    <Col md={2}>
                        <Form.Label className="fw-semibold small">Trạng thái</Form.Label>
                        <Form.Select name="isActive" value={filters.isActive} onChange={onFilterChange} size="sm">
                            <option value="">Tất cả</option>
                            <option value="true">Hoạt động</option>
                            <option value="false">Vô hiệu hóa</option>
                        </Form.Select>
                    </Col>
                    <Col md={2}>
                        <Form.Label className="fw-semibold small">Chức vụ</Form.Label>
                        <Form.Select name="staffPosition" value={filters.staffPosition} onChange={onFilterChange} size="sm">
                            <option value="">Tất cả</option>
                            {STAFF_POSITIONS.map(pos => (
                                <option key={pos} value={pos}>{pos}</option>
                            ))}
                        </Form.Select>
                    </Col>
                    <Col md={1}>
                        <Button variant="outline-secondary" size="sm" onClick={onReset} title="Xóa bộ lọc">
                            <BsArrowCounterclockwise />
                        </Button>
                    </Col>
                </Row>
            </div>
        </div>
    );
}
