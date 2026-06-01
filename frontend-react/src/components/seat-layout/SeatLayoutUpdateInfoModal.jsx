import { useEffect, useState } from "react"
import Button from "../common/Button";
import { seatLayoutService } from "../../services/seatLayoutService";

const INITIAL_FORM = {seatLayoutName: '', isActive: false};

export default function SeatLayoutUpdateInfoModal({isOpen, detailData, onClose, onSuccess}) {
    const [formData, setFormData] = useState(INITIAL_FORM);
    const [errorMsg, setErrorMsg] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const load = () => {
            if (detailData) setFormData({ seatLayoutName: detailData.seatLayoutName, isActive: detailData.isActive });
        }
        load();
    }, [detailData]);

    if(!isOpen) return null;

    const handleChange = (e) => {
        const { name, value, type } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'radio' ? value === 'true' : value
        }));
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            setLoading(true);
            setErrorMsg("");
            await seatLayoutService.updateSeatLayoutInfo(detailData.seatLayoutId, formData);
            setFormData(INITIAL_FORM);
            onSuccess();
            onClose();
        } catch(error) {
            setErrorMsg(error.response?.data?.message || 'Có lỗi xảy ra!');
            console.error("Lỗi cập nhật thông tin: " + error);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div style={{backgroundColor:'rgba(0, 0, 0, 0.5)', position:'fixed', top:0, left:0, right:0, bottom:0,
            display:'flex', justifyContent:'center', alignItems:'center', zIndex:'999'
        }}>
            <div style={{backgroundColor:'white', width:'450px', padding: '24px', borderRadius: '8px', 
                boxShadow: '0 4px 12px rgba(0,0,0,0.15)'}}>
                <h3 style={{ marginTop: 0, marginBottom: '20px' }}>Chỉnh sửa thông tin chung</h3>

                {errorMsg && <div style={{ color: 'red', marginBottom: '15px', fontSize: '14px' }}>{errorMsg}</div>}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <div>
                        <label htmlFor="seatLayoutName" style={{ marginBottom: '5px', fontWeight: 500 }}>Tên sơ đồ ghế: </label>
                        <input type="text" name="seatLayoutName" value={formData.seatLayoutName} onChange={handleChange} 
                            style={{width: '100%',  padding: '8px', boxSizing: 'border-box', textAlign:'center' }}/>
                    </div>

                    <div>
                        <input type="radio" name="isActive" value={true} checked={formData.isActive === true} onChange={handleChange}/>Đang hoạt động
                        <input type="radio" name="isActive" value={false} checked={formData.isActive === false} onChange={handleChange}/>Đã tắt
                    </div>

                    <div style={{display:'flex', gap:'15px', justifyContent:'center'}}>
                        <Button type={'submit'} disabled={loading} children={loading ? 'Đang sửa...' : 'Cập nhật'} />
                        <Button onClick={onClose} disabled={loading} children={'Hủy bỏ'}/>
                    </div>
                </form>
            </div>
        </div>
    )
}