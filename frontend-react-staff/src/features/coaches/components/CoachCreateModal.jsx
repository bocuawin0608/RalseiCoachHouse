import { useEffect, useState } from "react";
import { Alert, Button, Form, Modal } from "react-bootstrap";
import { BsExclamationTriangleFill } from "react-icons/bs";
import { coachApi } from "../api/coachApi";
import { useCoachTypeDropdown } from "../../../hooks/useCoachTypeDropdown";
import { useRouteDropdown } from "../../../hooks/useRouteDropdown";

import {
    COACH_VALIDATION,
    validateAndFormatLicensePlate,
} from "../../../utils/coachValidation";

const INITIAL_FORM = {
    coachTypeId: '',
    routeId: '',
    licensePlate: '',
    manufacturer: '',
    year: ''
}

export default function CoachCreateModal({isOpen, onClose, onSuccess}) {
    const [formData, setFormData] = useState(INITIAL_FORM);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const [fieldErrors, setFieldErrors] = useState({});
    const { coachTypes, loadingCoachTypes } = useCoachTypeDropdown(isOpen);
    const { routes, loadingRoutes } = useRouteDropdown(isOpen);

    const isDropdownLoading = loadingCoachTypes || loadingRoutes;

    useEffect(() => {
        const load = () => {
            if (isOpen) {
                setFormData(INITIAL_FORM);
                setError(null);
                setFieldErrors({});
            }
        }
        load();
    }, [isOpen]);

    const handleInputChange = (e) => {
        setFormData(prev => ({
            ...prev,
            [e.target.name]: e.target.value
        }));
        setError(null);
        setFieldErrors(prev => ({ ...prev, [e.target.name]: null }));
    }

    const handleLicensePlateBlur = (e) => {
        const result = validateAndFormatLicensePlate(e.target.value);
        if (result.valid) {
            setFormData(prev => ({ ...prev, licensePlate: result.data }));
        }
    };

    const validateForm = () => {
        const nextErrors = {};
        const plateResult = validateAndFormatLicensePlate(formData.licensePlate);
        if (!plateResult.valid) {
            nextErrors.licensePlate = COACH_VALIDATION.LICENSE_PLATE_MESSAGE;
        }

        if (!formData.manufacturer.trim()) {
            nextErrors.manufacturer = 'Hãng xe không được để trống.';
        }

        const year = Number(formData.year);
        if (!formData.year || Number.isNaN(year)) {
            nextErrors.year = 'Năm sản xuất không được để trống.';
        } else if (year < COACH_VALIDATION.YEAR_MIN || year > COACH_VALIDATION.getYearMax()) {
            nextErrors.year = `Năm sản xuất phải từ ${COACH_VALIDATION.YEAR_MIN} đến ${COACH_VALIDATION.getYearMax()}.`;
        }

        if (!formData.coachTypeId) {
            nextErrors.coachTypeId = 'Vui lòng chọn loại xe.';
        }

        setFieldErrors(nextErrors);

        return { isValid: Object.keys(nextErrors).length === 0, plateResult };
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const { isValid, plateResult } = validateForm();
        if (!isValid) return;

        setIsSubmitting(true);
        setError(null);

        try {
            await coachApi.createCoach({
                coachTypeId: Number(formData.coachTypeId),
                routeId: formData.routeId ? Number(formData.routeId) : null,
                licensePlate: plateResult.data,
                manufacturer: formData.manufacturer.trim(),
                year: Number(formData.year),
            });

            onSuccess();
            onClose();

        } catch (error) {
            console.log(error.response);
            setError(error.response?.data?.message || "Có lỗi xảy ra khi thêm mới xe.");
        } finally {
            setIsSubmitting(false);
        }
    }

    if(!isOpen) return null;

    return (
        <Modal show={isOpen} onHide={onClose} centered backdrop='static'>
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    Thêm xe mới
                </Modal.Title>
            </Modal.Header>

            <Modal.Body>
                <Form id="create-coach-form" onSubmit={handleSubmit}>
                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold text-secondary">
                            Loại xe <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Select
                            value={formData.coachTypeId}
                            name="coachTypeId"
                            onChange={handleInputChange}
                            required
                            disabled={isDropdownLoading}
                            isInvalid={!!fieldErrors.coachTypeId}
                            className="py-2"
                        >
                            <option value="">-- Chọn loại xe --</option>
                            {coachTypes.map(ct => (
                                <option key={ct.coachTypeId} value={ct.coachTypeId}>{ct.coachTypeName}</option>
                            ))}
                        </Form.Select>
                        <Form.Control.Feedback type="invalid">
                            {fieldErrors.coachTypeId}
                        </Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold text-secondary">
                            Biển số xe <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control 
                            type="text"
                            placeholder="Ví dụ: 30B-111.11"
                            value={formData.licensePlate}
                            name="licensePlate"
                            onChange={handleInputChange}
                            onBlur={handleLicensePlateBlur}
                            required maxLength={20}
                            isInvalid={!!fieldErrors.licensePlate}
                            className="py-2"
                        />
                        <Form.Control.Feedback type="invalid">
                            {fieldErrors.licensePlate}
                        </Form.Control.Feedback>
                    </Form.Group>

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
                                isInvalid={!!fieldErrors.manufacturer}
                                className="py-2"
                            />
                            <Form.Control.Feedback type="invalid">
                                {fieldErrors.manufacturer}
                            </Form.Control.Feedback>
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
                                min={COACH_VALIDATION.YEAR_MIN}
                                max={COACH_VALIDATION.getYearMax()}
                                isInvalid={!!fieldErrors.year}
                                className="py-2"
                            />
                            <Form.Control.Feedback type="invalid">
                                {fieldErrors.year}
                            </Form.Control.Feedback>
                        </Form.Group>
                    </div>

                    <Form.Group className="mb-4">
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
                <Button type="submit" form="create-coach-form" disabled={isSubmitting} className="px-4 custom-btn-general">
                    {isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi'}
                </Button>
            </Modal.Footer>
        </Modal>
    )
}