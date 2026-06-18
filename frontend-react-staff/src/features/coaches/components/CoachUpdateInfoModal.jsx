import { useEffect, useRef, useState } from "react";
import { useCoachTypeDropdown } from "../../../hooks/useCoachTypeDropdown";
import { useRouteDropdown } from "../../../hooks/useRouteDropdown";
import { Alert, Button, Form, Modal } from "react-bootstrap";
import { BsExclamationTriangleFill } from "react-icons/bs";
import { coachApi } from "../api/coachApi";

const INITIAL_DETAIL = {
    licensePlate: '',
    manufacturer: '',
    year: '',
    coachTypeId: '',
    routeId: '',
    status: ''
}

export default function CoachUpdateInfoModal({isOpen, data, onClose, onSuccess, statusLabels}) {
    const [formData, setFormData] = useState(INITIAL_DETAIL);
    const [loading, setLoading] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const { coachTypes, loadingCoachTypes } = useCoachTypeDropdown(isOpen);
    const { routes, loadingRoutes } = useRouteDropdown(isOpen);

    const isDropdownLoading = loadingCoachTypes || loadingRoutes;

    const originalDataRef = useRef(null);

    useEffect(() => {
        const fetchDetail = async () => {
            setLoading(true);
            setError(null);
            try {
                if(data && isOpen) {
                    const res = await coachApi.getCoachDetailForEdit(data.coachId);

                    const initData = {
                        licensePlate: res.licensePlate,
                        manufacturer: res.manufacturer,
                        year: res.year,
                        coachTypeId: res.coachTypeId,
                        routeId: res.routeId,
                        status: res.status
                    }
                    setFormData(initData);

                    originalDataRef.current = initData;
                }
            } catch(error) {
                setError(error.response?.data?.message || "Có lỗi xảy ra khi tải thông tin chi tiết.");
            } finally {
                setLoading(false);
            }
        }
        fetchDetail();
    }, [data, isOpen]);

    const handleInputChange = (e) => {
        setFormData(prev => ({
            ...prev,
            [e.target.name]: e.target.value
        }));
        setError(null);
    }

    const handleSubmit = async (e) => {
        e.preventDefault();

        const hasAnyChange = originalDataRef.current ? Object.keys(formData).some(key => String(formData[key]) !== String(originalDataRef.current[key])): false;

        if (!hasAnyChange) {
            onClose();
            return;
        }

        setIsSubmitting(true);
        setError(null);

        try {
            await coachApi.updateCoachInfo(data.coachId, formData);
            onSuccess();
            onClose();
        } catch (error) {
            setError(error.response?.data?.message || "Có lỗi xảy ra khi cập nhật thông tin xe.");
        } finally {
            setIsSubmitting(false);
        }
    }

    if(!isOpen) return null;

    return (
        <Modal show={isOpen} onHide={onClose} centered backdrop='static'>
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Cập nhật thông tin xe
                </Modal.Title>
            </Modal.Header>

            <Modal.Body>
                <Form id="create-coach-form" onSubmit={handleSubmit}>
                    <div className="d-flex gap-3">
                        <Form.Group className="mb-4 flex-fill">
                            <Form.Label className="fw-semibold text-secondary">
                                Biển số xe <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Control 
                                type="text"
                                placeholder="Ví dụ: 30B-111.11"
                                value={formData.licensePlate}
                                name="licensePlate"
                                onChange={handleInputChange}
                                required maxLength={20}
                                className="py-2"
                                />
                        </Form.Group>
                        <Form.Group className="mb-4 flex-fill">
                            <Form.Label className="fw-semibold text-secondary">
                                Trạng thái <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Select
                                value={formData.status}
                                name="status"
                                onChange={handleInputChange}
                                className="py-2"    
                            >   
                                <option value="">-- Chọn trạng thái --</option>
                                {Object.keys(statusLabels).map(statusKey => (
                                    <option key={statusKey} value={statusKey}>
                                        {statusLabels[statusKey].text}
                                    </option>
                                ))}
                            </Form.Select>
                        </Form.Group>
                    </div>
                               
                    <div className="d-flex gap-3">
                        <Form.Group className="mb-4 flex-fill">
                            <Form.Label className="fw-semibold text-secondary">
                                Hãng sản xuất <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Control 
                                type="text"
                                placeholder="Ví dụ: Thaco, Hyundai, VinFast..."
                                value={formData.manufacturer}
                                name="manufacturer"
                                onChange={handleInputChange}
                                required maxLength={100}
                                className="py-2"
                            />
                        </Form.Group>

                        <Form.Group className="mb-4">
                            <Form.Label className="fw-semibold text-secondary">
                                Năm sản xuất <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Control 
                                type="number"
                                value={formData.year}
                                name="year"
                                onChange={handleInputChange}
                                required
                                min='2000' max={new Date().getFullYear()}
                                className="py-2"
                            />
                        </Form.Group>
                    </div>
                    
                    <Form.Group className="mb-4 flex-fill">
                        <Form.Label className="fw-semibold text-secondary">
                            Loại xe <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Select
                            value={formData.coachTypeId}
                            name="coachTypeId"
                            onChange={handleInputChange}
                            required
                            disabled={isDropdownLoading}
                            className="py-2"
                        >
                            <option value="">-- Chọn loại xe --</option>
                            {coachTypes.map(ct => (
                                <option key={ct.coachTypeId} value={ct.coachTypeId}>{ct.coachTypeName}</option>
                            ))}
                        </Form.Select>
                    </Form.Group>

                    <Form.Group className="mb-4 flex-fill">
                        <Form.Label className="fw-semibold text-secondary">
                            Tuyến (tùy chọn)
                        </Form.Label>
                        <Form.Select
                            value={formData.routeId}
                            name="routeId"
                            onChange={handleInputChange}
                            disabled={isDropdownLoading}
                            className="py-2"
                        >
                            <option value="">-- Chọn tuyến xe --</option>
                            {routes.map(r => <option key={r.routeId} value={r.routeId}>{r.routeName}</option>)}
                        </Form.Select>
                    </Form.Group>

                    {error && <Alert variant='danger' className="mb-3 py-2 px-3 border-0 d-flex align-items-center gap-2">
                        <BsExclamationTriangleFill />
                        <span>{error}</span>
                    </Alert>}
                </Form>
            </Modal.Body>

            <Modal.Footer className="bg-light border-0 rounded-bottom">
                <Button variant="outline-secondary" onClick={onClose} disabled={isSubmitting} className="px-4">
                    Hủy bỏ
                </Button>
                <Button type="submit" form="create-coach-form" disabled={isSubmitting || loading} className="px-4 custom-btn-general">
                    {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
                </Button>
            </Modal.Footer>
        </Modal>
    )
}