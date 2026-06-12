import React, { useState, useEffect } from 'react';
import { Container, Button, Alert, Spinner } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router-dom';
import VoucherForm from '../../../features/vouchers/components/VoucherForm';
import { voucherApi } from '../../../features/vouchers/api/voucherApi';

const EditVoucherPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [voucher, setVoucher] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [hasReferences, setHasReferences] = useState(false);

    useEffect(() => {
        const fetchVoucher = async () => {
            setLoading(true);
            setError(null);
            try {
                const response = await voucherApi.getById(id);
                setVoucher(response);
            } catch (err) {
                setError(err.response?.data?.message || 'Không thể tải thông tin voucher');
            } finally {
                setLoading(false);
            }
        };
        if (id) fetchVoucher();
    }, [id]);

    const handleSubmit = async (data) => {
        setIsSubmitting(true);
        setError(null);
        try {
            await voucherApi.update(id, data);
            navigate('/management/vouchers');
        } catch (err) {
            setError(err.response?.data?.message || 'Cập nhật voucher thất bại');
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) {
        return (
            <Container fluid className="py-4 text-center">
                <Spinner animation="border" />
            </Container>
        );
    }

    if (error && !voucher) {
        return (
            <Container fluid className="py-4">
                <Button variant="outline-secondary" className="mb-3" onClick={() => navigate('/management/vouchers')}>
                    &larr; Quay lại danh sách
                </Button>
                <Alert variant="danger">{error}</Alert>
            </Container>
        );
    }

    return (
        <Container fluid className="py-4">
            <Button variant="outline-secondary" className="mb-3" onClick={() => navigate('/management/vouchers')}>
                &larr; Quay lại danh sách
            </Button>
            <h4 className="mb-3">Chỉnh sửa voucher</h4>
            {error && <Alert variant="danger">{error}</Alert>}
            <VoucherForm
                initialData={voucher}
                isSubmitting={isSubmitting}
                hasReferences={hasReferences}
                onSubmit={handleSubmit}
                onBack={() => navigate('/management/vouchers')}
            />
        </Container>
    );
};

export default EditVoucherPage;
