import { Form, Row, Col, Button } from 'react-bootstrap';
import { BsSearch, BsArrowCounterclockwise } from 'react-icons/bs';

const POSITIONS = ['DRIVER', 'ATTENDANT', 'TICKET_STAFF', 'MANAGER'];

export default function StaffFilter({ filters, onFilterChange, onReset, ticketAgencies }) {
    return (
        <div className="card mb-3">
            <div className="card-body">
                <Row className="g-2 align-items-end">
                    <Col md={3}>
                        <Form.Label className="fw-semibold small">Tìm kiếm</Form.Label>
                        <div className="position-relative">
                            <Form.Control type="text" name="search" value={filters.search}
                                onChange={onFilterChange} placeholder="Tên, SĐT, email, CCCD..." size="sm" />
                            <BsSearch className="position-absolute top-50 end-0 translate-middle-y me-2 text-muted" />
                        </div>
                    </Col>
                    <Col md={2}>
                        <Form.Label className="fw-semibold small">Chức vụ</Form.Label>
                        <Form.Select name="staffPosition" value={filters.staffPosition} onChange={onFilterChange} size="sm">
                            <option value="">Tất cả</option>
                            {POSITIONS.map(p => (
                                <option key={p} value={p}>{p}</option>
                            ))}
                        </Form.Select>
                    </Col>
                    <Col md={2}>
                        <Form.Label className="fw-semibold small">Bến xe / Đại lý</Form.Label>
                        <Form.Select name="ticketAgencyId" value={filters.ticketAgencyId} onChange={onFilterChange} size="sm">
                            <option value="">Tất cả</option>
                            {(ticketAgencies || []).map(ta => (
                                <option key={ta.ticketAgencyId} value={ta.ticketAgencyId}>
                                    {ta.ticketAgencyName}  ({ta.stopPointName}{ta.city ? `, ${ta.city}` : ''})
                                </option>
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
