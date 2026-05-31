export default function Pagination({ pageInfo, onPageChange }) {
    return (
        <div style={{ marginTop: '20px', display: 'flex', gap: '15px', alignItems: 'center', justifyContent:'center'}}>
                <button 
                    disabled={pageInfo.page === 0} 
                    onClick={() => onPageChange(prev => ({ ...prev, page: prev.page - 1 }))}
                    style={{ cursor: pageInfo.page === 0 ? 'not-allowed' : 'pointer' }}
                >
                    Trang trước
                </button>
                
                <span>
                    Trang {pageInfo.page + 1} / {pageInfo.totalPages === 0 ? 1 : pageInfo.totalPages} 
                    {" "}(Tổng: {pageInfo.totalElements} bản ghi)
                </span>
                
                <button 
                    disabled={pageInfo.page >= pageInfo.totalPages - 1 || pageInfo.totalPages === 0} 
                    onClick={() => onPageChange(prev => ({ ...prev, page: prev.page + 1 }))}
                    style={{ cursor: (pageInfo.page >= pageInfo.totalPages - 1) ? 'not-allowed' : 'pointer' }}
                >
                    Trang sau
                </button>
            </div>
    );
}