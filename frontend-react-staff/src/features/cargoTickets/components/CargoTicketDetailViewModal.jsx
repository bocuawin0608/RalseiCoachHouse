import { useEffect, useState } from 'react';
import { Modal, Table, Badge, Spinner } from 'react-bootstrap';
import { cargoTicketApi } from '../api/cargoTicketApi';
import { useCargoTypes } from '../../cargo/hooks/useCargoTypes';
import { formatCurrency } from '../../../utils/formatters';

export default function CargoTicketDetailViewModal({ ticket, onClose }) {
    const [details, setDetails] = useState([]);
    const [loadingDetails, setLoadingDetails] = useState(true);
    const { cargoTypes, setPageInfo } = useCargoTypes();

    useEffect(() => {
        setPageInfo(prev => ({ ...prev, size: 100 }));
    }, [setPageInfo]);

    useEffect(() => {
        if (!ticket) return;
        
        let isMounted = true;
        const fetchDetails = async () => {
            setLoadingDetails(true);
            try {
                const response = await cargoTicketApi.getCargoTicketDetails(ticket.cargoTicketId);
                if (isMounted) setDetails(response);
            } catch (error) {
                console.error("Failed to load details", error);
            } finally {
                if (isMounted) setLoadingDetails(false);
            }
        };

        fetchDetails();
        return () => { isMounted = false; };
    }, [ticket]);

    if (!ticket) return null;

    return (
        <Modal show onHide={onClose} size="xl" centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title>Chi tiết vé hàng hóa: {ticket.ticketCode}</Modal.Title>
            </Modal.Header>
            <Modal.Body className="p-4">
                {loadingDetails ? (
                    <div className="text-center p-5 text-secondary fw-medium">
                        <Spinner animation="border" size="sm" className="me-2" />
                        Đang tải chi tiết hàng hóa...
                    </div>
                ) : details.length === 0 ? (
                    <div className="text-center p-5 text-muted">Không có chi tiết hàng hóa nào.</div>
                ) : (
                    <Table responsive hover bordered className="align-middle">
                        <thead className="table-light text-secondary">
                            <tr>
                                <th className="py-3 px-3">Loại hàng</th>
                                <th className="py-3 px-3 text-end">Số lượng</th>
                                <th className="py-3 px-3 text-end">Trọng lượng (kg)</th>
                                <th className="py-3 px-3 text-end">Thể tích (m3)</th>
                                <th className="py-3 px-3 text-end">Giá tính (VNĐ)</th>
                                <th className="py-3 px-3">Mô tả</th>
                            </tr>
                        </thead>
                        <tbody>
                            {details.map((d) => {
                                const cargoType = cargoTypes.find(ct => ct.cargoTypePriceId === d.cargoTypePriceId);
                                const cargoTypeName = cargoType ? `${cargoType.cargoTypeName} (${formatCurrency(cargoType.pricePerUnit)} đ/${cargoType.unit})` : `ID: ${d.cargoTypePriceId}`;
                                
                                return (
                                    <tr key={d.cargoTicketDetailId}>
                                        <td className="px-3 fw-medium">{cargoTypeName}</td>
                                        <td className="px-3 text-end">{d.quantity}</td>
                                        <td className="px-3 text-end">{d.weightKg}</td>
                                        <td className="px-3 text-end">{d.dimensionVol}</td>
                                        <td className="px-3 text-end fw-bold text-dark">{formatCurrency(d.calculatedPrice)}</td>
                                        <td className="px-3 text-muted">{d.description || '-'}</td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </Table>
                )}
            </Modal.Body>
        </Modal>
    );
}
