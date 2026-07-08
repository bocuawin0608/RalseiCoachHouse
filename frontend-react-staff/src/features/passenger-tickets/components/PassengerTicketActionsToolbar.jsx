import { Button, OverlayTrigger, Tooltip } from 'react-bootstrap';

function DisabledActionButton({ disabled, tooltip, variant, onClick, children }) {
    const button = (
        <Button
            variant={variant}
            size="sm"
            onClick={onClick}
            disabled={disabled}
            style={{
                cursor: disabled ? 'not-allowed' : 'pointer',
                pointerEvents: 'auto',
            }}
        >
            {children}
        </Button>
    );

    if (!disabled || !tooltip) {
        return button;
    }

    return (
        <OverlayTrigger overlay={<Tooltip>{tooltip}</Tooltip>}>
            <span className="d-inline-block">{button}</span>
        </OverlayTrigger>
    );
}

export default function PassengerTicketActionsToolbar({
    canChangeItinerary,
    canCancelTicket,
    cancelDisabledTooltip = 'Không thể hủy vé này',
    onChangeItinerary,
    onCancelTicket,
}) {
    return (
        <div className="border-top pt-3 mt-1">
            <div className="text-muted small mb-2">Thao tác trên vé</div>
            <div className="d-flex flex-wrap justify-content-start gap-2">
                <DisabledActionButton
                    variant="outline-primary"
                    disabled={!canChangeItinerary}
                    tooltip="Không thể đổi hành trình cho vé này"
                    onClick={() => onChangeItinerary?.()}
                >
                    Đổi hành trình
                </DisabledActionButton>

                <DisabledActionButton
                    variant="outline-danger"
                    disabled={!canCancelTicket}
                    tooltip={cancelDisabledTooltip}
                    onClick={() => onCancelTicket?.()}
                >
                    Hủy vé
                </DisabledActionButton>
            </div>
        </div>
    );
}
