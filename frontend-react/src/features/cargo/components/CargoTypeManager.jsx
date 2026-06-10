import React, { useState, useEffect } from 'react';
import axiosClient from '../../../api/axiosClient';
import './CargoTypeManager.css';

const CargoTypeManager = () => {
    const [cargoTypes, setCargoTypes] = useState([]);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);

    const [isEditing, setIsEditing] = useState(false);
    const [currentType, setCurrentType] = useState({
        cargoTypeName: ''
    });

    const fetchCargoTypes = async () => {
        setLoading(true);
        try {
            const data = await axiosClient.get('/v1/manager/cargo-types', {
                params: {
                    search,
                    page,
                    size: 5
                }
            });
            setCargoTypes(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (error) {
            console.error('Failed to fetch cargo types', error.response?.data?.message || error.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCargoTypes();
    }, [page, search]);

    const handleSearchChange = (e) => {
        setSearch(e.target.value);
        setPage(0);
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setCurrentType(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSave = async (e) => {
        e.preventDefault();
        try {
            if (isEditing && currentType.cargoTypeId) {
                await axiosClient.put(`/v1/manager/cargo-types/${currentType.cargoTypeId}`, currentType);
            } else {
                await axiosClient.post('/v1/manager/cargo-types', currentType);
            }
            setIsEditing(false);
            setCurrentType({ cargoTypeName: '' });
            fetchCargoTypes();
        } catch (error) {
            console.error('Failed to save cargo type', error.response?.data?.message || error.message);
            alert(error.response?.data?.message || 'Failed to save cargo type');
        }
    };

    const handleEdit = (type) => {
        setCurrentType(type);
        setIsEditing(true);
    };

    const handleDisable = async (id) => {
        if (!window.confirm('Are you sure you want to disable this cargo type?')) return;
        try {
            await axiosClient.patch(`/v1/manager/cargo-types/${id}/soft-delete`);
            fetchCargoTypes();
        } catch (error) {
            console.error('Failed to disable cargo type', error.response?.data?.message || error.message);
            alert(error.response?.data?.message || 'Failed to disable cargo type');
        }
    };

    const handleRestore = async (id) => {
        if (!window.confirm('Are you sure you want to restore this cargo type?')) return;
        try {
            await axiosClient.patch(`/v1/manager/cargo-types/${id}/restore`);
            fetchCargoTypes();
        } catch (error) {
            console.error('Failed to restore cargo type', error.response?.data?.message || error.message);
            alert(error.response?.data?.message || 'Failed to restore cargo type');
        }
    };

    const handleCancel = () => {
        setIsEditing(false);
        setCurrentType({ cargoTypeName: '' });
    };

    return (
        <div className="cargo-type-manager">
            <div className="cargo-type-manager-header">
                <h2 style={{ fontWeight: 'bold' }}>Quản lý loại hàng</h2>
            </div>

            <div className="cargo-type-manager-content">
                <div className="cargo-type-form-container">
                    <h3>{isEditing ? 'Edit Cargo Type' : 'Add New Cargo Type'}</h3>
                    <form onSubmit={handleSave} className="cargo-type-form">
                        <div className="form-group">
                            <label>Tên Loại Hàng</label>
                            <input
                                type="text"
                                name="cargoTypeName"
                                value={currentType.cargoTypeName || ''}
                                onChange={handleInputChange}
                                required
                                placeholder="VD: Hàng dễ vỡ, Hàng cồng kềnh..."
                            />
                        </div>
                        <div className="form-actions">
                            <button type="submit" className="save-btn">{isEditing ? 'Update' : 'Create'}</button>
                            {isEditing && <button type="button" className="cancel-btn" onClick={handleCancel}>Cancel</button>}
                        </div>
                    </form>
                </div>

                <div className="cargo-type-list-container">
                    <div className="cargo-type-list-header">
                        <h3>Danh sách loại hàng</h3>
                        <input
                            type="text"
                            placeholder="Tìm kiếm loại hàng..."
                            value={search}
                            onChange={handleSearchChange}
                            className="search-input"
                        />
                    </div>

                    {loading ? (
                        <p>Loading cargo types...</p>
                    ) : (
                        <div className="table-responsive">
                            <table className="cargo-type-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Tên loại hàng</th>
                                        <th>Trạng thái</th>
                                        <th>Hành động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {cargoTypes.length > 0 ? cargoTypes.map(type => (
                                        <tr key={type.cargoTypeId}>
                                            <td>{type.cargoTypeId}</td>
                                            <td>{type.cargoTypeName}</td>
                                            <td>
                                                <span className={`status-badge ${type.active ? "active" : "inactive"}`}>
                                                    {type.active ? "Active" : "Inactive"}
                                                </span>
                                            </td>
                                            <td className="actions-cell">
                                                <button onClick={() => handleEdit(type)} className="edit-btn">Edit</button>
                                                {type.active ? (
                                                    <button onClick={() => handleDisable(type.cargoTypeId)} className="disable-btn">Disable</button>
                                                ) : (
                                                    <button onClick={() => handleRestore(type.cargoTypeId)} className="restore-btn">Restore</button>
                                                )}
                                            </td>
                                        </tr>
                                    )) : (
                                        <tr>
                                            <td colSpan="4" className="text-center">Không tìm thấy loại hàng nào.</td>
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

export default CargoTypeManager;
