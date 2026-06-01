import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { seatLayoutService } from "../../services/seatLayoutService";
import SeatIcon from "../../components/seat-layout/SeatIcon";
import Button from '../../components/common/Button';

export default function SeatLayoutDetailPage () {
    const navigate = useNavigate();
    const {id} = useParams();
    const [detailData, setDetailData] = useState(null);
    const [errorMsg, setErrorMsg] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const loadDetail = async () => {
            try {
                setLoading(true);
                setDetailData(await seatLayoutService.getSeatLayoutDetail(id));
            } catch(error) {
                setErrorMsg(error.response?.data?.message || "Có lỗi xảy ra!") 
                console.log("Lỗi tải dữ liệu chi tiết sơ đồ ghế: ", error);
            } finally {
                setLoading(false);
            }
        }
        loadDetail();
    }, [id]);

    const { maxRow, maxCol } = useMemo(() => {
        if (!detailData || !detailData.seats || detailData.seats.length === 0) {
            return { maxRow: 0, maxCol: 0 };
        }

        let maxR = 0;
        let maxC = 0;

        detailData.seats.forEach(seat => {
            if (seat.rowIndex > maxR) maxR = seat.rowIndex;
            if (seat.colIndex > maxC) maxC = seat.colIndex;
        });

        return { maxRow: maxR, maxCol: maxC };
    }, [detailData]);

    if (loading) return (<div style={{ padding: '20px' }}>Đang tải chi tiết sơ đồ ghế...</div>)
    if (errorMsg) return (<div style={{ padding: '20px', color: 'red' }}>{errorMsg}</div>);
    if (!detailData) return null;

    return (
        <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <div>
                    <h2 style={{ margin: 0 }}>Sơ đồ: {detailData.seatLayoutName}</h2>
                    <p style={{ margin: '5px 0 0 0', color: '#666' }}>
                        Tổng ghế: <strong>{detailData.totalSeat}</strong> | Giá: <strong>{detailData.currentPrice?.toLocaleString('vi-VN')} đ</strong> | Trạng thái: <strong>{detailData.isActive ? "Đang hoạt động" : "Đã tắt"}</strong>
                    </p>
                </div>
                <Button variant="secondary" onClick={() => navigate(-1)}>
                    Quay lại
                </Button>
            </div>

            <div style={{ 
                borderRadius: '20px', 
                padding: '40px 20px',
                backgroundColor: '#f9f9f9'
            }}>

                <div style={{
                    display: 'grid',
                    gridTemplateColumns: `repeat(${maxCol}, 50px)`,
                    justifyContent: 'center',
                    gap: '15px 20px'
                }}>
                    {detailData.seats.map((seat) => {
                        return (
                            <div 
                                key={seat.seatId}
                                style={{
                                    gridRow: seat.rowIndex,
                                    gridColumn: seat.colIndex
                                }}
                            >
                                <SeatIcon 
                                    status={seat.isActive ? 'active' : 'inactive'} 
                                    code={seat.seatCode} 
                                />
                            </div>
                        );
                    })}
                </div>
            </div>

            <div style={{ display: 'flex', gap: '20px', marginTop: '30px', justifyContent: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <div style={{ width: '20px', height: '20px', backgroundColor: '#FCE6D5', border: '1px solid #F6CDB5', borderRadius:'5px' }}></div>
                    <span>Ghế đang hoạt động</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <div style={{ width: '20px', height: '20px', backgroundColor: '#D9D9D9', border: '1px solid #D9D9D9', borderRadius:'5px'}}></div>
                    <span>Ghế đã khóa/hỏng</span>
                </div>
            </div>
        </div>
    );
}