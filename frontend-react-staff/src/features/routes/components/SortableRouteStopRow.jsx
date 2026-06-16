import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { Button } from "react-bootstrap";
import { BsTrash, BsPencilFill, BsGripVertical } from "react-icons/bs";
import { MdDangerous } from "react-icons/md";

export default function SortableRouteStopRow({ stop, onEdit, onDelete }) {
    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition,
        isDragging,
    } = useSortable({ id: stop.routeStopId });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
        opacity: isDragging ? 0.5 : 1,
        backgroundColor: isDragging ? '#f8f9fa' : undefined,
        zIndex: isDragging ? 1 : undefined,
        position: isDragging ? 'relative' : undefined,
    };

    return (
        <tr ref={setNodeRef} style={style} className={!stop.stopPointActive ? 'table-danger' : ''}>
            <td className={`fw-bold ${!stop.stopPointActive ? 'text-danger' : 'text-primary'}`}>
                <div className="d-flex align-items-center justify-content-center gap-2">
                    <span
                        {...attributes}
                        {...listeners}
                        style={{ cursor: 'grab', display: 'flex', alignItems: 'center' }}
                        title="Kéo thả để sắp xếp"
                    >
                        <BsGripVertical size={20} className="text-secondary" />
                    </span>
                    <span>{stop.stopOrder}</span>
                    {!stop.stopPointActive && (
                        <MdDangerous size={18} className="text-danger" title="Ngừng HĐ" />
                    )}
                </div>
            </td>
            <td className="text-start fw-medium">{stop.stopPointName}</td>
            <td>{stop.kilometersFromStart} km</td>
            <td>{stop.minutesFromStart} phút</td>
            <td>
                <div className="d-flex gap-2 justify-content-center">
                    <Button className="custom-btn-general" size="sm" onClick={() => onEdit(stop)} title="Sửa điểm dừng">
                        <BsPencilFill />
                    </Button>
                    <Button variant="danger" size="sm" onClick={() => onDelete(stop.routeStopId)} title="Xóa điểm dừng">
                        <BsTrash />
                    </Button>
                </div>
            </td>
        </tr>
    );
}
