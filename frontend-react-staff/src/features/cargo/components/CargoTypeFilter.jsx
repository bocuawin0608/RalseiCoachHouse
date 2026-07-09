import { BsArrowClockwise } from 'react-icons/bs';
import { Button, Card, Form } from 'react-bootstrap';
import '../styles/CargoTypeManagement.css';

/**
 * Filter controls for the cargo type management table.
 */
export default function CargoTypeFilter({ filters, onFilterChange, onReset }) {
    return (
        <Card className="mb-4 cargo-type-management-filter">
            <Card.Body className="p-4">
                <Form className="cargo-type-management-filter__form">

                    <Form.Control
                        name="search"
                        placeholder="Tìm kiếm loại hàng..."
                        value={filters.search || ''}
                        onChange={onFilterChange}
                        className="cargo-type-management-filter__search"
                        maxLength={100}
                    />

                    <Form.Select
                        name="isActive"
                        value={filters.isActive || ''}
                        onChange={onFilterChange}
                        className="cargo-type-management-filter__status"
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
