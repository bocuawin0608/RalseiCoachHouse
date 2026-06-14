import { Container, Row, Col, Badge } from 'react-bootstrap';

export default function PublicFooter() {
    return (
        <footer style={{ backgroundColor: 'var(--ralsei-footer)' }} className="pt-5 pb-3 text-white mt-auto">
            <Container>
                <Row className="gy-4">
                    {/* Cột Thương hiệu */}
                    <Col md={6} className="d-flex flex-column align-items-start">
                        <img src="/images/ralseiiii.jpg" alt="Logo" height="60" className="rounded mb-3 bg-white p-1 shadow-sm" />
                        <h6 className="fw-bold text-uppercase mb-0">Công ty TNHH Du lịch Hola Ralsei</h6>
                    </Col>

                    {/* Cột Thông tin */}
                    <Col md={6}>
                        <Badge bg="light" text="dark" className="px-3 py-2 rounded-pill mb-3" style={{ color: 'var(--ralsei-black)' }}>
                            CÔNG TY CHỦ QUẢN
                        </Badge>
                        <ul className="list-unstyled mb-0" style={{ fontSize: '0.9rem', lineHeight: '1.8' }}>
                            <li>👤 <strong>Chịu trách nhiệm:</strong> Đoàn Ngọc Đức</li>
                            <li>📍 <strong>Địa chỉ:</strong> FPT University, Hòa Lạc, Thạch Thất, Hà Nội</li>
                        </ul>
                    </Col>
                </Row>
                
                <hr className="border-white opacity-25 my-4" />
                
                <div className="text-center opacity-75" style={{ fontSize: '0.85rem' }}>
                    © Bản quyền thuộc về <strong>CÔNG TY TNHH DU LỊCH HOLA RALSEI</strong>
                </div>
            </Container>
        </footer>
    );
}