import { useState, useEffect } from "react";
import { Badge, Button, Form, Spinner } from "react-bootstrap";
import { BsPlusCircleFill, BsSearch } from "react-icons/bs";
import { FaCirclePlus, FaCircleXmark } from "react-icons/fa6";

export default function AvailableCoachStopsPanel({
    showRightPanel,
    isClosingRightPanel,
    availableCoachStops,
    coachStopSearch,
    setCoachStopSearch,
    coachStopsLoading,
    routeStops,
    handleAddCoachStop,
    pendingCoachStop
}) {
    if (!showRightPanel) return null;

    const [localSearch, setLocalSearch] = useState(coachStopSearch || "");

    useEffect(() => {
        if (coachStopSearch === '') {
            setLocalSearch('');
        }
    }, [coachStopSearch]);

    useEffect(() => {
        const handler = setTimeout(() => {
            setCoachStopSearch(localSearch);
        }, 300);

        return () => {
            clearTimeout(handler);
        };
    }, [localSearch, setCoachStopSearch]);

    return (
        <div
            className="border-start bg-white overflow-auto px-3 py-4"
            style={{
                flex: '1 1 0%',
                maxHeight: '70vh',
                animation: isClosingRightPanel ? 'slideOutRight 0.25s ease forwards' : 'slideInRight 0.25s ease'
            }}
        >
            <div className="d-flex align-items-center justify-content-between mb-3">
                <h6 className="fw-bold text-dark mb-0" style={{ fontSize: '0.95rem' }}>
                    Danh sách trạm dừng
                </h6>
                <Badge bg="secondary" pill>
                    {availableCoachStops.length}
                </Badge>
            </div>

            {/* Search box */}
            <div className="position-relative mb-3">
                <BsSearch
                    size={14}
                    className="position-absolute text-secondary"
                    style={{ left: '10px', top: '50%', transform: 'translateY(-50%)' }}
                />
                <Form.Control
                    type="text"
                    size="sm"
                    placeholder="Tìm kiếm trạm dừng..."
                    value={localSearch}
                    onChange={(e) => setLocalSearch(e.target.value)}
                    style={{ paddingLeft: '32px' }}
                    className="rounded-pill"
                />
            </div>

            {coachStopsLoading ? (
                <div className="d-flex flex-column align-items-center justify-content-center py-4">
                    <Spinner animation="border" size="sm" variant="primary" />
                    <small className="mt-2 text-secondary">Đang tải...</small>
                </div>
            ) : availableCoachStops.length === 0 ? (
                <div className="text-center py-4">
                    <p className="text-muted mb-0 fst-italic" style={{ fontSize: '0.85rem' }}>
                        {coachStopSearch ? 'Không tìm thấy trạm dừng phù hợp.' : 'Tất cả trạm dừng đã được thêm vào tuyến.'}
                    </p>
                </div>
            ) : (
                <div className="d-flex flex-column gap-2">
                    {availableCoachStops.map(cs => (
                        <div
                            key={cs.stopPointId}
                            className={`d-flex align-items-center gap-2 p-2 rounded border coach-stop-item ${pendingCoachStop?.stopPointId === cs.stopPointId ? 'border-primary bg-primary bg-opacity-10' : 'bg-light'
                                }`}
                            style={{ cursor: 'pointer', transition: 'all 0.15s ease' }}
                        >
                            <div className="flex-fill" style={{ minWidth: 0 }}>
                                <div className="fw-semibold text-dark text-truncate" style={{ fontSize: '0.85rem' }}>
                                    {cs.stopPointName}
                                </div>
                                <small className="text-muted text-truncate d-block" style={{ fontSize: '0.75rem' }}>
                                    {cs.address}
                                </small>
                            </div>
                            <Button
                                variant="none"
                                className={`rounded-circle p-0 d-flex align-items-center justify-content-center text-white ${pendingCoachStop?.stopPointId === cs.stopPointId ? 'btn-danger' : 'custom-btn-general'
                                    }`}
                                style={{ width: '28px', height: '28px', flexShrink: 0 }}
                                onClick={() => handleAddCoachStop(cs)}
                            >
                                {pendingCoachStop?.stopPointId === cs.stopPointId ? (
                                    <FaCircleXmark size={20} />
                                ) : (
                                    <FaCirclePlus size={20} />
                                )}
                            </Button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
