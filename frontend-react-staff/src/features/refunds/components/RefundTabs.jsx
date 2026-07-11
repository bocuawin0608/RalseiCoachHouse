import { Nav } from 'react-bootstrap';

export default function RefundTabs({ activeTab, onTabChange }) {
    return (
        <Nav variant="tabs" className="mb-3 border-0">
            <Nav.Item>
                <Nav.Link
                    active={activeTab === 'passenger'}
                    onClick={() => onTabChange('passenger')}
                    style={{ cursor: 'pointer' }}
                >
                    Hành khách
                </Nav.Link>
            </Nav.Item>
            <Nav.Item>
                <Nav.Link
                    active={activeTab === 'cargo'}
                    onClick={() => onTabChange('cargo')}
                    style={{ cursor: 'pointer' }}
                >
                    Hàng hóa
                </Nav.Link>
            </Nav.Item>
        </Nav>
    );
}
