import React, { useState, useEffect } from 'react';
import axiosClient from '../../api/axiosClient';
import './RouteManager.css'; // Optional: if we want specific styling

const RouteManager = ({ onBack }) => {
    const [routes, setRoutes] = useState([]);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);

    const [isEditing, setIsEditing] = useState(false);
    const [currentRoute, setCurrentRoute] = useState({
        routeName: '',
        totalKilometers: '',
        totalMinutes: '',
        active: true
    });

    const fetchRoutes = async () => {
        setLoading(true);
        try {
            const data = await axiosClient.get('/v1/routes', {
                params: {
                    search,
                    page,
                    size: 5
                }
            });
            setRoutes(data.content || []);
            setTotalPages(data.totalPages || 0);
        } catch (error) {
            console.error('Failed to fetch routes', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchRoutes();
    }, [page, search]);

    const handleSearchChange = (e) => {
        setSearch(e.target.value);
        setPage(0);
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setCurrentRoute(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSave = async (e) => {
        e.preventDefault();
        try {
            if (isEditing && currentRoute.routeId) {
                await axiosClient.put(`/v1/routes/${currentRoute.routeId}`, currentRoute);
            } else {
                await axiosClient.post('/v1/routes', currentRoute);
            }
            setIsEditing(false);
            setCurrentRoute({ routeName: '', totalKilometers: '', totalMinutes: '', active: true });
            fetchRoutes();
        } catch (error) {
            console.error('Failed to save route', error);
            alert('Failed to save route. Check console for details.');
        }
    };

    const handleEdit = (route) => {
        setCurrentRoute(route);
        setIsEditing(true);
    };

    const handleDisable = async (id) => {
        if (!window.confirm('Are you sure you want to disable this route?')) return;
        try {
            await axiosClient.patch(`/v1/routes/${id}/soft-delete`);
            fetchRoutes();
        } catch (error) {
            console.error('Failed to disable route', error);
            alert('Failed to disable route. Check console for details.');
        }
    };

    const handleRestore = async (id) => {
        if (!window.confirm('Are you sure you want to restore this route?')) return;
        try {
            await axiosClient.patch(`/v1/routes/${id}/restore`);
            fetchRoutes();
        } catch (error) {
            console.error('Failed to restore route', error);
            alert('Failed to restore route. Check console for details.');
        }
    };

    const handleCancel = () => {
        setIsEditing(false);
        setCurrentRoute({ routeName: '', totalKilometers: '', totalMinutes: '', active: true });
    };

    return (
        <div className="route-manager">
            <div className="route-manager-header">
                <button className="back-btn" onClick={onBack}>&larr; Back to Dashboard</button>
                <h2>Route Management</h2>
            </div>

            <div className="route-manager-content">
                <div className="route-form-container">
                    <h3>{isEditing ? 'Edit Route' : 'Add New Route'}</h3>
                    <form onSubmit={handleSave} className="route-form">
                        <div className="form-group">
                            <label>Route Name</label>
                            <input
                                type="text"
                                name="routeName"
                                value={currentRoute.routeName}
                                onChange={handleInputChange}
                                required
                                placeholder="e.g. Hanoi - Sapa"
                            />
                        </div>
                        <div className="form-group">
                            <label>Distance (km)</label>
                            <input
                                type="number"
                                step="0.1"
                                name="totalKilometers"
                                value={currentRoute.totalKilometers}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label>Duration (minutes)</label>
                            <input
                                type="number"
                                name="totalMinutes"
                                value={currentRoute.totalMinutes}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-actions">
                            <button type="submit" className="save-btn">{isEditing ? 'Update' : 'Create'}</button>
                            {isEditing && <button type="button" className="cancel-btn" onClick={handleCancel}>Cancel</button>}
                        </div>
                    </form>
                </div>

                <div className="route-list-container">
                    <div className="route-list-header">
                        <h3>Routes List</h3>
                        <input
                            type="text"
                            placeholder="Search routes..."
                            value={search}
                            onChange={handleSearchChange}
                            className="search-input"
                        />
                    </div>

                    {loading ? (
                        <p>Loading routes...</p>
                    ) : (
                        <div className="table-responsive">
                            <table className="route-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Name</th>
                                        <th>Distance</th>
                                        <th>Duration</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {routes.length > 0 ? routes.map(route => (
                                        <tr key={route.routeId}>
                                            <td>{route.routeId}</td>
                                            <td>{route.routeName}</td>
                                            <td>{route.totalKilometers} km</td>
                                            <td>{route.totalMinutes} min</td>
                                            <td>
                                                <span className={`status-badge ${route.active ? "active" : "inactive"}`}>
                                                    {route.active ? "Active" : "Inactive"}
                                                </span>
                                            </td>
                                            <td className="actions-cell">
                                                <button onClick={() => handleEdit(route)} className="edit-btn">Edit</button>
                                                {route.active ? (
                                                    <button onClick={() => handleDisable(route.routeId)} className="disable-btn">Disable</button>
                                                ) : (
                                                    <button onClick={() => handleRestore(route.routeId)} className="restore-btn">Restore</button>
                                                )}
                                            </td>
                                        </tr>
                                    )) : (
                                        <tr>
                                            <td colSpan="6" className="text-center">No routes found.</td>
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

export default RouteManager;
