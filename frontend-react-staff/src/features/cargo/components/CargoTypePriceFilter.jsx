import { BsArrowClockwise } from 'react-icons/bs';
import { Button, Card, Form } from 'react-bootstrap';

export default function CargoTypePriceFilter({ filters, onFilterChange, onReset }) {
    return (
        <Card className="mb-4 shadow-sm border-0">
            <Card.Body className="p-4">
                <Form className="d-flex flex-wrap gap-3 align-items-center justify-content-center">
                    
                    <Form.Control 
                        name="search" 
                        placeholder="Tìm kiếm tên loại hàng..." 
                        value={filters.search || ''}
                        onChange={(e) => onFilterChange('search', e.target.value)}
                        style={{ width: '300px' }} maxLength={100}
                    />

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
