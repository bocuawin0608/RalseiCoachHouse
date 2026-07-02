import { Modal, Button, Table } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const STATUS_LABELS = {
    ACTIVE: 'Đang hoạt động',
    MAINTENANCE: 'Đang bảo trì',
    RETIRED: 'Ngừng hoạt động',
};

export default function CoachTypeDeactivationDialog({
    isOpen,
    coachTypeId,
    checkData,
    onClose,
}) {
    if (!isOpen || !checkData) return null;

    return (
        <Modal show={isOpen} onHide={onClose} centered backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title className="fs-5 fw-bold text-danger">
                    Không thể tắt loại xe
                </Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <p className="text-secondary mb-3">
                    Loại xe này vẫn còn <strong>{checkData.activeCoaches?.length ?? 0}</strong> xe
                    chưa ngừng hoạt động. Bạn cần xử lý các xe sau trước khi tắt loại xe.
                </p>
                <Table responsive size="sm" className="mb-0">
                    <thead className="table-light">
                        <tr>
                            <th>Biển số</th>
                            <th>Trạng thái</th>
                            <th>Chuyến sắp tới</th>
                        </tr>
                    </thead>
                    <tbody>
                        {(checkData.activeCoaches ?? []).map((c) => (
                            <tr key={c.coachId}>
                                <td className="fw-medium">{c.licensePlate}</td>
                                <td>{STATUS_LABELS[c.status] ?? c.status}</td>
                                <td>{c.upcomingTripCount > 0 ? c.upcomingTripCount : '—'}</td>
                            </tr>
                        ))}
                    </tbody>
                </Table>
            </Modal.Body>
            <Modal.Footer className="bg-light border-0">
                <Button variant="outline-secondary" onClick={onClose}>
                    Đóng
                </Button>
                <Button
                    as={Link}
                    to={`/management/coaches?coachTypeId=${coachTypeId}`}
                    className="custom-btn-general"
                    onClick={onClose}
                >
                    Xem danh sách xe
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
