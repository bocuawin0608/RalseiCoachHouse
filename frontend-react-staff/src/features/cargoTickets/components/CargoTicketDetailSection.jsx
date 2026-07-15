import { Button, Form } from 'react-bootstrap';
import { useCargoTypes } from '../../cargo/hooks/useCargoTypes';
import { useEffect, useState } from 'react';
import { BsTrash, BsPlus } from 'react-icons/bs';
import { cargoTicketApi } from '../api/cargoTicketApi';

function Field({ label, required, suffix, ...props }) {
    return (
        <Form.Group>
            <Form.Label className="small text-muted fw-semibold mb-1">
                {label}{required && <span className="text-danger ms-1">*</span>}
            </Form.Label>
            {suffix ? (
                <div className="position-relative">
                    <Form.Control {...props} required={required} aria-required={required} style={{ paddingRight: '40px' }} />
                    <span className="position-absolute end-0 top-50 translate-middle-y me-3 text-muted small" style={{ pointerEvents: 'none' }}>
                        {suffix}
                    </span>
                </div>
            ) : (
                <Form.Control {...props} required={required} aria-required={required} />
            )}
        </Form.Group>
    );
}

function DetailItem({ detail, index, cargoTypes, armedDeleteIndex, setArmedDeleteIndex, onChange, onRemove }) {
    const isArmed = armedDeleteIndex === index;

    const handleDeleteClick = () => {
        if (isArmed) {
            onRemove(index);
            setArmedDeleteIndex(null);
        } else {
            setArmedDeleteIndex(index);
        }
    };

    useEffect(() => {
        if (!detail.cargoTypePriceId || detail.dimensionVol === undefined || detail.dimensionVol === '' || !detail.quantity) {
            if (detail.calculatedPrice !== undefined) {
                onChange(index, 'calculatedPrice', undefined);
            }
            return;
        }

        const timeoutId = setTimeout(() => {
            cargoTicketApi.calculatePrice({
                cargoTypePriceId: Number(detail.cargoTypePriceId),
                dimensionVol: Number(detail.dimensionVol),
                quantity: Number(detail.quantity)
            }).then(res => {
                if (detail.calculatedPrice !== res.calculatedPrice) {
                    onChange(index, 'calculatedPrice', res.calculatedPrice);
                }
            }).catch(console.error);
        }, 300);

        return () => clearTimeout(timeoutId);
    }, [detail.cargoTypePriceId, detail.dimensionVol, detail.quantity, index, onChange]);

    return (
        <div className="bg-light" style={{ borderRadius: '12px', padding: '1rem' }}>
            <div className="d-flex justify-content-between align-items-center mb-3">
                <span className="fw-bold">Hàng hóa #{index + 1}</span>
                <Button
                    variant={isArmed ? 'danger' : 'outline-secondary'}
                    size="sm"
                    className="d-flex align-items-center justify-content-center p-0"
                    style={{ width: '32px', height: '32px' }}
                    onClick={handleDeleteClick}
                    aria-label={isArmed ? "Xác nhận xóa hàng hóa này" : "Xóa hàng hóa này"}
                >
                    <BsTrash size={16} />
                </Button>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '16px', marginBottom: '16px' }}>
                <Form.Group>
                    <Form.Label className="small text-muted fw-semibold mb-1">
                        Loại hàng <span className="text-danger ms-1">*</span>
                    </Form.Label>
                    <Form.Select
                        value={detail.cargoTypePriceId || ''}
                        onChange={(e) => onChange(index, 'cargoTypePriceId', e.target.value)}
                        required
                        aria-required="true"
                        className={!detail.cargoTypePriceId ? 'text-muted' : ''}
                    >
                        <option value="" disabled>-- Chọn loại hàng --</option>
                        {cargoTypes.map(ct => ct.cargoTypePriceId ? (
                            <option key={ct.cargoTypePriceId} value={ct.cargoTypePriceId} className="text-dark">
                                {ct.cargoTypeName} - {Number(ct.pricePerUnit).toLocaleString('vi-VN')} đ/{ct.unit}
                            </option>
                        ) : null)}
                    </Form.Select>
                </Form.Group>

                <Field
                    label="Số lượng"
                    type="number"
                    required
                    min="1"
                    placeholder="0"
                    value={detail.quantity ?? ''}
                    onChange={(e) => onChange(index, 'quantity', e.target.value)}
                />

                <Field
                    label="Trọng lượng"
                    type="number"
                    required
                    min="0.01"
                    step="any"
                    placeholder="0"
                    suffix="kg"
                    value={detail.weightKg ?? ''}
                    onChange={(e) => onChange(index, 'weightKg', e.target.value)}
                />

                <Field
                    label="Thể tích"
                    type="number"
                    required
                    min="0.01"
                    step="any"
                    placeholder="0"
                    suffix="m³"
                    value={detail.dimensionVol ?? ''}
                    onChange={(e) => onChange(index, 'dimensionVol', e.target.value)}
                />

                <Field
                    label="Giá"
                    type="text"
                    disabled
                    suffix="đ"
                    value={detail.calculatedPrice ? Number(detail.calculatedPrice).toLocaleString('vi-VN') : '0'}
                />
            </div>

            <Field
                label="Mô tả"
                as="textarea"
                rows={2}
                placeholder="Ghi chú thêm về hàng hóa này"
                value={detail.description || ''}
                onChange={(e) => onChange(index, 'description', e.target.value)}
            />
        </div>
    );
}

export default function CargoTicketDetailSection({ draftDetails, onAdd, onChange, onRemove }) {
    const { cargoTypes, setPageInfo } = useCargoTypes();
    const [armedDeleteIndex, setArmedDeleteIndex] = useState(null);

    useEffect(() => {
        setPageInfo(prev => ({ ...prev, size: 100 }));
    }, [setPageInfo]);

    return (
        <div className="mb-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h5 className="fw-bold mb-0">Chi tiết hàng hóa đính kèm</h5>
                <Button className="d-flex align-items-center gap-2 custom-btn-general fw-medium" size="sm" onClick={onAdd}>
                    <BsPlus size={18} /> Đính kèm thêm hàng hóa
                </Button>
            </div>

            {draftDetails.length === 0 ? (
                <p className="text-muted mb-0">Chưa có chi tiết hàng hóa.</p>
            ) : (
                <div className="d-flex flex-column" style={{ gap: '12px' }}>
                    {draftDetails.map((detail, index) => (
                        <DetailItem
                            key={index}
                            detail={detail}
                            index={index}
                            cargoTypes={cargoTypes}
                            armedDeleteIndex={armedDeleteIndex}
                            setArmedDeleteIndex={setArmedDeleteIndex}
                            onChange={onChange}
                            onRemove={onRemove}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}
