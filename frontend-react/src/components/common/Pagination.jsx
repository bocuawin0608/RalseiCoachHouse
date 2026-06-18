import { Button } from "react-bootstrap";

export default function Pagination({ pageInfo, onPageChange }) {
    return (
        <div className="d-flex align-items-center justify-content-center gap-3 mt-4 mb-2">
            
            <Button 
                variant="outline-secondary"
                disabled={pageInfo.page === 0} 
                onClick={() => onPageChange(prev => ({ ...prev, page: prev.page - 1 }))}
                className="fw-medium px-3"
            >
                &laquo; Trang trước
            </Button>
            
            <span className="fw-medium text-dark">
                Trang {pageInfo.page + 1} / {pageInfo.totalPages === 0 ? 1 : pageInfo.totalPages} 
                <span className="text-muted ms-2 fw-normal">
                    (Tổng: {pageInfo.totalElements} bản ghi)
                </span>
            </span>
            
            <Button 
                variant="outline-secondary"
                disabled={pageInfo.page >= pageInfo.totalPages - 1 || pageInfo.totalPages === 0} 
                onClick={() => onPageChange(prev => ({ ...prev, page: prev.page + 1 }))}
                className="fw-medium px-3"
            >
                Trang sau &raquo;
            </Button>

        </div>
    );
}