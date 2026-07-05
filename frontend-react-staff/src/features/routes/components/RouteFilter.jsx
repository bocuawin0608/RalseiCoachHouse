import { useState, useEffect } from 'react';
import { BsArrowClockwise } from 'react-icons/bs';
import { Button, Card, Form } from 'react-bootstrap';

export default function RouteFilter({ filters, onFilterChange, onReset }) {
    const [localSearch, setLocalSearch] = useState(filters.search || '');

    useEffect(() => {
        if (!filters.search) {
            setLocalSearch('');
        }
    }, [filters.search]);

    useEffect(() => {
        const timerId = setTimeout(() => {
            if (localSearch !== (filters.search || '')) {
                onFilterChange({ target: { name: 'search', value: localSearch } });
            }
        }, 300);
        return () => clearTimeout(timerId);
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
                        placeholder="Tìm Kiếm Tuyến Đường..."
                        value={localSearch}
                        onChange={handleSearchChange}
                        style={{ width: '300px' }} maxLength={100}
                    />

                    <Form.Select
                        name="isActive"
                        value={filters.isActive || ''}
                        onChange={onFilterChange}
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
