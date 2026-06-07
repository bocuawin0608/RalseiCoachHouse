import React, { useState } from 'react';
import './AdminDashboard.css';
import { CoachStopManager, RouteManager } from '../../../features/routes';

const AdminDashboard = () => {
    const [activeScreen, setActiveScreen] = useState('schedule-screen');

    const screens = {
        'schedule-screen': (
            <div id="schedule-screen" className="screen">
                <div className="screen-header">
                    <h1 className="screen-title">Trip Scheduling</h1>
                    <p className="screen-subtitle">Plan and monitor upcoming trips across all routes.</p>
                </div>
                <div className="dashboard-cards">
                    <div className="card">
                        <div className="card-icon">📅</div>
                        <h3 className="card-title">Schedule New Trip</h3>
                        <p className="card-desc">Assign vehicles and drivers to specific routes for upcoming dates.</p>
                    </div>
                    <div className="card">
                        <div className="card-icon">👁️</div>
                        <h3 className="card-title">Active Trips</h3>
                        <p className="card-desc">Monitor trips currently on the road and their live status.</p>
                    </div>
                </div>
            </div>
        ),
        'fleet-screen': (
            <div id="fleet-screen" className="screen">
                <div className="screen-header">
                    <h1 className="screen-title">Vehicle Fleet</h1>
                    <p className="screen-subtitle">Manage buses, maintenance schedules, and vehicle assignments.</p>
                </div>
                <div className="dashboard-cards">
                    <div className="card">
                        <div className="card-icon">🚌</div>
                        <h3 className="card-title">Fleet Directory</h3>
                        <p className="card-desc">View all registered vehicles, capacities, and current condition.</p>
                    </div>
                    <div className="card">
                        <div className="card-icon">🔧</div>
                        <h3 className="card-title">Maintenance Logs</h3>
                        <p className="card-desc">Track repairs, upcoming services, and inspection dates.</p>
                    </div>
                </div>
            </div>
        ),
        'route-screen': (
            <div id="route-screen" className="screen">
                <div className="screen-header">
                    <h1 className="screen-title">Route Network</h1>
                    <p className="screen-subtitle">Configure operational routes, stops, and pricing details.</p>
                </div>
                <div className="dashboard-cards">
                    <div className="card" onClick={() => setActiveScreen('route-manager')} style={{ cursor: 'pointer' }}>
                        <div className="card-icon">🗺️</div>
                        <h3 className="card-title">Route Map</h3>
                        <p className="card-desc">Manage origin and destination points across the service area.</p>
                    </div>
                    <div className="card" onClick={() => setActiveScreen('coach-stop-manager')} style={{ cursor: 'pointer' }}>
                        <div className="card-icon">📍</div>
                        <h3 className="card-title">Pickup/Dropoff Points</h3>
                        <p className="card-desc">Configure waypoints and intermediary stops for each route.</p>
                    </div>
                </div>
            </div>
        ),
        'cargo-screen': (
            <div id="cargo-screen" className="screen">
                <div className="screen-header">
                    <h1 className="screen-title">Cargo Catalog</h1>
                    <p className="screen-subtitle">Track packages, freight assignments, and delivery statuses.</p>
                </div>
                <div className="dashboard-cards">
                    <div className="card">
                        <div className="card-icon">📦</div>
                        <h3 className="card-title">Parcel Tracking</h3>
                        <p className="card-desc">Search and update status for customer packages in transit.</p>
                    </div>
                    <div className="card">
                        <div className="card-icon">⚖️</div>
                        <h3 className="card-title">Freight Rates</h3>
                        <p className="card-desc">Configure pricing tiers based on weight, dimensions, and distance.</p>
                    </div>
                </div>
            </div>
        ),
        'finance-screen': (
            <div id="finance-screen" className="screen">
                <div className="screen-header">
                    <h1 className="screen-title">Finance</h1>
                    <p className="screen-subtitle">Overview of revenue, expenses, and financial reporting.</p>
                </div>
                <div className="dashboard-cards">
                    <div className="card">
                        <div className="card-icon">💰</div>
                        <h3 className="card-title">Revenue Dashboard</h3>
                        <p className="card-desc">Analyze ticket sales and cargo income over time.</p>
                    </div>
                    <div className="card">
                        <div className="card-icon">📊</div>
                        <h3 className="card-title">Expense Reports</h3>
                        <p className="card-desc">Track fuel costs, maintenance expenses, and payroll.</p>
                    </div>
                </div>
            </div>
        ),
        'voucher-screen': (
            <div id="voucher-screen" className="screen">
                <div className="screen-header">
                    <h1 className="screen-title">Vouchers & Promos</h1>
                    <p className="screen-subtitle">Create and manage discount codes and promotional campaigns.</p>
                </div>
                <div className="dashboard-cards">
                    <div className="card">
                        <div className="card-icon">🎫</div>
                        <h3 className="card-title">Active Promos</h3>
                        <p className="card-desc">View currently running discounts and usage statistics.</p>
                    </div>
                    <div className="card">
                        <div className="card-icon">✨</div>
                        <h3 className="card-title">Create Campaign</h3>
                        <p className="card-desc">Generate new discount codes for holidays or special events.</p>
                    </div>
                </div>
            </div>
        ),
        'route-manager': <RouteManager onBack={() => setActiveScreen('route-screen')} />,
        'coach-stop-manager': <CoachStopManager onBack={() => setActiveScreen('route-screen')} />
    };

    return (
        <div className="admin-dashboard-wrapper">
            <header>
                <div className="header-content">
                    <div className="brand">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2" /><circle cx="7" cy="17" r="2" /><path d="M9 17h6" /><circle cx="17" cy="17" r="2" /></svg>
                        Admin Dashboard
                    </div>

                    <nav id="navMenu">
                        <button className={`nav-btn ${activeScreen === 'schedule-screen' ? 'active' : ''}`} onClick={() => setActiveScreen('schedule-screen')}>Manage Trip Scheduling</button>
                        <button className={`nav-btn ${activeScreen === 'fleet-screen' ? 'active' : ''}`} onClick={() => setActiveScreen('fleet-screen')}>Manage Vehicle Fleet</button>
                        <button className={`nav-btn ${activeScreen === 'route-screen' ? 'active' : ''}`} onClick={() => setActiveScreen('route-screen')}>Manage Route Network</button>
                        <button className={`nav-btn ${activeScreen === 'cargo-screen' ? 'active' : ''}`} onClick={() => setActiveScreen('cargo-screen')}>Manage Cargo Catalog</button>
                        <button className={`nav-btn ${activeScreen === 'finance-screen' ? 'active' : ''}`} onClick={() => setActiveScreen('finance-screen')}>Manage Finance</button>
                        <button className={`nav-btn ${activeScreen === 'voucher-screen' ? 'active' : ''}`} onClick={() => setActiveScreen('voucher-screen')}>Manage Vouchers</button>
                    </nav>
                </div>
            </header>

            <main>
                {screens[activeScreen]}
            </main>
        </div>
    );
};

export default AdminDashboard;
