import { useEffect, useState } from "react";
import { Alert, Button, Form, Modal } from "react-bootstrap";
import { BsExclamationTriangleFill } from "react-icons/bs";
import { coachTypeApi } from "../api/coachTypeApi";
import { routeApi } from "../../routes";
import { coachApi } from "../api/coachApi";

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
    const [coachTypes, setCoachTypes] = useState([]);
    const [routes, setRoutes] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if(!isOpen) return;

        const fetchDropdownData = async () => {
            setLoading(true);
            setError(null);

            try {
                const [coachTypesRes, routesRes] = await Promise.all([
                    coachTypeApi.getCoachTypesDropdown(),
                    routeApi.getRoutesForDropdown()
                ]);
                setCoachTypes(coachTypesRes || []);
                setRoutes(routesRes || []);
            } catch (error) {
                setError(error.response?.data?.message || "Có lỗi xảy ra khi tải dữ liệu.");
            } finally {
                setLoading(false);
            }
        };

        fetchDropdownData();
    }, [isOpen]);

    const handleInputChange = (e) => {
        setFormData(prev => ({
            ...prev,
            [e.target.name]: e.target.value
        }));
        setError(null);
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError(null);

        try {
            await coachApi.createCoach(formData);
            onSuccess();
            onClose();
        } catch (error) {
            console.log(error.response);
            console.log(formData);
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
                    
                    <div className="d-flex gap-3">
                        <Form.Group className="mb-4 flex-fill">
                            <Form.Label className="fw-semibold text-secondary">
                                Loại xe <span className="text-danger">*</span>
                            </Form.Label>
                            <Form.Select
                                value={formData.coachTypeId}
                                name="coachTypeId"
                                onChange={handleInputChange}
                                required
                                disabled={loading}
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
                                disabled={loading}
                                className="py-2"
                            >
                                <option value="">-- Chọn tuyến xe --</option>
                                {routes.map(r => <option key={r.routeId} value={r.routeId}>{r.routeName}</option>)}
                            </Form.Select>
                        </Form.Group>
                    </div>

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