import React, { useState } from 'react';
import { Container, Button, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import VoucherForm from '../../../features/vouchers/components/VoucherForm';
import { voucherApi } from '../../../features/vouchers/api/voucherApi';

const CreateVoucherPage = () => {
    const navigate = useNavigate();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const handleSubmit = async (data) => {
        setIsSubmitting(true);
        setError(null);
        try {
            await voucherApi.create(data);
            navigate('/management/vouchers');
        } catch (err) {
            setError(err.response?.data?.message || 'Tạo voucher thất bại');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Container fluid className="py-4">
            <Button variant="outline-secondary" className="mb-3" onClick={() => navigate('/management/vouchers')}>
                &larr; Quay lại danh sách
            </Button>
            <h4 className="mb-3">Thêm mới voucher</h4>
            {error && <Alert variant="danger">{error}</Alert>}
            <VoucherForm
                isSubmitting={isSubmitting}
                onSubmit={handleSubmit}
                onBack={() => navigate('/management/vouchers')}
            />
        </Container>
    );
};

export default CreateVoucherPage;
