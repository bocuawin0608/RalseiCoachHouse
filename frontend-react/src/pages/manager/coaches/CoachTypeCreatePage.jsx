import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { BsArrowLeft, BsCheckCircle } from 'react-icons/bs';
import { coachTypeApi, SeatMapBuilder } from '../../../features/coaches';
import { Alert, Button, Card, Col, Container, Form, Row } from 'react-bootstrap';

export default function CoachTypeCreatePage() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        coachTypeName: '',
        seatPrice: '',
        rows: 5, 
        cols: 2
    });

    const [matrix, setMatrix] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    const currentTotalSeats = useMemo(() => {
        let count = 0;
        matrix.forEach(row => {
            row.forEach(cell => {
                if (cell === 'seat') count++;
            });
        });
        return count;
    }, [matrix]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;

        if (name === 'rows' || name === 'cols') {
            if (value === '') {
                setFormData(prev => ({ ...prev, [name]: '' }));
                return;
            }

            let numValue = parseInt(value, 10);
            if (isNaN(numValue)) return;

            const maxLimit = name === 'rows' ? 11 : 5;
            
            if (numValue < 1) numValue = 1;
            if (numValue > maxLimit) numValue = maxLimit;

            setFormData(prev => ({ ...prev, [name]: numValue }));
            return;
        }

        setFormData(prev => ({
            ...prev,
            [name]: name === 'coachTypeName' ? value : Number(value)
        }));
    };

    const handleMatrixChange = (newMatrix) => {
        setMatrix(newMatrix);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        if (!formData.rows || !formData.cols) {
            setErrorMsg('Vui lòng nhập đầy đủ số hàng và số cột!');
            return;
        }

        if (currentTotalSeats === 0) {
            setErrorMsg('Vui lòng click vào sơ đồ để chọn ít nhất 1 vị trí làm Ghế ngồi!');
            return;
        }

        setIsSubmitting(true);

        try {
            const layoutPayload = {
                rows: formData.rows,
                cols: formData.cols,
                matrix: matrix
            };

            const payload = {
                coachTypeName: formData.coachTypeName,
                seatPrice: formData.seatPrice,
                seatLayout: JSON.stringify(layoutPayload)
            };

            await coachTypeApi.createCoachType(payload);
            
            navigate('/manager/coach-types');
        } catch (error) {
            console.error("Lỗi tạo loại xe:", error);
            setErrorMsg(error.response?.data?.message || 'Có lỗi xảy ra khi lưu vào hệ thống.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            
            <Button 
                variant="link" 
                onClick={() => navigate('/manager/coach-types')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18}/> Quay lại danh sách
            </Button>

            <h2 className="mb-4 text-dark fw-bold">Thêm mới loại xe & Sơ đồ cấu hình</h2>

            {errorMsg && (
                <Alert variant="danger" className="fw-medium">
                    ⚠️ {errorMsg}
                </Alert>
            )}

            <Form onSubmit={handleSubmit}>
                <Row className="g-4">
                    
                    <Col lg={4} md={12}>
                        <Card className="shadow-sm border-0 h-100">
                            <Card.Header className="bg-white border-bottom pt-3 pb-2">
                                <h5 className="fw-bold mb-0 text-dark">Thông tin cơ bản</h5>
                            </Card.Header>
                            <Card.Body className="d-flex flex-column gap-3">
                                
                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Tên loại xe <span className="text-danger">*</span></Form.Label>
                                    <Form.Control 
                                        type="text" 
                                        name="coachTypeName" 
                                        required 
                                        maxLength={100}
                                        placeholder="Ví dụ: Limousine 22 Phòng VIP"
                                        value={formData.coachTypeName} 
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>

                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Giá vé mặc định (VNĐ) <span className="text-danger">*</span></Form.Label>
                                    <Form.Control 
                                        type="number" 
                                        name="seatPrice" 
                                        required 
                                        min="0" max={100000000}
                                        placeholder="Ví dụ: 350000"
                                        value={formData.seatPrice} 
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>

                                <div className="d-flex gap-3">
                                    <Form.Group className="flex-fill">
                                        <Form.Label className="fw-semibold text-secondary mb-1">Số hàng dọc (1-11) <span className="text-danger">*</span></Form.Label>
                                        <Form.Control 
                                            type="number" 
                                            name="rows" 
                                            required 
                                            min="1" 
                                            max="11"
                                            value={formData.rows} 
                                            onChange={handleInputChange}
                                            className="py-2"
                                        />
                                    </Form.Group>
                                    <Form.Group className="flex-fill">
                                        <Form.Label className="fw-semibold text-secondary mb-1">Số cột ngang (1-5) <span className="text-danger">*</span></Form.Label>
                                        <Form.Control 
                                            type="number" 
                                            name="cols" 
                                            required 
                                            min="1" 
                                            max="5"
                                            value={formData.cols} 
                                            onChange={handleInputChange}
                                            className="py-2"
                                        />
                                    </Form.Group>
                                </div>

                                <div className="bg-light p-3 rounded mt-2 border">
                                    <span className="fw-medium text-dark">Tổng số ghế thiết lập: </span>
                                    <span className="text-success fs-5 fw-bold mx-1">{currentTotalSeats}</span> 
                                    <span className="fw-medium text-dark">ghế</span>
                                </div>

                                <Button 
                                    type="submit" 
                                    variant="primary" 
                                    disabled={isSubmitting}
                                    className="w-100 py-2 mt-3 fw-medium d-flex justify-content-center align-items-center gap-2"
                                >
                                    <BsCheckCircle size={18}/> 
                                    {isSubmitting ? 'Đang lưu hệ thống...' : 'Lưu & Kích hoạt'}
                                </Button>

                            </Card.Body>
                        </Card>
                    </Col>

                    <Col lg={8} md={12}>
                        <Card className="shadow-sm border-0 h-100">
                            <Card.Body className="p-4">
                                <h4 className="fw-bold mb-2">Thiết kế sơ đồ mặt bằng xe</h4>
                                <p className="text-secondary small mb-4">
                                    * Click chuột vào các ô vuông bên dưới để biến nó thành vị trí đặt <span className="text-success fw-bold">Ghế ngồi</span>. Ô màu xám mặc định sẽ là <span className="text-muted fw-bold">Lối đi / Khoang trống</span>.
                                </p>
                                
                                <div className="d-flex justify-content-center align-items-center bg-light rounded border border-2 border-dashed p-4 min-vh-50" style={{ minHeight: '350px' }}>
                                    <SeatMapBuilder 
                                        mode="CREATE"
                                        rows={formData.rows}
                                        cols={formData.cols}
                                        onChange={handleMatrixChange}
                                    />
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>

                </Row>
            </Form>

        </Container>
    );
}