import { Card } from 'react-bootstrap';

const STEP_LABELS = [
    { number: '❶', label: 'Chọn ghế ngồi' },
    { number: '❷', label: 'Thông tin lịch trình' },
    { number: '❸', label: 'Thanh toán online' },
];

export default function BookingWizardShell({
    step = 1,
    paymentMode = false,
    onBack,
    tripTitle = '',
    tripDate = '',
    children,
}) {
    const activeStep = paymentMode ? 3 : step;
    const progressWidth = activeStep === 1 ? '16.66%' : activeStep === 2 ? '50%' : '100%';

    return (
        <Card className="shadow-sm border-0 rounded-4 overflow-hidden">
            <Card.Header className="bg-white border-bottom pt-3 pb-1 px-3">
                <div className="d-flex align-items-center gap-3 mb-3">
                    {!paymentMode && (
                        <button
                            type="button"
                            onClick={onBack}
                            className="btn btn-sm d-flex align-items-center gap-1 px-3 py-1.5 rounded-pill border border-dark"
                            style={{
                                backgroundColor: '#fff',
                                color: '#000',
                                fontSize: '0.9rem',
                                fontWeight: '600',
                                transition: 'all 0.2s ease',
                            }}
                            onMouseOver={(e) => { e.currentTarget.style.backgroundColor = 'var(--ralsei-black)'; e.currentTarget.style.color = '#fff'; }}
                            onMouseOut={(e) => { e.currentTarget.style.backgroundColor = '#fff'; e.currentTarget.style.color = '#000'; }}
                        >
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                                <line x1="19" y1="12" x2="5" y2="12"></line>
                                <polyline points="12 19 5 12 12 5"></polyline>
                            </svg>
                            Quay lại
                        </button>
                    )}

                    <div className="d-flex flex-column text-start">
                        <span className="fw-bold text-dark lh-sm" style={{ fontSize: '1rem', letterSpacing: '-0.01em' }}>
                            {tripTitle}
                        </span>
                        <small className="text-muted mt-0.5" style={{ fontSize: '0.8rem', fontWeight: '500' }}>
                            {tripDate}
                        </small>
                    </div>
                </div>

                <div className="row text-center g-0 my-2.5" style={{ fontSize: '0.9rem', fontWeight: '600' }}>
                    {STEP_LABELS.map((item, index) => {
                        const stepNumber = index + 1;
                        const isActive = activeStep >= stepNumber;
                        return (
                            <div key={item.label} className="col">
                                <div style={{ color: isActive ? 'var(--ralsei-black)' : '#a0a0a0', transition: 'color 0.3s' }}>
                                    <span className="me-1.5">{item.number}</span> {item.label}
                                </div>
                            </div>
                        );
                    })}
                </div>

                <div className="progress" style={{ height: '3px', margin: '8px -16px -4px -16px', borderRadius: 0, backgroundColor: '#f0f0f0' }}>
                    <div
                        className="progress-bar"
                        role="progressbar"
                        style={{
                            width: progressWidth,
                            backgroundColor: 'var(--ralsei-black)',
                            transition: 'width 0.4s ease',
                        }}
                    />
                </div>
            </Card.Header>

            <Card.Body className="p-4 p-md-5">
                {children}
            </Card.Body>
        </Card>
    );
}
