import { Button, Card, Col, Form, Row } from 'react-bootstrap';
import { BsSearch, BsArrowClockwise } from 'react-icons/bs';

export default function CoachStopFilter({ filters, onFilterChange, onReset }) {
    return (
        <Card className="mb-4 shadow-sm border-0">
            <Card.Body className="p-4">
                <Form className="d-flex flex-wrap gap-3 align-items-center justify-content-center">

                    <Form.Control
                        type="text"
                        placeholder="Nhập điểm dừng, địa chỉ, thành phố..."
                        name="search"
                        value={filters.search}
                        onChange={(e) => onFilterChange(e.target.name, e.target.value)}
                        style={{ width: '300px' }} maxLength={100}
                    />


                    <Form.Select
                        name="isActive"
                        value={filters.isActive}
                        onChange={(e) => onFilterChange(e.target.name, e.target.value)}
                        style={{ width: '200px' }}
                    >
                        <option value="">Tất cả trạng thái</option>
                        <option value="true">Đang hoạt động</option>
                        <option value="false">Ngừng hoạt động</option>
                    </Form.Select>
                    <Button
                        variant="outline-secondary"
                        onClick={onReset}
                        size='lg'
                        className="d-flex align-items-center"
                        title='Làm mới bộ lọc'
                    >
                        <BsArrowClockwise size={18} />
                    </Button>
                </Form>
            </Card.Body>
        </Card>
    );
}
