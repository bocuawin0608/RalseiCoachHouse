import { useEffect, useState } from 'react';
import { Modal, Spinner, Button } from 'react-bootstrap';
import { BsPencilFill, BsClipboard, BsCheck, BsPrinter } from 'react-icons/bs';
import { cargoTicketApi } from '../api/cargoTicketApi';
import { useCargoTypes } from '../../cargo/hooks/useCargoTypes';
import { formatCurrency } from '../../../utils/formatters';
import { printCargoTicket } from '../utils/printCargoTicket';
import CargoTicketDetailSection from './CargoTicketDetailSection';

/** Shows current cargo detail rows; mutation is available only for pending orders. */
export default function CargoTicketDetailViewModal({
    ticket,
    onClose,
    readOnly = false,
    canPrint = false
}) {
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

    const [copied, setCopied] = useState(false);

    const handleCopy = () => {
        navigator.clipboard.writeText(ticket.ticketCode);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    const activeDetails = isDraftMode ? draftDetails : details;
    const totalQuantity = activeDetails.reduce((sum, d) => sum + (Number(d.quantity) || 0), 0);
    const totalWeightKg = activeDetails.reduce((sum, d) => sum + (Number(d.weightKg) || 0), 0);
    const totalDimensionVol = activeDetails.reduce((sum, d) => sum + (Number(d.dimensionVol) || 0), 0);
    const totalPrice = activeDetails.reduce((sum, d) => sum + (Number(d.calculatedPrice) || 0), 0);

    if (!ticket) return null;

    return (
        <Modal show onHide={onClose} size="xl" centered backdrop="static" scrollable>
            <Modal.Header>
                <div>
                    <Modal.Title className="fw-bold">Chi tiết đơn gửi hàng</Modal.Title>
                    <div className="d-flex align-items-center gap-2 mt-1">
                        <span className="text-primary fw-bold fs-16">{ticket.ticketCode}</span>
                        <Button
                            variant="light"
                            size="sm"
                            className="d-flex align-items-center justify-content-center p-1 text-muted shadow-sm"
                            onClick={handleCopy}
                            title="Sao chép mã đơn"
                            style={{ width: '28px', height: '28px', borderRadius: '6px' }}
                        >
                            {copied ? <BsCheck size={18} className="text-success" /> : <BsClipboard size={16} />}
                        </Button>
                    </div>
                </div>
            </Modal.Header>
            <Modal.Body className="p-4">

                {isDraftMode ? (
                    <form id="draft-form" onSubmit={(e) => { e.preventDefault(); handleSaveDraft(); }}>
                        <CargoTicketDetailSection
                            draftDetails={draftDetails}
                            onAdd={handleAddDetail}
                            onChange={handleDetailChange}
                            onRemove={handleRemoveDetail}
                        />
                    </form>
                ) : (
                    loadingDetails ? (
                        <div className="text-center p-5 text-secondary fw-medium">
                            <Spinner animation="border" size="sm" className="me-2" /> Đang tải chi tiết...
                        </div>
                    ) : details.length === 0 ? (
                        <div className="text-center p-5 text-muted">Không có chi tiết hàng hóa nào.</div>
                    ) : (
                        <div className="d-flex flex-column" style={{ gap: '12px' }}>
                            {details.map((d, index) => {
                                const cargoType = cargoTypes.find(ct => ct.cargoTypePriceId === d.cargoTypePriceId);
                                const cargoTypeName = cargoType ? `${cargoType.cargoTypeName}` : `ID: ${d.cargoTypePriceId}`;

                                return (
                                    <div key={d.cargoTicketDetailId} className="bg-light" style={{ borderRadius: '12px', padding: '1rem' }}>
                                        <div className="d-flex justify-content-between align-items-center mb-3">
                                            <span className="fw-bold">Hàng hóa #{index + 1}</span>
                                        </div>
                                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '16px', marginBottom: '16px' }}>
                                            <div>
                                                <div className="small text-muted fw-semibold mb-1">Loại hàng</div>
                                                <div className="fw-medium text-dark bg-white border border-light" style={{ padding: '0.375rem 0.75rem', borderRadius: '0.375rem', minHeight: '38px', display: 'flex', alignItems: 'center' }}>
                                                    {cargoTypeName}
                                                </div>
                                            </div>
                                            <div>
                                                <div className="small text-muted fw-semibold mb-1">Số lượng</div>
                                                <div className="fw-medium text-dark bg-white border border-light" style={{ padding: '0.375rem 0.75rem', borderRadius: '0.375rem', minHeight: '38px', display: 'flex', alignItems: 'center' }}>
                                                    {d.quantity}
                                                </div>
                                            </div>
                                            <div>
                                                <div className="small text-muted fw-semibold mb-1">Trọng lượng</div>
                                                <div className="fw-medium text-dark bg-white border border-light position-relative" style={{ padding: '0.375rem 0.75rem', borderRadius: '0.375rem', minHeight: '38px', display: 'flex', alignItems: 'center' }}>
                                                    {d.weightKg}
                                                    <span className="position-absolute end-0 top-50 translate-middle-y me-3 text-muted small">kg</span>
                                                </div>
                                            </div>
                                            <div>
                                                <div className="small text-muted fw-semibold mb-1">Thể tích</div>
                                                <div className="fw-medium text-dark bg-white border border-light position-relative" style={{ padding: '0.375rem 0.75rem', borderRadius: '0.375rem', minHeight: '38px', display: 'flex', alignItems: 'center' }}>
                                                    {d.dimensionVol}
                                                    <span className="position-absolute end-0 top-50 translate-middle-y me-3 text-muted small">m³</span>
                                                </div>
                                            </div>
                                            <div>
                                                <div className="small text-muted fw-semibold mb-1">Giá</div>
                                                <div className="fw-bold text-dark bg-white border border-light position-relative" style={{ padding: '0.375rem 0.75rem', borderRadius: '0.375rem', minHeight: '38px', display: 'flex', alignItems: 'center' }}>
                                                    {formatCurrency(d.calculatedPrice)}
                                                    <span className="position-absolute end-0 top-50 translate-middle-y me-3 text-muted small"></span>
                                                </div>
                                            </div>
                                        </div>
                                        {d.description && (
                                            <div>
                                                <div className="small text-muted fw-semibold mb-1">Mô tả</div>
                                                <div className="fw-medium text-dark bg-white border border-light" style={{ padding: '0.375rem 0.75rem', borderRadius: '0.375rem', minHeight: '38px' }}>
                                                    {d.description}
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    )
                )}

                {!loadingDetails && activeDetails.length > 0 && (
                    <div className="mt-3 d-flex justify-content-end align-items-center gap-3 small" style={{ padding: '0.5rem 1rem' }}>
                        <span className="fw-bold text-dark">Tổng: {totalQuantity} kiện</span>
                        <span className="text-muted fw-medium">{Number(totalWeightKg.toFixed(2))} kg</span>
                        <span className="text-muted fw-medium">{Number(totalDimensionVol.toFixed(2))} m³</span>
                        <span className="fw-bold text-success">{formatCurrency(totalPrice)}</span>
                    </div>
                )}
            </Modal.Body>
            <Modal.Footer>
                {canPrint && (
                    <Button
                        variant="outline-secondary"
                        className="fw-medium px-3 me-auto"
                        disabled={saving}
                        onClick={() => printCargoTicket(ticket, {
                            pieceCount: totalQuantity > 0 ? totalQuantity : null
                        })}
                    >
                        <BsPrinter className="me-2" />
                        In tem
                    </Button>
                )}
                <Button variant="outline-secondary" className="fw-medium px-4" onClick={onClose} disabled={saving}>
                    Đóng
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
