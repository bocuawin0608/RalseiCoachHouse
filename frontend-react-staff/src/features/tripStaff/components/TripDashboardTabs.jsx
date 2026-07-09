import { Nav } from 'react-bootstrap';

export default function TripDashboardTabs({ activeTab, onChange }) {
    return (
        <Nav variant="tabs" className="mb-3">
            <Nav.Item>
                <Nav.Link active={activeTab === 'passenger'} onClick={() => onChange('passenger')}>
                    Hành khách
                </Nav.Link>
            </Nav.Item>
            <Nav.Item>
                <Nav.Link active={activeTab === 'cargo'} onClick={() => onChange('cargo')}>
                    Hàng hóa
                </Nav.Link>
            </Nav.Item>
        </Nav>
    );
}
