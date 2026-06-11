import React, { useState, useEffect } from 'react';
import axiosClient from '../../../api/axiosClient';
import './CargoTypePriceManager.css';

const CargoTypePriceManager = () => {
    const [prices, setPrices] = useState([]);
    const [cargoTypes, setCargoTypes] = useState([]);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);

    const [isEditing, setIsEditing] = useState(false);
    const [currentPrice, setCurrentPrice] = useState({
        cargoTypeId: '',
        unit: '',
        pricePerUnit: '',
        startEffectiveDate: '',
        endEffectiveDate: ''
    });

    const fetchCargoTypes = async () => {
        try {
            const data = await axiosClient.get('/v1/manager/cargo-types', {
                params: { size: 1000 }
            });
            setCargoTypes(data.content || []);
        } catch (error) {
            console.error('Failed to fetch cargo types', error);
        }
    };

    const fetchPrices = async () => {
        setLoading(true);
        try {
            const data = await axiosClient.get('/v1/manager/cargo-type-prices', {
                params: {
                    search,
                    page,
                    size: 5
                }
            });
            setPrices(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (error) {
            console.error('Failed to fetch cargo type prices', error.response?.data?.message || error.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCargoTypes();
    }, []);

    useEffect(() => {
        fetchPrices();
    }, [page, search]);

    const handleSearchChange = (e) => {
        setSearch(e.target.value);
        setPage(0);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCurrentPrice(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSave = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...currentPrice,
                cargoTypeId: parseInt(currentPrice.cargoTypeId),
                pricePerUnit: parseFloat(currentPrice.pricePerUnit)
            };

            if (isEditing && currentPrice.cargoTypePriceId) {
                await axiosClient.put(`/v1/manager/cargo-type-prices/${currentPrice.cargoTypePriceId}`, payload);
            } else {
                await axiosClient.post('/v1/manager/cargo-type-prices', payload);
            }
            setIsEditing(false);
            setCurrentPrice({
                cargoTypeId: '',
                unit: '',
                pricePerUnit: '',
                startEffectiveDate: '',
                endEffectiveDate: ''
            });
            fetchPrices();
        } catch (error) {
            console.error('Failed to save cargo type price', error.response?.data?.message || error.message);
            alert(error.response?.data?.message || 'Failed to save price');
        }
    };

    const handleEdit = (price) => {
        setCurrentPrice({
            ...price,
            startEffectiveDate: price.startEffectiveDate ? price.startEffectiveDate.substring(0, 16) : '',
            endEffectiveDate: price.endEffectiveDate ? price.endEffectiveDate.substring(0, 16) : ''
        });
        setIsEditing(true);
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this price record?')) return;
        try {
            await axiosClient.delete(`/v1/manager/cargo-type-prices/${id}`);
            fetchPrices();
        } catch (error) {
            console.error('Failed to delete cargo type price', error.response?.data?.message || error.message);
            alert(error.response?.data?.message || 'Failed to delete price');
        }
    };

    const handleCancel = () => {
        setIsEditing(false);
        setCurrentPrice({
            cargoTypeId: '',
            unit: '',
            pricePerUnit: '',
            startEffectiveDate: '',
            endEffectiveDate: ''
        });
    };

    const getCargoTypeName = (id) => {
        const type = cargoTypes.find(t => t.cargoTypeId === id);
        return type ? type.cargoTypeName : id;
    };

    return (
        <div className="cargo-type-price-manager">
            <div className="cargo-type-price-manager-header">
                <h2 style={{ fontWeight: 'bold' }}>Giá cước</h2>
            </div>

            <div className="cargo-type-price-manager-content">
                <div className="cargo-type-price-form-container">
                    <h3>{isEditing ? 'Sửa Giá Cước' : 'Thêm Giá Cước Mới'}</h3>
                    <form onSubmit={handleSave} className="cargo-type-price-form">
                        <div className="form-group">
                            <label>Loại Hàng</label>
                            <select
                                name="cargoTypeId"
                                value={currentPrice.cargoTypeId || ''}
                                onChange={handleInputChange}
                                required
                            >
                                <option value="" disabled>-- Chọn Loại Hàng --</option>
                                {cargoTypes.map(type => (
                                    <option key={type.cargoTypeId} value={type.cargoTypeId}>
                                        {type.cargoTypeName}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label>Đơn Vị (VD: kg, m3)</label>
                            <input
                                type="text"
                                name="unit"
                                value={currentPrice.unit || ''}
                                onChange={handleInputChange}
                                required
                                placeholder="VD: kg"
                            />
                        </div>
                        <div className="form-group">
                            <label>Đơn Giá (VNĐ)</label>
                            <input
                                type="number"
                                name="pricePerUnit"
                                value={currentPrice.pricePerUnit || ''}
                                onChange={handleInputChange}
                                required
                                min="0"
                                step="1000"
                                placeholder="VD: 50000"
                            />
                        </div>
                        <div className="form-group">
                            <label>Ngày Bắt Đầu Hiệu Lực</label>
                            <input
                                type="datetime-local"
                                name="startEffectiveDate"
                                value={currentPrice.startEffectiveDate || ''}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label>Ngày Kết Thúc Hiệu Lực</label>
                            <input
                                type="datetime-local"
                                name="endEffectiveDate"
                                value={currentPrice.endEffectiveDate || ''}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-actions">
                            <button type="submit" className="save-btn">{isEditing ? 'Cập Nhật' : 'Tạo Mới'}</button>
                            {isEditing && <button type="button" className="cancel-btn" onClick={handleCancel}>Hủy</button>}
                        </div>
                    </form>
                </div>

                <div className="cargo-type-price-list-container">
                    <div className="cargo-type-price-list-header">
                        <h3>Danh sách Giá cước</h3>
                        <input
                            type="text"
                            placeholder="Tìm kiếm..."
                            value={search}
                            onChange={handleSearchChange}
                            className="search-input"
                        />
                    </div>

                    {loading ? (
                        <p>Đang tải dữ liệu...</p>
                    ) : (
                        <div className="table-responsive">
                            <table className="cargo-type-price-table">
                                <thead>
                                    <tr>
                                        <th>Loại Hàng</th>
                                        <th>Đơn Vị</th>
                                        <th>Đơn Giá</th>
                                        <th>Bắt Đầu</th>
                                        <th>Kết Thúc</th>
                                        <th>Hành động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {prices.length > 0 ? prices.map(price => (
                                        <tr key={price.cargoTypePriceId}>
                                            <td>{getCargoTypeName(price.cargoTypeId)}</td>
                                            <td>{price.unit}</td>
                                            <td>{price.pricePerUnit.toLocaleString()} VNĐ</td>
                                            <td>{new Date(price.startEffectiveDate).toLocaleString()}</td>
                                            <td>{new Date(price.endEffectiveDate).toLocaleString()}</td>
                                            <td className="actions-cell">
                                                <button onClick={() => handleEdit(price)} className="edit-btn">Sửa</button>
                                                <button onClick={() => handleDelete(price.cargoTypePriceId)} className="disable-btn">Xóa</button>
                                            </td>
                                        </tr>
                                    )) : (
                                        <tr>
                                            <td colSpan="6" className="text-center">Không tìm thấy giá cước nào.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {totalPages > 1 && (
                        <div className="pagination">
                            <button
                                disabled={page === 0}
                                onClick={() => setPage(p => p - 1)}
                            >
                                Previous
                            </button>
                            <span>Page {page + 1} of {totalPages}</span>
                            <button
                                disabled={page >= totalPages - 1}
                                onClick={() => setPage(p => p + 1)}
                            >
                                Next
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default CargoTypePriceManager;
