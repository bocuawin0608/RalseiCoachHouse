import { useState, useEffect } from 'react';
import { Form, Row, Col, Button } from 'react-bootstrap';
import { BsSearch, BsArrowCounterclockwise } from 'react-icons/bs';
import accountApi from '../api/accountApi';

export default function AccountFilter({ filters, onFilterChange, onReset }) {
    const [roles, setRoles] = useState([]);

    useEffect(() => {
        accountApi.getAllRoles().then(res => setRoles(res || [])).catch(() => {});
    }, []);

    return (
        <div className="card mb-3">
            <div className="card-body">
                <Row className="g-2 align-items-end">
                    <Col md={4}>
                        <Form.Label className="fw-semibold small">Tìm kiếm</Form.Label>
                        <div className="position-relative">
                            <Form.Control
                                type="text"
                                name="search"
                                value={filters.search}
                                onChange={onFilterChange}
                                placeholder="Username, tên NV..."
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
