import { Outlet } from "react-router-dom";
import PublicHeader from "./PublicHeader";
import PublicFooter from "./PublicFooter";

export default function PublicLayout() {
    return (
        <div className="d-flex flex-column min-vh-100 bg-light">
            <style>{`
                /* 1. Menu Links (Trang chủ, Tra cứu,...) */
                .navbar .btn-link {
                    color: var(--ralsei-black) !important;
                    font-weight: 700 !important; /* Bold hơn */
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
                    text-decoration: none !important;
                    padding: 8px 16px !important;
                }
                .navbar .btn-link:hover {
                    background-color: var(--ralsei-black) !important; /* Nền đen đậm */
                    color: var(--ralsei-green) !important;           /* Chữ xanh ngọc nổi bật */
                    border-radius: 8px;
                    transform: translateY(-2px);
                }

                /* 2. Nút Đăng nhập - Phong cách Nút phụ (Outline) */
                .navbar .btn-light {
                    background-color: transparent !important;
                    color: var(--ralsei-black) !important;
                    border: 2px solid var(--ralsei-black) !important; /* Viền đen mạnh mẽ */
                    font-weight: 700 !important;
                    transition: all 0.3s ease !important;
                }
                .navbar .btn-light:hover {
                    background-color: var(--ralsei-pink) !important; 
                    border-color: var(--ralsei-pink) !important;
                    color: var(--ralsei-white) !important;
                    transform: translateY(-2px);
                    /* Thêm bóng đổ đồng màu với màu hồng để tạo chiều sâu */
                    box-shadow: 0 6px 15px rgba(255, 71, 163, 0.4) !important;
                }

                /* 3. Nút Đăng ký - Unique & Nổi bật */
                .navbar .btn[style*="background-color: var(--ralsei-black)"] {
                    background-color: var(--ralsei-black) !important;
                    color: var(--ralsei-white) !important;
                    border: 2px solid var(--ralsei-black) !important;
                    font-weight: 700 !important;
                    transition: all 0.4s ease !important; /* Tăng thời gian chuyển động để mượt hơn */
                }
                
                .navbar .btn[style*="background-color: var(--ralsei-black)"]:hover {
                    /* Màu hồng rực rỡ nhưng sang trọng */
                    background-color: var(--ralsei-pink) !important; 
                    border-color: var(--ralsei-pink) !important;
                    color: var(--ralsei-white) !important;
                    transform: translateY(-2px);
                    /* Thêm bóng đổ đồng màu với màu hồng để tạo chiều sâu */
                    box-shadow: 0 6px 15px rgba(255, 71, 163, 0.4) !important;
                }
            `}</style>
            <PublicHeader />
            
            <main className="flex-grow-1 d-flex flex-column">
                <Outlet /> 
            </main>
            
            <PublicFooter />
        </div>
    )
}