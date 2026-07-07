import { Badge, Button, OverlayTrigger, Tooltip } from 'react-bootstrap';
import { BsPersonHeart } from 'react-icons/bs';
import './TripStaff.css';

const STATUS_BADGE = {
    CONFIRMED: 'warning',
    CHECKED_IN: 'success',
};

export default function PassengerCard({ passenger, onCheckIn, onNoShow, checkingIn, noShowing, noShow }) {
    const canCheckIn = passenger.status === 'CONFIRMED' && !noShow;

    return (
        <div className={`passenger-card ${noShow ? 'passenger-card-no-show' : ''}`}>
            <div className="d-flex justify-content-between align-items-start gap-2 mb-2">
                <div>
                    <div className="fw-bold">{passenger.fullName}</div>
                    <div className="text-muted" style={{ fontSize: '14px' }}>{passenger.phone}</div>
                </div>
                {noShow ? (
                    <Badge bg="danger">Vắng mặt</Badge>
                ) : (
                    <Badge bg={STATUS_BADGE[passenger.status] || 'secondary'}>{passenger.status}</Badge>
                )}
            </div>
            <div className="text-muted mb-1" style={{ fontSize: '13px' }}>
                Ghế {passenger.seatCodeSnapshot}
            </div>
            <div className="text-muted mb-2" style={{ fontSize: '13px' }}>
                {passenger.pickupStopName} → {passenger.dropoffStopName}
            </div>
            {passenger.accompaniedChild && (
                <div className="passenger-card-child d-flex align-items-center gap-2">
                    <BsPersonHeart />
                    <span>
                        Trẻ em: {passenger.accompaniedChild.fullname} (sinh {passenger.accompaniedChild.birthYear})
                    </span>
                </div>
            )}
            <div className="d-flex gap-2 mt-3">
                <Button
                    size="sm"
                    className="custom-btn-general flex-grow-1"
                    disabled={!canCheckIn || checkingIn}
                    onClick={() => onCheckIn(passenger.ticketDetailId)}
                >
                    {checkingIn ? 'Đang xử lý...' : 'Check-in'}
                </Button>
                {canCheckIn && (
                    <Button
                        size="sm"
                        variant="outline-danger"
                        disabled={noShowing}
                        onClick={() => onNoShow(passenger.ticketDetailId)}
                    >
                        {noShowing ? '...' : 'Vắng mặt'}
                    </Button>
                )}
                <OverlayTrigger overlay={<Tooltip>In nhãn hành lý (demo)</Tooltip>}>
                    <span>
                        <Button size="sm" variant="outline-secondary" disabled>
                            In nhãn
                        </Button>
                    </span>
                </OverlayTrigger>
            </div>
        </div>
    );
}
