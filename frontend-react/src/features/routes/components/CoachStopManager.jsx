import React, { useState, useEffect } from 'react';
import axiosClient from '../../../api/axiosClient';
import './CoachStopManager.css';

const CoachStopManager = ({ onBack }) => {
    const [stops, setStops] = useState([]);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);

    const [isEditing, setIsEditing] = useState(false);
    const [currentStop, setCurrentStop] = useState({
        stopPointName: '',
        address: ''
    });

    const fetchStops = async () => {
        setLoading(true);
        try {
            const data = await axiosClient.get('/v1/coach-stops', {
                params: {
                    search,
                    page,
                    size: 5
                }
            });
            setStops(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (error) {
            console.error('Failed to fetch coach stops', error.response.data.message);
            alert(error.response.data.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchStops();
    }, [page, search]);

    const handleSearchChange = (e) => {
        setSearch(e.target.value);
        setPage(0);
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setCurrentStop(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSave = async (e) => {
        e.preventDefault();
        try {
            if (isEditing && currentStop.stopPointId) {
                await axiosClient.put(`/v1/coach-stops/${currentStop.stopPointId}`, currentStop);
            } else {
                await axiosClient.post('/v1/coach-stops', currentStop);
            }
            setIsEditing(false);
            setCurrentStop({ stopPointName: '', address: '' });
            fetchStops();
        } catch (error) {
            console.error('Failed to save coach stop', error.response.data.message);
            alert(error.response.data.message);
        }
    };

    const handleEdit = (stop) => {
        setCurrentStop(stop);
        setIsEditing(true);
    };

    const handleDisable = async (id) => {
        if (!window.confirm('Are you sure you want to disable this stop?')) return;
        try {
            await axiosClient.patch(`/v1/coach-stops/${id}/soft-delete`);
            fetchStops();
        } catch (error) {
            console.error('Failed to disable coach stop', error.response.data.message);
            alert(error.response.data.message);
        }
    };

    const handleRestore = async (id) => {
        if (!window.confirm('Are you sure you want to restore this stop?')) return;
        try {
            await axiosClient.patch(`/v1/coach-stops/${id}/restore`);
            fetchStops();
        } catch (error) {
            console.error('Failed to restore coach stop', error.response.data.message);
            alert(error.response.data.message);
        }
    };

    const handleCancel = () => {
        setIsEditing(false);
        setCurrentStop({ stopPointName: '', address: '' });
    };

    return (
        <div className="coach-stop-manager">
            <div className="coach-stop-manager-header">
                <h2 style={{ fontWeight: 'bold' }}>Quản lý điểm dừng</h2>
            </div>

            <div className="coach-stop-manager-content">
                <div className="coach-stop-form-container">
                    <h3>{isEditing ? 'Edit Stop' : 'Add New Stop'}</h3>
                    <form onSubmit={handleSave} className="coach-stop-form">
                        <div className="form-group">
                            <label>Stop Name</label>
                            <input
                                type="text"
                                name="stopPointName"
                                value={currentStop.stopPointName || ''}
                                onChange={handleInputChange}
                                required
                                placeholder="e.g. My Dinh Bus Station"
                            />
                        </div>
                        <div className="form-group">
                            <label>Address</label>
                            <input
                                type="text"
                                name="address"
                                value={currentStop.address || ''}
                                onChange={handleInputChange}
                                required
                                placeholder="e.g. 20 Pham Hung"
                            />
                        </div>
                        <div className="form-actions">
                            <button type="submit" className="save-btn">{isEditing ? 'Update' : 'Create'}</button>
                            {isEditing && <button type="button" className="cancel-btn" onClick={handleCancel}>Cancel</button>}
                        </div>
                    </form>
                </div>

                <div className="coach-stop-list-container">
                    <div className="coach-stop-list-header">
                        <h3>Stops List</h3>
                        <input
                            type="text"
                            placeholder="Search stops..."
                            value={search}
                            onChange={handleSearchChange}
                            className="search-input"
                        />
                    </div>

                    {loading ? (
                        <p>Loading stops...</p>
                    ) : (
                        <div className="table-responsive">
                            <table className="coach-stop-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Name</th>
                                        <th>Address</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {stops.length > 0 ? stops.map(stop => (
                                        <tr key={stop.stopPointId}>
                                            <td>{stop.stopPointId}</td>
                                            <td>{stop.stopPointName}</td>
                                            <td>{stop.address}</td>
                                            <td>
                                                <span className={`status-badge ${stop.active ? "active" : "inactive"}`}>
                                                    {stop.active ? "Active" : "Inactive"}
                                                </span>
                                            </td>
                                            <td className="actions-cell">
                                                <button onClick={() => handleEdit(stop)} className="edit-btn">Edit</button>
                                                {stop.active ? (
                                                    <button onClick={() => handleDisable(stop.stopPointId)} className="disable-btn">Disable</button>
                                                ) : (
                                                    <button onClick={() => handleRestore(stop.stopPointId)} className="restore-btn">Restore</button>
                                                )}
                                            </td>
                                        </tr>
                                    )) : (
                                        <tr>
                                            <td colSpan="5" className="text-center">No stops found.</td>
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

export default CoachStopManager;
