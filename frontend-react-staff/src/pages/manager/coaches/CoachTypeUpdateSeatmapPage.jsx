import { useState, useMemo, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { BsArrowLeft, BsCheckCircle, BsExclamationTriangleFill } from 'react-icons/bs';
import { coachTypeApi, SeatMapBuilder } from '../../../features/coaches'; 
import { Alert, Button, Card, Col, Container, Form, Row, Spinner } from 'react-bootstrap';

export default function CoachTypeUpdateSeatmapPage() {
    const navigate = useNavigate();
    const { id } = useParams();

    

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>

            <Button 
                variant="link" 
                onClick={() => navigate('/manager/coach-types')}
                className="text-decoration-none text-secondary p-0 mb-3 d-flex align-items-center gap-2 fw-medium"
            >
                <BsArrowLeft size={18}/> Quay lại danh sách
            </Button>

            <h2 className="mb-4 text-dark fw-bold">Chỉnh sửa sơ đồ ghế</h2>
            
            <Alert variant='warning' className='d-flex align-items-center gap-2'>
                <BsExclamationTriangleFill/>
                <span>Work in Progress!</span>
            </Alert>

        </Container>
    );
}