import { Form, Row, Col, Button } from 'react-bootstrap';
import { BsSearch, BsArrowCounterclockwise } from 'react-icons/bs';

export default function CustomerFilter({ filters, onFilterChange, onReset }) {
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
                                placeholder="Tên, SĐT, email..."
                                size="sm"
                            />
                            <BsSearch className="position-absolute top-50 end-0 translate-middle-y me-2 text-muted" />
                        </div>
                    </Col>
                    <Col md={2}>
                        <Form.Label className="fw-semibold small">Loại tài khoản</Form.Label>
                        <Form.Select name="accountType" value={filters.accountType} onChange={onFilterChange} size="sm">
                            <option value="">Tất cả</option>
                            <option value="registered">Đã đăng ký</option>
                            <option value="crm">CRM</option>
                        </Form.Select>
                    </Col>
                    <Col md={2}>
                        <Form.Label className="fw-semibold small">Hoạt động gần đây</Form.Label>
                        <Form.Select name="activity" value={filters.activity} onChange={onFilterChange} size="sm">
                            <option value="">Tất cả</option>
                            <option value="active">Đang hoạt động</option>
                            <option value="inactive_3mo">Không hoạt động &gt;3 tháng</option>
                            <option value="never_booked">Chưa từng đặt vé</option>
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