import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { Button } from "react-bootstrap";
import { BsTrash, BsGripVertical, BsPlusCircleFill } from "react-icons/bs";
import { MdDangerous } from "react-icons/md";
import { FaCirclePlus } from "react-icons/fa6";

export default function SortableRouteStopRow({ stop, isDraftMode, pendingCoachStop, onInsertPending, isDeleteMode, isSelected, onToggleSelection }) {
    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition,
        isDragging,
    } = useSortable({
        id: stop.routeStopId || stop.id,
        disabled: !isDraftMode
    });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.5 : 1,
        backgroundColor: isDragging ? '#f8f9fa' : undefined,
        zIndex: isDragging ? 1 : undefined,
        position: isDragging ? 'relative' : undefined,
    };

    return (
        <tr
            ref={setNodeRef}
            style={{ ...style, cursor: isDraftMode ? 'grab' : 'default' }}
            className={!stop.stopPointActive ? 'table-danger' : ''}
            {...attributes}
            {...(isDraftMode ? listeners : {})}
            title={isDraftMode ? "Kéo thả để sắp xếp" : ""}
        >
            {isDeleteMode && (
                <td>
                    <div className="d-flex align-items-center justify-content-center h-100 mt-1">
                        <input
                            type="checkbox"
                            className="form-check-input mt-0"
                            style={{ cursor: 'pointer', width: '18px', height: '18px' }}
                            checked={isSelected || false}
                            onChange={(e) => { e.stopPropagation(); onToggleSelection(); }}
                            onPointerDown={(e) => e.stopPropagation()}
                            disabled={!stop.routeStopId} // disable if it's a draft stop that hasn't been saved yet
                        />
                    </div>
                </td>
            )}
            <td className={`fw-bold ${!stop.stopPointActive ? 'text-danger' : 'text-primary'}`}>
                <div className="d-flex align-items-center justify-content-center gap-2">
                    {pendingCoachStop && (
                        <Button
                            variant="none"
                            className="p-0 text-success d-flex align-items-center justify-content-center"
                            style={{ minWidth: '24px' }}
                            onClick={(e) => { e.stopPropagation(); onInsertPending(); }}
                            onPointerDown={(e) => e.stopPropagation()}
                            title="Thêm vào sau vị trí này"
                        >
                            <FaCirclePlus size={20} />
                        </Button>
                    )}
                    <span>{stop.stopOrder}</span>
                    {!stop.stopPointActive && (
                        <MdDangerous size={18} className="text-danger" title="Ngừng HĐ" />
                    )}
                </div>
            </td>
            <td className="text-start fw-medium">{stop.stopPointName}</td>
            <td className="text-start fw-medium">{stop.city}</td>
            <td>{stop.kilometersFromStart === 0 && stop.stopOrder != 1 || stop.kilometersFromStart === 1.2 ? "- -" : stop.kilometersFromStart + " km"}</td>
            <td>{stop.minutesFromStart === 0 && stop.stopOrder != 1 || stop.minutesFromStart === 1.2 ? "- -" : (stop.minutesFromStart >= 60 ? Math.floor(stop.minutesFromStart / 60) + " giờ " + (stop.minutesFromStart % 60) + " phút" : stop.minutesFromStart + " phút")} </td>
        </tr>
    );
}
