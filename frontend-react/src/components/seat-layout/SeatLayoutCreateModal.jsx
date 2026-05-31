import { useState, useEffect } from "react"
import Button from "../common/Button";
import { seatLayoutService } from "../../services/seatLayoutService";

const INITIAL_FORM = { seatLayoutName: '', totalRows: '', totalCols: '', seatPrice: '' };

export default function SeatLayoutCreateModal({isOpen, onClose, onSuccess}) {
    const [formData, setFormData] = useState(INITIAL_FORM);
    const [loading, setLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState('');

    useEffect(() => {
        const load = () => setFormData(INITIAL_FORM)
        if (!isOpen) load();
    }, [isOpen]);

    if (!isOpen) return null;

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({...prev, [name]: value}));
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setErrorMsg('');

        try {
            const payload = {
                seatLayoutName: formData.seatLayoutName.trim(),
                totalRows: +formData.totalRows,
                totalCols: +formData.totalCols,
                seatPrice: +formData.seatPrice
            }

            await seatLayoutService.createSeatLayout(payload);

            setFormData(INITIAL_FORM);
            onSuccess();
            onClose();
        } catch (error) {
            setErrorMsg(error.response?.data?.message || 'Có lỗi xảy ra khi tạo sơ đồ ghế!');
            console.error("Lỗi tạo SeatLayout: ", error);
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
                <h3 style={{ marginTop: 0, marginBottom: '20px' }}>Tạo sơ đồ ghế mới</h3>

                {errorMsg && <div style={{ color: 'red', marginBottom: '15px', fontSize: '14px' }}>{errorMsg}</div>}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <div>
                        <label style={{ display: 'block', marginBottom: '5px', fontWeight: 500 }}>Tên sơ đồ:</label>
                        <input type="text" name="seatLayoutName" value={formData.seatLayoutName}
                            onChange={handleChange} required maxLength={100}
                            placeholder="VD: Xe giường nằm 34 chỗ"
                            style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}/>
                    </div>

                    <div style={{display:'flex', gap:'5px'}}>
                        <div style={{flex:'1'}}>
                            <label>Số hàng:</label>
                            <input type="number" name="totalRows" value={formData.totalRows}
                                onChange={handleChange} required min={1} max={20}
                                style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}/>
                        </div>
                        <div style={{flex:'1'}}>
                            <label>Số cột:</label>
                            <input type="number" name="totalCols" value={formData.totalCols}
                                onChange={handleChange} required min={1} max={10}
                                style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}/>
                        </div>
                    </div>

                    <div>
                        <label style={{ display: 'block', marginBottom: '5px', fontWeight: 500 }}>Giá tiền:</label>
                        <input type="number" name="seatPrice" value={formData.seatPrice}
                            onChange={handleChange} required min={0} step='1000'
                            placeholder="VD: 250000" style={{ width: '100%', padding: '8px', boxSizing: 'border-box' }}/>
                    </div>

                    <div style={{display:'flex', gap:'15px', justifyContent:'center'}}>
                        <Button type={'submit'} disabled={loading} children={loading ? 'Đang tạo...' : 'Tạo mới'} />
                        <Button onClick={onClose} disabled={loading} children={'Hủy bỏ'}/>
                    </div>
                </form>
            </div>
        </div>
    )
}