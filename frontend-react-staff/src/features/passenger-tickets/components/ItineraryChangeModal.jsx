import { Alert, Button, Modal } from 'react-bootstrap';
import { BsExclamationTriangleFill } from 'react-icons/bs';
import { useItineraryChangeWorkflow } from '../hooks/useItineraryChangeWorkflow';
import { formatDateTime } from '../utils/passengerTicketFormatters';
import ItineraryChangePreview from './itinerary-change/ItineraryChangePreview';
import ItineraryStopSelection from './itinerary-change/ItineraryStopSelection';
import TransferSeatSelection from './itinerary-change/TransferSeatSelection';
import TransferTripSelection from './itinerary-change/TransferTripSelection';

export default function ItineraryChangeModal({
    isOpen,
    mode = 'same-trip',
    ticket,
    onClose,
    onSuccess,
}) {
    const workflow = useItineraryChangeWorkflow({
        isOpen,
        mode,
        ticket,
        onClose,
        onSuccess,
    });

    if (!isOpen || !ticket) return null;

    const operationBusy = workflow.submitting || workflow.locking;

    return (
        <Modal
            show={isOpen}
            onHide={workflow.handleClose}
            size="lg"
            centered
            backdrop="static"
            keyboard={!operationBusy}
        >
            <Modal.Header closeButton={!operationBusy}>
                <Modal.Title className="fs-5 fw-bold text-primary">
                    {workflow.isTransferMode ? 'Đổi chuyến' : 'Đổi hành trình'}
                </Modal.Title>
            </Modal.Header>

            <Modal.Body className="px-4">
                <Alert variant="info" className="py-2 px-3 border-0 small">
                    {workflow.isTransferMode
                        ? 'Chọn chuyến mới có giá ≤ giá vé hiện tại. Thao tác này dùng quyền đổi chuyến/hủy một lần.'
                        : 'Chỉ đổi điểm đón/trả trên chuyến hiện tại. Không hoàn tiền chênh lệch. Mã QR lên xe không đổi.'}
                </Alert>

                <div className="mb-3 p-3 bg-light border rounded small">
                    <div className="fw-semibold mb-1">Chuyến hiện tại</div>
                    <div>{ticket.routeName} • {formatDateTime(ticket.departureTime)}</div>
                    <div className="text-muted">
                        {ticket.pickupStopName} → {ticket.dropoffStopName}
                    </div>
                </div>

                {workflow.isTransferMode && <TransferTripSelection workflow={workflow} />}

                {(workflow.keepCurrentTrip || workflow.selectedTripId) && (
                    <ItineraryStopSelection workflow={workflow} />
                )}

                {workflow.isTransferMode && workflow.selectedTripId && (
                    <TransferSeatSelection workflow={workflow} />
                )}

                <ItineraryChangePreview workflow={workflow} />

                {workflow.error && (
                    <Alert
                        variant="danger"
                        className="mt-3 mb-0 py-2 px-3 border-0 d-flex align-items-center gap-2"
                    >
                        <BsExclamationTriangleFill />
                        <span>{workflow.error}</span>
                    </Alert>
                )}
            </Modal.Body>

            <Modal.Footer className="bg-light border-0 rounded-bottom">
                <Button
                    variant="outline-secondary"
                    onClick={workflow.handleClose}
                    disabled={operationBusy}
                >
                    Hủy bỏ
                </Button>
                <Button
                    className="custom-btn-general px-4"
                    onClick={workflow.handleSubmit}
                    disabled={
                        !workflow.canSubmit
                        || operationBusy
                        || workflow.previewing
                    }
                >
                    {workflow.submitting
                        ? 'Đang lưu...'
                        : (workflow.isTransferMode
                            ? 'Xác nhận đổi chuyến'
                            : 'Xác nhận đổi hành trình')}
                </Button>
            </Modal.Footer>
        </Modal>
    );
}
