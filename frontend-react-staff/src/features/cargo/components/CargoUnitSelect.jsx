import { Form } from 'react-bootstrap';
import { CARGO_UNITS } from '../constants/cargoUnits';

/**
 * Shared unit dropdown for cargo type / cargo type price forms.
 * Keeps a legacy value selectable when editing older free-text units.
 */
export default function CargoUnitSelect({
    name = 'unit',
    value,
    onChange,
    required = true,
    className = 'py-2',
    label = null
}) {
    const current = value || '';
    const options = CARGO_UNITS.includes(current) || !current
        ? CARGO_UNITS
        : [current, ...CARGO_UNITS];

    return (
        <>
            {label}
            <Form.Select
                name={name}
                value={current}
                onChange={onChange}
                required={required}
                className={className}
            >
                <option value="">Chọn đơn vị</option>
                {options.map((unit) => (
                    <option key={unit} value={unit}>{unit}</option>
                ))}
            </Form.Select>
        </>
    );
}
