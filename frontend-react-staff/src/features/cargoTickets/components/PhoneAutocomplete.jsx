import { useState, useEffect, useRef } from 'react';
import { Form, Dropdown as BootstrapDropdown } from 'react-bootstrap';
import { cargoTicketApi } from '../api/cargoTicketApi';

export default function PhoneAutocomplete({ label, prefix, data, onChange, setFormData }) {
    const [suggestions, setSuggestions] = useState([]);
    const [show, setShow] = useState(false);
    const skipSearch = useRef(false);

    const phone = data[`${prefix}Phone`];

    useEffect(() => {
        if (!phone || phone.length < 3) {
            setSuggestions([]);
            setShow(false);
            return;
        }
        if (skipSearch.current) {
            skipSearch.current = false;
            return;
        }
        const timer = setTimeout(async () => {
            try {
                const res = await cargoTicketApi.searchContacts(phone);
                setSuggestions(res || []);
                setShow(true);
            } catch (err) {
                console.error(err);
            }
        }, 300);
        return () => clearTimeout(timer);
    }, [phone]);

    const handleSelect = (contact) => {
        skipSearch.current = true;
        setFormData(prev => ({
            ...prev,
            [`${prefix}Phone`]: contact.phone,
            [`${prefix}Name`]: contact.name,
        }));
        setShow(false);
    };

    const handleChange = (e) => {
        onChange(e);
        if (e.target.value.length < 3) {
            setShow(false);
        }
    };

    return (
        <Form.Group className="position-relative">
            <Form.Label className="fw-semibold">{label} *</Form.Label>
            <Form.Control
                name={`${prefix}Phone`}
                value={phone}
                onChange={handleChange}
                required
                maxLength={20}
                onBlur={() => setTimeout(() => setShow(false), 200)}
                onFocus={() => { if (suggestions.length > 0) setShow(true) }}
            />
            {show && suggestions.length > 0 && (
                <div className="position-absolute w-100 bg-white border rounded shadow-sm mt-1 z-3" style={{ maxHeight: '200px', overflowY: 'auto' }}>
                    <BootstrapDropdown.Menu show className="w-100 position-static border-0 shadow-none m-0">
                        {suggestions.map((c, idx) => (
                            <BootstrapDropdown.Item key={idx} onClick={() => handleSelect(c)}>
                                <strong>{c.name || 'Không tên'}</strong> - {c.phone}
                            </BootstrapDropdown.Item>
                        ))}
                    </BootstrapDropdown.Menu>
                </div>
            )}
        </Form.Group>
    );
}
