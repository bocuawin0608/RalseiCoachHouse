import { BsArrowClockwise } from 'react-icons/bs';
import { Badge, Button, Card, Dropdown, Form } from 'react-bootstrap';

const statusLabels = {
        ACTIVE: { text: 'Đang hoạt động', bg: 'success' },
        MAINTENANCE: { text: 'Đang bảo trì', bg: 'warning' },
        RETIRED: { text: 'Ngừng hoạt động', bg: 'danger' }
    };

export default function CoachFilter({filters, onFilterChange, onReset, onCheckboxChange}) {
    return (
        <Card className="mb-4 shadow-sm border-0">
            <Card.Body className="p-4">
                <Form className="d-flex flex-wrap gap-3 align-items-center justify-content-center">
                    
                    <Form.Control 
                        name="licensePlate" 
                        placeholder="Mã biển số xe..." 
                        value={filters.licensePlate || ''}
                        onChange={onFilterChange}
                        style={{ width: '150px' }} maxLength={20}
                    />

                    <Form.Control 
                        name="routeName" 
                        placeholder="Tuyến xe..." 
                        value={filters.routeName || ''}
                        onChange={onFilterChange}
                        style={{ width: '150px' }} maxLength={255}
                    />

                        <Form.Select
                            style={{ width: '150px' }}
                            name="coachTypeId"
                            value={filters.coachTypeId || ''}
                            onChange={onFilterChange}
                        >
                            <option value="">Tất cả loại xe</option>
                            {/* {coachTypes?.map(type => (
                                <option key={type.coachTypeId} value={type.coachTypeId}>
                                    {type.coachTypeName} ({type.totalSeat} ghế)
                                </option>
                            ))} */}
                        </Form.Select>
                    
                    <Form.Group>
                        <Dropdown style={{ width: '180px' }}>
                            <Dropdown.Toggle 
                                variant="outline-md-secondary" 
                                className="w-100 text-start d-flex justify-content-between align-items-center border"
                                style={{ backgroundColor: '#fff', color: '#495057' }}
                            >
                                <span className="text-truncate">
                                    {filters.statuses.length === 0 
                                        ? 'Trạng thái xe' 
                                        : `Đã chọn (${filters.statuses.length})`}
                                </span>
                            </Dropdown.Toggle>

                            {/* dùng stopPropagation để click chọn nhiều box mà không bị đóng menu */}
                            <Dropdown.Menu className="p-3 w-100 shadow-sm" onClick={(e) => e.stopPropagation()}>
                                {Object.keys(statusLabels).map((statusKey) => (
                                    <Form.Check
                                        key={statusKey}
                                        type="checkbox"
                                        id={`check-status-${statusKey}`}
                                        label={
                                            <Badge bg={statusLabels[statusKey].bg} className="ms-1">
                                                {statusLabels[statusKey].text}
                                            </Badge>
                                        }
                                        name="statuses"
                                        value={statusKey}
                                        checked={filters.statuses.includes(statusKey)}
                                        onChange={onCheckboxChange}
                                        className="mb-2"
                                    />
                                ))}
                            </Dropdown.Menu>
                        </Dropdown>
                    </Form.Group>

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
    )
}