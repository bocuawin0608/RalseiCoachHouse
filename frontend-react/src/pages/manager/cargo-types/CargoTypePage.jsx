import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
    useCargoTypes, 
    CargoTypeTable, 
    CargoTypeFilter,
    CargoTypeUpdateInfoModal
} from '../../../features/cargo';
import Pagination from '../../../components/common/Pagination';
import { Alert, Button, Card, Container } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';

export default function CargoTypePage() {
    const navigate = useNavigate();
    
    const { 
        cargoTypes, loading, pageInfo, setPageInfo, refetch,
        filters, handleFilterChange, handleReset, error
    } = useCargoTypes();
    
    const [modalState, setModalState] = useState({ type: null, data: null });
    const closeModal = () => setModalState({ type: null, data: null });

    return (
        <Container fluid className="py-4" style={{ maxWidth: '1200px' }}>
            
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="m-0 fw-bold text-dark">Quản lý các loại hàng</h2>
                <Button 
                    variant="primary" 
                    className="fw-medium shadow-sm"
                    onClick={() => navigate('/manager/cargo-types/create')}
                >
                    + Thêm loại hàng mới
                </Button>
            </div>

            <CargoTypeFilter 
                filters={filters} 
                onFilterChange={handleFilterChange}
                onReset={handleReset}
            />

            {error && (
                <Alert variant="danger" className="shadow-sm border-0 d-flex align-items-center gap-2">
                    <BsExclamationTriangleFill />
                    <span>{error}</span>
                </Alert>
            )}

            <Card className="shadow-sm border-0">
                <Card.Body className="p-0"> 
                    <CargoTypeTable 
                        data={cargoTypes} 
                        loading={loading}
                        onEditInfo={(row) => setModalState({ type: 'EDIT_INFO', data: row })}
                    />
                    
                    <div className="d-flex justify-content-center py-4 bg-white border-top">
                        <Pagination pageInfo={pageInfo} onPageChange={setPageInfo} />
                    </div>
                </Card.Body>
            </Card>

            <CargoTypeUpdateInfoModal 
                isOpen={modalState.type === 'EDIT_INFO'} 
                data={modalState.data} 
                onClose={closeModal} 
                onSuccess={refetch} 
            />

        </Container>
    );
}
