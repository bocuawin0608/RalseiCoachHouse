import { useEffect, useState } from 'react';
import { Modal, Spinner } from 'react-bootstrap';
import CargoTicketForm from './CargoTicketForm';
import { cargoTicketApi } from '../api/cargoTicketApi';

export default function CargoTicketUpdateModal({ data, onClose, onSuccess }) {
    const [details, setDetails] = useState(null);

    useEffect(() => {
        if (!data) return;
        cargoTicketApi.getCargoTicketDetails(data.cargoTicketId)
            .then(setDetails)
            .catch(() => setDetails([]));
    }, [data]);

    if (!data) return null;

    const handleSubmit = async (payload) => {
        await cargoTicketApi.updateCargoTicketWithDetails(data.cargoTicketId, payload);
        await onSuccess();
        onClose();
    };

    return (
        <Modal show onHide={onClose} size="xl" centered backdrop="static">
            <Modal.Header closeButton><Modal.Title>Cập nhật đơn gửi hàng</Modal.Title></Modal.Header>
            <Modal.Body className="p-4">{details === null
                ? <div className="text-center p-5"><Spinner size="sm" /> Đang tải thông tin hiện tại...</div>
                : <CargoTicketForm initialData={{ ...data, details }} onSubmit={handleSubmit} submitLabel="Lưu thay đổi" />}
            </Modal.Body>
        </Modal>
    );
}
