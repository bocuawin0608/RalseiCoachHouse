import { Button, Modal } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { BsBusFront, BsPersonBadge, BsTelephone } from 'react-icons/bs';
import './TripCrewModal.css';

/** Displays the complete driving crew assigned to one concrete trip. */
export default function TripCrewModal({ isOpen, trip, onClose, ticketsHref }) {
    if (!isOpen || !trip) return null;

    const departureDate = trip.departureDate
        ? new Date(`${trip.departureDate}T00:00:00`).toLocaleDateString('vi-VN')
        : '—';
    const departureTime = String(trip.departureTime || '').substring(0, 5) || '—';

    return (
        <Modal show onHide={onClose} centered>
            <Modal.Header closeButton>
                <Modal.Title className="trip-crew-title">Tổ lái chuyến xe</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <section className="trip-crew-trip-summary">
                    <BsBusFront size={24} />
                    <div>
                        <strong>{trip.routeName}</strong>
                        <span>{trip.licensePlate} · {departureDate} lúc {departureTime}</span>
                    </div>
                </section>

                <div className="trip-crew-list">
                    <article className="trip-crew-member">
                        <BsPersonBadge className="trip-crew-icon" />
                        <div className="driver-info">
                            <span className="trip-crew-role trip-crew-role--driver">
                                Tài xế
                            </span>
                            <strong>{trip.driverName || 'Chưa phân công'}</strong>
                            <span><BsTelephone /> {trip.driverPhone || 'Chưa có số điện thoại'}</span>
                        </div>
                    </article>
                    <article className="trip-crew-member">
                        <BsPersonBadge className="trip-crew-icon" />
                        <div className="attendant-info">
                            <span className="trip-crew-role trip-crew-role--attendant">
                                Phụ xe
                            </span>
                            <strong>{trip.attendantName || 'Chưa phân công'}</strong>
                            <span><BsTelephone /> {trip.attendantPhone || 'Chưa có số điện thoại'}</span>
                        </div>
                    </article>
                </div>
            </Modal.Body>
            <Modal.Footer className="d-flex justify-content-end gap-2">
                {ticketsHref ? (
                    <>
                        <Button variant="outline-secondary" onClick={onClose}>
                            Đóng
                        </Button>
                        <Button
                            as={Link}
                            to={ticketsHref}
                            className="custom-btn-general"
                            onClick={onClose}
                        >
                            Xem vé của chuyến này
                        </Button>
                    </>
                ) : (
                    <Button className="custom-btn-general" onClick={onClose}>
                        Đóng
                    </Button>
                )}
            </Modal.Footer>
        </Modal>
    );
}
