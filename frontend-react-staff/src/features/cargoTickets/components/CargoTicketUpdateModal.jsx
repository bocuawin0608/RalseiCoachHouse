import { Modal } from 'react-bootstrap';
import CargoTicketForm from './CargoTicketForm';
import { cargoTicketApi } from '../api/cargoTicketApi';

export default function CargoTicketUpdateModal({ data, onClose, onSuccess }) {
    if (!data) return null;

    const handleSubmit = async (payload) => {
        await cargoTicketApi.updateCargoTicket(data.cargoTicketId, payload);
        await onSuccess();
        onClose();
    };

    return (
        <Modal show onHide={onClose} size="xl" centered backdrop="static">
            <Modal.Header closeButton><Modal.Title>Cập nhật vé hàng hóa</Modal.Title></Modal.Header>
            <Modal.Body className="p-4"><CargoTicketForm initialData={data} onSubmit={handleSubmit} submitLabel="Lưu thay đổi" /></Modal.Body>
        </Modal>
    );
}
