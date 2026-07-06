import { BsArrowClockwise } from 'react-icons/bs';
import { Button, Card, Form } from 'react-bootstrap';
import { useState, useEffect } from 'react';

export default function CargoTicketFilter({ filters, onFilterChange, onReset }) {
    const [localSearch, setLocalSearch] = useState(filters.search || '');

    useEffect(() => {
        setLocalSearch(filters.search || '');
    }, [filters.search]);

    useEffect(() => {
        const handler = setTimeout(() => {
            if (localSearch !== (filters.search || '')) {
                onFilterChange({ target: { name: 'search', value: localSearch } });
            }
        }, 300);
        return () => clearTimeout(handler);
    }, [localSearch, filters.search, onFilterChange]);

    const handleSearchChange = (e) => {
        setLocalSearch(e.target.value);
    };
    return (
        <Card className="mb-4 shadow-sm border-0">
            <Card.Body className="p-4">
                <Form className="d-flex flex-wrap gap-3 align-items-center justify-content-center">
                    <Form.Control
                        name="search"
                        value={localSearch}
                        onChange={handleSearchChange}
                        placeholder="Tìm mã vé, người gửi, người nhận..."
                        style={{ width: '340px' }}
                    />
                    <Form.Select
                        name="status"
                        value={filters.status}
                        onChange={onFilterChange}
                        style={{ width: '220px' }}
                    >
                        <option value="">Tất cả trạng thái</option>
                        <option value="RECEIVED">Đã nhận hàng</option>
                        <option value="LOADED">Đã xếp hàng</option>
                        <option value="ARRIVED">Đã đến nơi</option>
                        <option value="DELIVERED">Đã giao</option>
                        <option value="CANCELLED">Đã hủy</option>
                        <option value="REJECTED">Từ chối</option>
                        <option value="ABANDONED">Bỏ hàng</option>
                    </Form.Select>
                    <Button variant="outline-secondary" size="lg" onClick={onReset} title="Làm mới bộ lọc">
                        <BsArrowClockwise size={18} />
                    </Button>
                </Form>
            </Card.Body>
        </Card>
    );
}
