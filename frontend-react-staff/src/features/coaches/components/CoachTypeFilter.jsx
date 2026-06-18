import { BsArrowClockwise } from 'react-icons/bs';
import { Button, Card, Form, InputGroup } from 'react-bootstrap';

export default function CoachTypeFilter({ filters, onFilterChange, onReset }) {
    return (
        <Card className="mb-4 shadow-sm border-0">
            <Card.Body className="p-4">
                <Form className="d-flex flex-wrap gap-3 align-items-center justify-content-center">
                    
                    <Form.Control 
                        name="coachTypeName" 
                        placeholder="Tên loại xe..." 
                        value={filters.coachTypeName || ''}
                        onChange={onFilterChange}
                        style={{ width: '200px' }} maxLength={100}
                    />
                    
                    <InputGroup style={{ width: '280px' }}>
                        <Form.Control 
                            type="number"
                            name="minPrice" 
                            placeholder="Giá từ..." 
                            value={filters.minPrice || ''}
                            onChange={onFilterChange} 
                            step={1000} min={0} max={100000000}
                        />
                        <InputGroup.Text className="bg-light text-muted">-</InputGroup.Text>
                        <Form.Control 
                            type="number"
                            name="maxPrice" 
                            placeholder="Đến giá..." 
                            value={filters.maxPrice || ''}
                            onChange={onFilterChange} 
                            step={1000} min={0} max={100000000}
                        />
                    </InputGroup>

                    <InputGroup style={{ width: '250px' }}>
                        <Form.Control 
                            type="number"
                            name="minSeats" 
                            placeholder="Số ghế từ..." 
                            value={filters.minSeats || ''}
                            onChange={onFilterChange} min={0} max={200}
                        />
                        <InputGroup.Text className="bg-light text-muted">-</InputGroup.Text>
                        <Form.Control 
                            type="number"
                            name="maxSeats" 
                            placeholder="Đến ghế..." 
                            value={filters.maxSeats || ''}
                            onChange={onFilterChange} min={0} max={200}
                        />
                    </InputGroup>

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
                        <BsArrowClockwise size={18}/>
                    </Button>
                    
                </Form>
            </Card.Body>
        </Card>
    );
}