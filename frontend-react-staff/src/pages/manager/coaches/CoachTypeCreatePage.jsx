import { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { coachTypeApi, SeatMapBuilder } from '../../../features/coaches';
import { Alert, Button, Card, Col, Container, Form, InputGroup, Row } from 'react-bootstrap';
import { formatCurrency } from '../../../utils/formatters';

export default function CoachTypeCreatePage() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        coachTypeName: '',
        seatPrice: '',
        totalFloors: 2,
        rows: 5, 
        cols: 2
    });

    const [floorData, setFloorData] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        const load = () => setFloorData(prevData => {
            const newFloorData = Array(formData.totalFloors).fill().map((_, floorIndex) => {
                const oldMatrix = prevData[floorIndex];
                
                if (oldMatrix && oldMatrix.length === formData.rows && oldMatrix[0]?.length === formData.cols) {
                    return oldMatrix;
                }
                
                return Array(formData.rows).fill().map(() => Array(formData.cols).fill("EMPTY"));
            });
            return newFloorData;
        });
        load();
    }, [formData.totalFloors, formData.rows, formData.cols]);

    const currentTotalSeats = useMemo(() => {
        let count = 0;
        floorData.forEach(matrix => {
            matrix.forEach(row => {
                row.forEach(cell => {
                    if (cell === 'SEAT') count++;
                });
            });
        })
        return count;
    }, [floorData]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;

        if (name === 'rows' || name === 'cols' || name === 'totalFloors') {
            if (value === '') {
                setFormData(prev => ({ ...prev, [name]: '' }));
                return;
            }

            let numValue = parseInt(value, 10);
            if (isNaN(numValue)) return;

            const maxLimit = name === 'totalFloors' ? 2 : name === 'rows' ? 11 : 5;
            
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

    const handleMatrixChange = (floorIndex, newMatrix) => {
        const newFloorData = [...floorData];
        newFloorData[floorIndex] = [...newMatrix];
        setFloorData(newFloorData);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMsg('');

        if (!formData.rows || !formData.cols || !formData.totalFloors) {
            setErrorMsg('Vui lòng nhập đầy đủ số tầng, số hàng và số cột!');
            return;
        }

        if (currentTotalSeats === 0) {
            setErrorMsg('Vui lòng click vào sơ đồ để chọn ít nhất 1 vị trí làm Ghế ngồi!');
            return;
        }

        setIsSubmitting(true);

        try {
            const layoutPayload = {
                totalFloors: formData.totalFloors,
                rows: formData.rows,
                cols: formData.cols,
                floors: floorData
            };

            const payload = {
                coachTypeName: formData.coachTypeName,
                seatPrice: formData.seatPrice,
                seatLayout: JSON.stringify(layoutPayload)
            };

            await coachTypeApi.createCoachType(payload);
            
            navigate('/management/coach-types');
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
                onClick={() => navigate('/management/coach-types')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18}/> Quay lại danh sách
            </Button>

            <h2 className="mb-4 text-dark fw-bold">Thêm mới loại xe & Sơ đồ cấu hình</h2>

            {errorMsg && (
                <Alert variant="danger" className="shadow-sm border-0 d-flex align-items-center gap-2">
                    <BsExclamationTriangleFill />
                    <span>{errorMsg}</span>
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
                                    <Form.Label className="fw-semibold text-secondary mb-1">Giá vé mặc định <span className="text-danger">*</span></Form.Label>
                                    
                                    <InputGroup>
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
                                        <InputGroup.Text className="bg-light fw-medium text-secondary">VNĐ</InputGroup.Text>
                                    </InputGroup>
                                    
                                    {formData.seatPrice !== '' && !isNaN(formData.seatPrice) && (
                                        <Form.Text className="text-success fw-medium mt-2 d-block">
                                            Giá trị: {formatCurrency(formData.seatPrice)}
                                        </Form.Text>
                                    )}
                                </Form.Group>

                                <Form.Group>
                                    <Form.Label className="fw-semibold text-secondary mb-1">Số tầng của loại xe <span className="text-danger">*</span></Form.Label>
                                    <Form.Control 
                                        type="number" 
                                        name="totalFloors" 
                                        required 
                                        min="1" 
                                        max="2"
                                        value={formData.totalFloors} 
                                        onChange={handleInputChange}
                                        className="py-2"
                                    />
                                </Form.Group>

                                <div className="d-flex gap-3">
                                    <Form.Group className="flex-fill">
                                        <Form.Label className="fw-semibold text-secondary mb-1">Số hàng ngang (1-11) <span className="text-danger">*</span></Form.Label>
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
                                        <Form.Label className="fw-semibold text-secondary mb-1">Số cột dọc (1-5) <span className="text-danger">*</span></Form.Label>
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
                                    disabled={isSubmitting}
                                    className="w-100 py-2 mt-3 fw-medium d-flex justify-content-center align-items-center gap-2 custom-btn-general"
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
                                    * Click chuột vào các ô vuông bên dưới để biến nó thành vị trí đặt <span className="text-success fw-bold">Ghế ngồi</span>. Ô màu xám mặc định sẽ là <span className="text-muted fw-bold">Khoang trống</span>.
                                </p>
                                
                                <div className="d-flex justify-content-center gap-2 overflow-x-auto align-items-center bg-light rounded border border-2 border-dashed p-4 min-vh-50" style={{ minHeight: '350px' }}>
                                    {floorData.map((matrix, index) => (
                                        <div key={index} className="floor-wrapper text-center">
                                            <p className="mb-3 fw-bold">Tầng {index+1}</p>
                                            <div className="border border-secondary rounded p-3 bg-light shadow-sm">
                                                <SeatMapBuilder 
                                                    mode="CREATE"
                                                    rows={formData.rows}
                                                    cols={formData.cols}
                                                    initialMatrix={matrix} 
                                                    onChange={(newMatrix) => handleMatrixChange(index, newMatrix)}
                                                />
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>

                </Row>
            </Form>

        </Container>
    );
}