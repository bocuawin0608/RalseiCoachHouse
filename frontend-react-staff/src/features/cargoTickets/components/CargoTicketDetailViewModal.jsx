import { useEffect, useState } from 'react';
import { Modal, Table, Spinner, Button } from 'react-bootstrap';
import { BsPencilFill } from 'react-icons/bs';
import { cargoTicketApi } from '../api/cargoTicketApi';
import { useCargoTypes } from '../../cargo/hooks/useCargoTypes';
import { formatCurrency } from '../../../utils/formatters';
import CargoTicketDetailSection from './CargoTicketDetailSection';

/** Shows current cargo detail rows; mutation is available only for pending orders. */
export default function CargoTicketDetailViewModal({ ticket, onClose, readOnly = false }) {
    const [details, setDetails] = useState([]);
    const [loadingDetails, setLoadingDetails] = useState(true);
    const { cargoTypes, setPageInfo } = useCargoTypes();

    const [isDraftMode, setIsDraftMode] = useState(false);
    const [draftDetails, setDraftDetails] = useState([]);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        setPageInfo(prev => ({ ...prev, size: 100 }));
    }, [setPageInfo]);

    const fetchDetails = async () => {
        setLoadingDetails(true);
        try {
            const response = await cargoTicketApi.getCargoTicketDetails(ticket.cargoTicketId);
            setDetails(response);
        } catch (error) {
            console.error("Failed to load details", error);
        } finally {
            setLoadingDetails(false);
        }
    };

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        if (ticket) fetchDetails();
        // The modal is remounted for each ticket; fetchDetails is intentionally local.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [ticket]);

    const handleEnableDraftMode = () => {
        setDraftDetails(structuredClone(details));
        setIsDraftMode(true);
    };

    const handleCancelDraftMode = () => {
        setIsDraftMode(false);
        setDraftDetails([]);
    };

    const handleAddDetail = () => {
        setDraftDetails(prev => {
            const newDetails = structuredClone(prev);
            newDetails.push({ cargoTypePriceId: '', description: '', quantity: 1, weightKg: 0, dimensionVol: 0 });
            return newDetails;
        });
    };

    const handleDetailChange = (index, field, value) => {
        setDraftDetails(prev => {
            const newDetails = structuredClone(prev);
            newDetails[index][field] = value;
            return newDetails;
        });
    };

    const handleRemoveDetail = (index) => {
        setDraftDetails(prev => {
            const newDetails = structuredClone(prev);
            newDetails.splice(index, 1);
            return newDetails;
        });
    };

    const handleSaveDraft = async () => {
        setSaving(true);
        try {
            const deletedIds = details
                .filter(d => !draftDetails.find(dd => dd.cargoTicketDetailId === d.cargoTicketDetailId))
                .map(d => d.cargoTicketDetailId);

            const createdItems = draftDetails.filter(d => !d.cargoTicketDetailId);
            const updatedItems = draftDetails.filter(d => d.cargoTicketDetailId);

            for (const id of deletedIds) {
                await cargoTicketApi.deleteCargoTicketDetail(id);
            }
            for (const item of createdItems) {
                await cargoTicketApi.createCargoTicketDetail(ticket.cargoTicketId, {
                    ...item,
                    cargoTypePriceId: Number(item.cargoTypePriceId),
                    quantity: Number(item.quantity),
                    weightKg: Number(item.weightKg),
                    dimensionVol: Number(item.dimensionVol)
                });
            }
            for (const item of updatedItems) {
                await cargoTicketApi.updateCargoTicketDetail(item.cargoTicketDetailId, {
                    ...item,
                    cargoTypePriceId: Number(item.cargoTypePriceId),
                    quantity: Number(item.quantity),
                    weightKg: Number(item.weightKg),
                    dimensionVol: Number(item.dimensionVol)
                });
            }

            await fetchDetails();
            setIsDraftMode(false);
        } catch (error) {
            console.error(error);
            window.alert('Lưu thay đổi thất bại. Vui lòng kiểm tra lại dữ liệu.');
        } finally {
            setSaving(false);
        }
    };

    if (!ticket) return null;

    return (
        <Modal show onHide={onClose} size="xl" centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title>Chi tiết đơn gửi hàng: {ticket.ticketCode}</Modal.Title>
            </Modal.Header>
            <Modal.Body className="p-4">
                <div className="d-flex justify-content-end mb-3 gap-2">
                    {!isDraftMode && !readOnly ? (
                        <Button variant="primary" className="fw-medium d-flex align-items-center custom-btn-general" onClick={handleEnableDraftMode}>
                            <BsPencilFill className="me-2" /> Chỉnh sửa tất cả
                        </Button>
                    ) : isDraftMode ? (
                        <>
                            <Button variant="outline-secondary" className='fw-medium' onClick={handleCancelDraftMode} disabled={saving}>Hủy bỏ</Button>
                            <Button variant="success" className='fw-medium' onClick={handleSaveDraft} disabled={saving}>
                                {saving ? <Spinner size="sm" /> : 'Lưu thay đổi'}
                            </Button>
                        </>
                    ) : null}
                </div>

                {isDraftMode ? (
                    <CargoTicketDetailSection
                        draftDetails={draftDetails}
                        onAdd={handleAddDetail}
                        onChange={handleDetailChange}
                        onRemove={handleRemoveDetail}
                    />
                ) : (
                    loadingDetails ? (
                        <div className="text-center p-5 text-secondary fw-medium">
                            <Spinner animation="border" size="sm" className="me-2" /> Đang tải chi tiết...
                        </div>
                    ) : details.length === 0 ? (
                        <div className="text-center p-5 text-muted">Không có chi tiết hàng hóa nào.</div>
                    ) : (
                        <Table responsive hover bordered className="align-middle">
                            <thead className="table-light text-secondary">
                                <tr>
                                    <th>Loại hàng</th>
                                    <th className="text-end">Số lượng</th>
                                    <th className="text-end">Trọng lượng (kg)</th>
                                    <th className="text-end">Thể tích (m3)</th>
                                    <th className="text-end">Giá (VNĐ)</th>
                                    <th>Mô tả</th>
                                </tr>
                            </thead>
                            <tbody>
                                {details.map((d) => {
                                    const cargoType = cargoTypes.find(ct => ct.cargoTypePriceId === d.cargoTypePriceId);
                                    const cargoTypeName = cargoType ? `${cargoType.cargoTypeName}` : `ID: ${d.cargoTypePriceId}`;

                                    return (
                                        <tr key={d.cargoTicketDetailId}>
                                            <td className="fw-medium">{cargoTypeName}</td>
                                            <td className="text-end">{d.quantity}</td>
                                            <td className="text-end">{d.weightKg}</td>
                                            <td className="text-end">{d.dimensionVol}</td>
                                            <td className="text-end fw-bold text-dark">{formatCurrency(d.calculatedPrice)}</td>
                                            <td className="text-muted">{d.description || '-'}</td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </Table>
                    )
                )}
            </Modal.Body>
        </Modal>
    );
}
