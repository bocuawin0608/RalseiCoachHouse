import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { tripService } from '../../../features/trips/api/tripServices';
import './HomePage.css';
import { buildTripInfoFromSearchCard } from '../../../features/booking';

const SEARCH_HISTORY_COOKIE = 'ralsei_trip_search_history';
const MAX_SEARCH_HISTORY = 4;
const TRIPS_PER_PAGE = 5;
const TRIP_TYPE = {
    ONE_WAY: 'ONE_WAY',
    ROUND_TRIP: 'ROUND_TRIP',
};
const JOURNEY_LEG = {
    OUTBOUND: 'OUTBOUND',
    RETURN: 'RETURN',
};
const FALLBACK_LOCATIONS = ['Hà Nội', 'Quảng Bình'];
const VEHICLE_FILTERS = [
    { label: 'Xe Limousine VIP 20 phòng', value: 'Limousine' },
    { label: 'Xe Giường Nằm Luxury 32 chỗ', value: 'luxury' },
    { label: 'Xe Khách Truyền Thống 38 chỗ', value: 'truyền thống' },
];
const TIME_SLOT_FILTERS = [
    ['00:00-06:00', 'Sáng sớm 00:00 - 06:00'],
    ['06:00-12:00', 'Buổi sáng 06:00 - 12:00'],
    ['12:00-18:00', 'Buổi chiều 12:00 - 18:00'],
    ['18:00-23:59', 'Buổi tối 18:00 - 24:00'],
];
const COACH_IMAGES = {
    luxury: '/images/xe-luxury.jpeg',
    limousine: '/images/xe-vip.jpg',
    standard: '/images/xe-thuong.jpg',
};

/**
 * Lightweight SVG icon set for the customer trip flow.
 * Text emoji icons were leaking platform-specific rendering into the UI.
 */
const SvgIcon = ({ name, className = '' }) => {
    const icons = {
        pin: (
            <path d="M12 21s7-5.1 7-11a7 7 0 1 0-14 0c0 5.9 7 11 7 11Zm0-8.5A2.5 2.5 0 1 0 12 7a2.5 2.5 0 0 0 0 5.5Z" />
        ),
        calendar: (
            <path d="M7 2v3M17 2v3M4 9h16M6 4h12a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Zm3 9h2v2H9v-2Zm4 0h2v2h-2v-2Z" />
        ),
        swap: (
            <path d="M7 7h11m0 0-3-3m3 3-3 3M17 17H6m0 0 3 3m-3-3 3-3" />
        ),
        clock: (
            <path d="M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Zm0-15v5l3 2" />
        ),
        close: (
            <path d="m6 6 12 12M18 6 6 18" />
        ),
        previous: (
            <path d="m15 18-6-6 6-6" />
        ),
        next: (
            <path d="m9 18 6-6-6-6" />
        ),
    };

    return (
        <svg className={`svg-icon ${className}`} viewBox="0 0 24 24" aria-hidden="true">
            {icons[name]}
        </svg>
    );
};

/**
 * Reads a JSON cookie safely so a malformed browser value cannot break the page.
 */
const readSearchHistory = () => {
    const cookie = document.cookie
        .split('; ')
        .find((item) => item.startsWith(`${SEARCH_HISTORY_COOKIE}=`));

    if (!cookie) return [];

    try {
        return JSON.parse(decodeURIComponent(cookie.split('=')[1]));
    } catch {
        return [];
    }
};

/**
 * Persists recent trip searches for quick reuse on the first screen.
 */
const writeSearchHistory = (history) => {
    const expires = new Date();
    expires.setDate(expires.getDate() + 30);
    document.cookie = `${SEARCH_HISTORY_COOKIE}=${encodeURIComponent(JSON.stringify(history))}; expires=${expires.toUTCString()}; path=/; SameSite=Lax`;
};

/**
 * Removes administrative prefixes so the dropdown does not show duplicate
 * concepts such as "Hà Nội" and "Thành phố Hà Nội" as separate choices.
 */
const normalizeLocationName = (value = '') => {
    return value
        .replace(/^Thành phố\s+/i, '')
        .replace(/^Tỉnh\s+/i, '')
        .trim();
};

/**
 * Converts route dropdown rows into unique searchable locations.
 */
const extractLocationsFromRoutes = (routes) => {
    const locations = new Set(FALLBACK_LOCATIONS);
    routes.forEach((route) => {
        const routeName = route.routeName || '';
        routeName.split('-').forEach((part) => {
            const location = normalizeLocationName(part);
            if (location) locations.add(location);
        });
    });
    return Array.from(locations).sort((first, second) => first.localeCompare(second, 'vi'));
};

/**
 * Normalizes backend timestamps before displaying HH:mm to the customer.
 */
const formatTime = (value) => {
    if (!value) return '--:--';
    if (value.includes('T')) return value.split('T')[1].substring(0, 5);
    if (value.includes(' ')) return value.split(' ')[1].substring(0, 5);
    return value.substring(0, 5);
};

/**
 * Forecasts the arrival timestamp on the client when an older backend response
 * does not yet include arrivalTime.
 */
const forecastArrivalTime = (departureTime) => {
    if (!departureTime) return null;
    const normalizedDeparture = departureTime.includes('T') ? departureTime : departureTime.replace(' ', 'T');
    const parsedDeparture = new Date(normalizedDeparture);
    if (Number.isNaN(parsedDeparture.getTime())) return null;
    parsedDeparture.setMinutes(parsedDeparture.getMinutes() + 432);
    const year = parsedDeparture.getFullYear();
    const month = String(parsedDeparture.getMonth() + 1).padStart(2, '0');
    const day = String(parsedDeparture.getDate()).padStart(2, '0');
    const hour = String(parsedDeparture.getHours()).padStart(2, '0');
    const minute = String(parsedDeparture.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hour}:${minute}:00`;
};

/**
 * Returns the browser-local ISO date so date inputs do not drift around UTC.
 */
const getLocalDateInputValue = () => {
    const now = new Date();
    const timezoneOffset = now.getTimezoneOffset() * 60000;
    return new Date(now.getTime() - timezoneOffset).toISOString().slice(0, 10);
};

/**
 * Keeps date display consistent in the search history cards.
 */
const formatDate = (value) => {
    if (!value) return '';
    const [year, month, day] = value.split('-');
    return `${day}/${month}/${year}`;
};

/**
 * Formats the active result tab date in the same Vietnamese style as the
 * booking sample.
 */
const formatJourneyDate = (value) => {
    if (!value) return '';
    const parsedDate = new Date(`${value}T00:00:00`);
    if (Number.isNaN(parsedDate.getTime())) return formatDate(value);
    const formattedDate = parsedDate.toLocaleDateString('vi-VN', {
        weekday: 'long',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    });
    return formattedDate.charAt(0).toUpperCase() + formattedDate.slice(1);
};

/**
 * Maps a real backend coach type name to the prepared public coach image.
 */
const getCoachImage = (coachTypeName = '') => {
    const normalizedName = coachTypeName.toLowerCase();
    if (normalizedName.includes('limousine') || normalizedName.includes('vip')) return COACH_IMAGES.limousine;
    if (normalizedName.includes('luxury')) return COACH_IMAGES.luxury;
    if (normalizedName.includes('truyền thống') || normalizedName.includes('khách')) return COACH_IMAGES.standard;
    return null;
};

/**
 * Gives the seat description from the actual backend type/seat count.
 */
const getCoachDescription = (trip) => {
    const seats = trip.totalSeats || trip.availableSeats;
    if (seats) return `Ghế giường nằm ${seats}`;
    return trip.coachTypeName?.toLowerCase().includes('thường') ? 'Ghế nằm tiêu chuẩn' : 'Ghế nằm cao cấp';
};

/**
 * Searchable dropdown used by departure and destination fields.
 */
const LocationCombobox = ({ id, label, value, options, iconTone, onChange }) => {
    const [open, setOpen] = useState(false);
    const visibleOptions = options
        .filter((option) => option.toLowerCase().includes(normalizeLocationName(value).toLowerCase()))
        .slice(0, 8);

    return (
        <div className="input-block location-combobox">
            <span className={`input-svg-bubble ${iconTone}`}>
                <SvgIcon name="pin" />
            </span>
            <div className="input-field-wrapper">
                <label htmlFor={id}>{label}</label>
                <input
                    id={id}
                    type="text"
                    value={value}
                    onFocus={() => setOpen(true)}
                    onBlur={() => window.setTimeout(() => setOpen(false), 120)}
                    onChange={(event) => {
                        onChange(normalizeLocationName(event.target.value));
                        setOpen(true);
                    }}
                    placeholder={`Chọn ${label.toLowerCase()}`}
                    autoComplete="off"
                    required
                />
                {open && visibleOptions.length > 0 && (
                    <div className="location-dropdown" role="listbox">
                        {visibleOptions.map((option) => (
                            <button
                                key={`${id}-${option}`}
                                type="button"
                                className="location-option"
                                onMouseDown={(event) => event.preventDefault()}
                                onClick={() => {
                                    onChange(option);
                                    setOpen(false);
                                }}
                            >
                                {option}
                            </button>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

const HomePage = () => {
    const navigate = useNavigate();
    const today = getLocalDateInputValue();

    const [tripType, setTripType] = useState(TRIP_TYPE.ONE_WAY);
    const [departure, setDeparture] = useState('');
    const [destination, setDestination] = useState('');
    const [date, setDate] = useState('');
    const [returnDate, setReturnDate] = useState('');
    const [routes, setRoutes] = useState([]);
    const [searchHistory, setSearchHistory] = useState([]);
    const [trips, setTrips] = useState([]);
    const [loading, setLoading] = useState(false);
    const [hasSearched, setHasSearched] = useState(false);
    const [resultMessage, setResultMessage] = useState('');
    const [currentSearchRoute, setCurrentSearchRoute] = useState('');
    const [activeJourneyLeg, setActiveJourneyLeg] = useState(JOURNEY_LEG.OUTBOUND);
    const [pagination, setPagination] = useState({ pageNumber: 0, totalElements: 0, totalPages: 0 });
    const [stopModal, setStopModal] = useState({ trip: null, stops: [], loading: false, error: '' });

    const [selectedTimeSlots, setSelectedTimeSlots] = useState([]);
    const [selectedLayouts, setSelectedLayouts] = useState([]);
    const [priceRange, setPriceRange] = useState({ min: null, max: null });

    useEffect(() => {
        setSearchHistory(readSearchHistory());
        tripService
            .getRouteDropdown()
            .then(setRoutes)
            .catch(() => setRoutes([]));
    }, []);

    useEffect(() => {
        if (!stopModal.trip) return undefined;

        const previousOverflow = document.body.style.overflow;
        const closeOnEscape = (event) => {
            if (event.key === 'Escape') {
                setStopModal({ trip: null, stops: [], loading: false, error: '' });
            }
        };

        document.body.style.overflow = 'hidden';
        document.addEventListener('keydown', closeOnEscape);
        return () => {
            document.body.style.overflow = previousOverflow;
            document.removeEventListener('keydown', closeOnEscape);
        };
    }, [stopModal.trip]);

    const locationOptions = useMemo(() => extractLocationsFromRoutes(routes), [routes]);
    const departureOptions = locationOptions;
    const destinationOptions = useMemo(
        () => locationOptions.filter((location) => location !== departure),
        [departure, locationOptions]
    );

    /**
     * Updates the departure and clears destination only when the two would collide.
     */
    const handleDepartureChange = (nextDeparture) => {
        const normalizedDeparture = normalizeLocationName(nextDeparture);
        setDeparture(normalizedDeparture);
        if (normalizedDeparture && normalizedDeparture === destination) {
            setDestination('');
        }
    };

    /**
     * Updates the destination and rejects the currently selected departure.
     */
    const handleDestinationChange = (nextDestination) => {
        const normalizedDestination = normalizeLocationName(nextDestination);
        if (normalizedDestination === departure) {
            setDestination('');
            return;
        }
        setDestination(normalizedDestination);
    };

    /**
     * Keeps the exact date selected by the customer. Validation must report an
     * invalid date instead of silently replacing it with another date.
     */
    const preserveDate = (nextDate) => nextDate || '';

    const blockDateKeyboardEditing = (event) => {
        if (!['Tab', 'Enter'].includes(event.key)) {
            event.preventDefault();
        }
    };

    /**
     * Saves a successful search at the front of the cookie-backed history list.
     */
    const saveSearchHistory = (routeText) => {
        const nextItem = {
            route: routeText,
            departure,
            destination,
            date,
            returnDate: tripType === TRIP_TYPE.ROUND_TRIP ? returnDate : '',
            tripType,
        };
        const nextHistory = [
            nextItem,
            ...searchHistory.filter(
                (item) => `${item.route}-${item.date}-${item.returnDate}` !== `${nextItem.route}-${nextItem.date}-${nextItem.returnDate}`
            ),
        ].slice(0, MAX_SEARCH_HISTORY);

        setSearchHistory(nextHistory);
        writeSearchHistory(nextHistory);
    };

    /**
     * Resolves the real route/date used by the current result tab. Return trips
     * must query the reversed route on the return date, not reuse outbound data.
     */
    const resolveSearchContext = (journeyLeg) => {
        const isReturnLeg = journeyLeg === JOURNEY_LEG.RETURN && tripType === TRIP_TYPE.ROUND_TRIP;
        return {
            searchDeparture: isReturnLeg ? destination : departure,
            searchDestination: isReturnLeg ? departure : destination,
            searchDate: isReturnLeg ? returnDate : date,
        };
    };

    /**
     * Executes the customer trip search and reuses the same API for filter changes.
     */
    const executeSearch = async (
        isAdvancedSearch = false,
        updatedFilters = {},
        journeyLeg = activeJourneyLeg,
        pageNumber = 0,
        searchOverride = null
    ) => {
        const { searchDeparture, searchDestination, searchDate } = searchOverride || resolveSearchContext(journeyLeg);
        const effectiveTripType = searchOverride?.tripType || tripType;
        const effectiveOutboundDate = searchOverride?.outboundDate || date;
        const effectiveReturnDate = searchOverride?.returnDate ?? returnDate;
        if (!searchDate || !searchDeparture || !searchDestination || searchDeparture === searchDestination) return;
        if (effectiveTripType === TRIP_TYPE.ROUND_TRIP
            && (!effectiveReturnDate || effectiveReturnDate < effectiveOutboundDate)) return;

        setHasSearched(true);
        setActiveJourneyLeg(journeyLeg);
        const routeText = `${searchDeparture.trim()} - ${searchDestination.trim()}`;
        setCurrentSearchRoute(routeText);

        if (searchDate < today) {
            setTrips([]);
            setPagination({ pageNumber: 0, totalElements: 0, totalPages: 0 });
            setResultMessage('Ngày đã chọn nằm trong quá khứ. Không có chuyến xe để tìm kiếm.');
            return;
        }

        setLoading(true);
        setResultMessage('');

        try {
            const filters = isAdvancedSearch
                ? updatedFilters
                : { timeSlots: selectedTimeSlots, layouts: selectedLayouts, priceRange };

            const searchParams = {
                route: routeText,
                date: searchDate,
                page: pageNumber,
                size: TRIPS_PER_PAGE,
                isAdvanced: true,
                timeSlots: filters.timeSlots.join(','),
                layouts: filters.layouts.join(','),
                minPrice: filters.priceRange.min,
                maxPrice: filters.priceRange.max,
            };

            if (!searchParams.timeSlots) delete searchParams.timeSlots;
            if (!searchParams.layouts) delete searchParams.layouts;
            if (searchParams.minPrice === null) delete searchParams.minPrice;
            if (searchParams.maxPrice === null) delete searchParams.maxPrice;

            const responseData = await tripService.searchTrips(searchParams);
            setTrips(responseData?.content || []);
            setPagination({
                pageNumber: responseData?.pageNumber ?? pageNumber,
                totalElements: responseData?.totalElements ?? 0,
                totalPages: responseData?.totalPages ?? 0,
            });
            if (!isAdvancedSearch && journeyLeg === JOURNEY_LEG.OUTBOUND) {
                saveSearchHistory(`${departure.trim()} - ${destination.trim()}`);
            }
        } catch (error) {
            setTrips([]);
            setPagination({ pageNumber: 0, totalElements: 0, totalPages: 0 });
            setResultMessage('Không thể tải lịch trình. Vui lòng thử lại.');
            console.error('Không thể tải lịch trình khách hàng:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearchSubmit = (event) => {
        event.preventDefault();
        executeSearch(false, {}, JOURNEY_LEG.OUTBOUND);
    };

    const handleTimeSlotChange = (slot) => {
        const nextSlots = selectedTimeSlots.includes(slot)
            ? selectedTimeSlots.filter((item) => item !== slot)
            : [...selectedTimeSlots, slot];
        setSelectedTimeSlots(nextSlots);
        if (hasSearched) executeSearch(true, { timeSlots: nextSlots, layouts: selectedLayouts, priceRange }, activeJourneyLeg);
    };

    const handleLayoutChange = (layout) => {
        const nextLayouts = selectedLayouts.includes(layout)
            ? selectedLayouts.filter((item) => item !== layout)
            : [...selectedLayouts, layout];
        setSelectedLayouts(nextLayouts);
        if (hasSearched) executeSearch(true, { timeSlots: selectedTimeSlots, layouts: nextLayouts, priceRange }, activeJourneyLeg);
    };

    const handlePriceRangeChange = (min, max) => {
        const nextPrice = priceRange.min === min && priceRange.max === max ? { min: null, max: null } : { min, max };
        setPriceRange(nextPrice);
        if (hasSearched) executeSearch(true, { timeSlots: selectedTimeSlots, layouts: selectedLayouts, priceRange: nextPrice }, activeJourneyLeg);
    };

    /**
     * Loads one compact result page while preserving the active journey and filters.
     */
    const handlePageChange = (pageNumber) => {
        if (loading || pageNumber < 0 || pageNumber >= pagination.totalPages) return;
        executeSearch(
            true,
            { timeSlots: selectedTimeSlots, layouts: selectedLayouts, priceRange },
            activeJourneyLeg,
            pageNumber
        );
    };

    /**
     * Opens the stop timeline and resolves route data using the selected trip id.
     */
    const openTripStops = async (trip) => {
        setStopModal({ trip, stops: [], loading: true, error: '' });
        try {
            const stops = await tripService.getTripStops(trip.tripId);
            setStopModal({ trip, stops: Array.isArray(stops) ? stops : [], loading: false, error: '' });
        } catch (error) {
            console.error('Không thể tải điểm đón trả của chuyến:', error);
            setStopModal({ trip, stops: [], loading: false, error: 'Không thể tải điểm đón trả. Vui lòng thử lại.' });
        }
    };

    /** Closes the trip-stop modal and clears its request state. */
    const closeTripStops = () => {
        setStopModal({ trip: null, stops: [], loading: false, error: '' });
    };

    const clearAllFilters = () => {
        setSelectedTimeSlots([]);
        setSelectedLayouts([]);
        setPriceRange({ min: null, max: null });
        if (hasSearched) {
            executeSearch(true, { timeSlots: [], layouts: [], priceRange: { min: null, max: null } }, activeJourneyLeg);
        }
    };

    /**
     * Restores a history item and searches immediately with its concrete values.
     * Passing an override avoids waiting for asynchronous React state updates.
     */
    const applyHistory = (item) => {
        const routeParts = (item.route || '').split('-');
        const nextDeparture = item.departure || routeParts[0]?.trim() || '';
        const nextDestination = item.destination || routeParts[1]?.trim() || '';
        const nextTripType = item.tripType || TRIP_TYPE.ONE_WAY;
        const nextDate = preserveDate(item.date);
        const nextReturnDate = preserveDate(item.returnDate);

        setTripType(nextTripType);
        setDeparture(nextDeparture);
        setDestination(nextDestination);
        setDate(nextDate);
        setReturnDate(nextReturnDate);
        setActiveJourneyLeg(JOURNEY_LEG.OUTBOUND);
        setSelectedTimeSlots([]);
        setSelectedLayouts([]);
        setPriceRange({ min: null, max: null });

        executeSearch(
            true,
            { timeSlots: [], layouts: [], priceRange: { min: null, max: null } },
            JOURNEY_LEG.OUTBOUND,
            0,
            {
                searchDeparture: nextDeparture,
                searchDestination: nextDestination,
                searchDate: nextDate,
                tripType: nextTripType,
                outboundDate: nextDate,
                returnDate: nextReturnDate,
            }
        );
    };

    const handleJourneyTabChange = (journeyLeg) => {
        executeSearch(true, { timeSlots: selectedTimeSlots, layouts: selectedLayouts, priceRange }, journeyLeg);
    };

    const swapLocations = () => {
        setDeparture(destination);
        setDestination(departure);
    };

    return (
        <div className="homepage-container">
            <div className="buddha-image-wrapper">
                <a href="https://docs.google.com/forms/d/e/1FAIpQLSeMSgtuTpv6P_T4SrOCv1S-HiuSjsoiWA22G70SSjRVGGtKGQ/viewform?usp=publish-editor" target="_blank" rel="noopener noreferrer">
                    <img className="buddha-img" src="/images/RalseiWallpaper.jpg" alt="Ralsei Banner" />
                </a>
            </div>

            <div className="search-form-wrapper">
                <form onSubmit={handleSearchSubmit} className="search-form">
                    <div className="form-tier-top">
                        <div className="radio-group" aria-label="Loại chuyến">
                            {/* <label className="radio-label">
                                <input
                                    type="radio"
                                    name="trip-type"
                                    checked={tripType === TRIP_TYPE.ONE_WAY}
                                    onChange={() => {
                                        setTripType(TRIP_TYPE.ONE_WAY);
                                        setActiveJourneyLeg(JOURNEY_LEG.OUTBOUND);
                                    }}
                                />
                                <span className="radio-checkmark"></span> Một chiều
                            </label>
                            <label className="radio-label">
                                <input
                                    type="radio"
                                    name="trip-type"
                                    checked={tripType === TRIP_TYPE.ROUND_TRIP}
                                    onChange={() => {
                                        setTripType(TRIP_TYPE.ROUND_TRIP);
                                        setActiveJourneyLeg(JOURNEY_LEG.OUTBOUND);
                                    }}
                                />
                                <span className="radio-checkmark"></span> Khứ hồi
                            </label> */}
                        </div>
                        <div className="policy-links">
                            <a href="#guide">Hướng dẫn đặt lịch trình</a>
                            <span className="v-divider"> | </span>
                            <a href="#policy">Quy định chung</a>
                        </div>
                    </div>

                    <div className={`form-tier-main ${tripType === TRIP_TYPE.ROUND_TRIP ? 'round-trip-mode' : ''}`}>
                        <LocationCombobox id="departure" label="Điểm đi" value={departure} options={departureOptions} iconTone="green" onChange={handleDepartureChange} />

                        <button type="button" className="btn-swap" onClick={swapLocations} aria-label="Đổi điểm đi và điểm đến">
                            <SvgIcon name="swap" />
                        </button>

                        <LocationCombobox id="destination" label="Điểm đến" value={destination} options={destinationOptions} iconTone="red" onChange={handleDestinationChange} />

                        <div className="input-block date-block">
                            <span className="input-svg-bubble orange">
                                <SvgIcon name="calendar" />
                            </span>
                            <div className="input-field-wrapper">
                                <label htmlFor="departure-date">Ngày đi</label>
                                <input
                                    id="departure-date"
                                    type="date"
                                    min={today}
                                    value={date}
                                    inputMode="none"
                                    onKeyDown={blockDateKeyboardEditing}
                                    onPaste={(event) => event.preventDefault()}
                                    onChange={(event) => {
                                        const nextDate = preserveDate(event.target.value);
                                        setDate(nextDate);
                                        if (returnDate && returnDate < nextDate) {
                                            setReturnDate(nextDate);
                                        }
                                    }}
                                    required
                                />
                            </div>
                        </div>

                        {tripType === TRIP_TYPE.ROUND_TRIP && (
                            <div className="input-block date-block">
                                <span className="input-svg-bubble blue">
                                    <SvgIcon name="calendar" />
                                </span>
                                <div className="input-field-wrapper">
                                    <label htmlFor="return-date">Ngày về</label>
                                    <input
                                        id="return-date"
                                        type="date"
                                        min={date || today}
                                        value={returnDate}
                                        inputMode="none"
                                        onKeyDown={blockDateKeyboardEditing}
                                        onPaste={(event) => event.preventDefault()}
                                        onChange={(event) => setReturnDate(preserveDate(event.target.value))}
                                        required
                                    />
                                </div>
                            </div>
                        )}

                        <button type="submit" className="btn-search-submit" disabled={loading || departure === destination}>
                            {loading ? 'Đang tìm...' : 'Tìm lịch trình'}
                        </button>
                    </div>

                    {searchHistory.length > 0 && (
                        <div className="form-tier-recent">
                            <span className="recent-title">Tìm kiếm gần đây</span>
                            <div className="recent-cards-container">
                                {searchHistory.map((item, index) => (
                                    <button key={`${item.route}-${item.date}-${item.returnDate || 'one-way'}-${item.tripType || TRIP_TYPE.ONE_WAY}-${index}`} type="button" className="recent-card" onClick={() => applyHistory(item)}>
                                        <span className="history-icon">
                                            <SvgIcon name="clock" />
                                        </span>
                                        <span className="recent-info">
                                            <strong>{item.route}</strong>
                                            <span>{formatDate(item.date)}{item.returnDate ? ` -> ${formatDate(item.returnDate)}` : ''}</span>
                                            <em>{item.tripType === TRIP_TYPE.ROUND_TRIP ? 'Khứ hồi' : 'Một chiều'}</em>
                                        </span>
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}
                </form>
            </div>

            <div className="results-wrapper">
                {!hasSearched ? (
                    <div className="office-section-container">
                        <h3 className="office-section-title">Liên hệ</h3>
                        <div className="office-grid-layout">
                            <div className="office-card">
                                <div className="office-card-header">VP Quảng Bình</div>
                                <div className="office-card-body">
                                    <ul className="address-list">
                                        <li><SvgIcon name="pin" className="location-icon" />19A Lý Thường Kiệt, Đồng Hới, Quảng Bình</li>
                                        <li><SvgIcon name="pin" className="location-icon" />Đường Nguyễn Văn Linh, Bố Trạch, Hoàn Lão, Quảng Bình</li>
                                        <li><SvgIcon name="pin" className="location-icon" />Nguyễn Tất Thành, Kiến Giang, Lệ Thủy, Quảng Bình</li>
                                    </ul>
                                </div>
                                <div className="office-card-footer">Hotline: <strong className="phone-highlight">0914.077.779</strong></div>
                            </div>
                            <div className="office-card">
                                <div className="office-card-header">VP Hà Nội</div>
                                <div className="office-card-body">
                                    <ul className="address-list">
                                        <li><SvgIcon name="pin" className="location-icon" />338 Trần Khát Chân, Hai Bà Trưng, Hà Nội</li>
                                        <li><SvgIcon name="pin" className="location-icon" />Sảnh T1 + T2 Sân bay Nội Bài</li>
                                    </ul>
                                </div>
                                <div className="office-card-footer">Hotline: <strong className="phone-highlight">0914.077.779</strong></div>
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="search-results-layout">
                        <aside className="filter-sidebar">
                            <div className="sidebar-header">
                                <h4>Bộ lọc tìm kiếm</h4>
                                <button type="button" className="btn-clear-filter" onClick={clearAllFilters}>
                                    <SvgIcon name="close" /> Bỏ lọc
                                </button>
                            </div>

                            <div className="filter-group">
                                <h5>Giờ đi</h5>
                                {TIME_SLOT_FILTERS.map(([slot, label]) => (
                                    <label key={slot} className="filter-checkbox-label">
                                        <input type="checkbox" checked={selectedTimeSlots.includes(slot)} onChange={() => handleTimeSlotChange(slot)} />
                                        <span>{label}</span>
                                    </label>
                                ))}
                            </div>

                            <div className="filter-group">
                                <h5>Loại xe</h5>
                                <div className="filter-tags-grid">
                                    {VEHICLE_FILTERS.map((layout) => (
                                        <button key={layout.value} type="button" className={`filter-tag-btn ${selectedLayouts.includes(layout.value) ? 'active' : ''}`} onClick={() => handleLayoutChange(layout.value)}>
                                            {layout.label}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div className="filter-group">
                                <h5>Giá</h5>
                                <div className="filter-tags-grid vertical-tags">
                                    <button type="button" className={`filter-tag-btn ${priceRange.min === 0 && priceRange.max === 300000 ? 'active' : ''}`} onClick={() => handlePriceRangeChange(0, 300000)}>Dưới 300.000đ</button>
                                    <button type="button" className={`filter-tag-btn ${priceRange.min === 300000 && priceRange.max === 500000 ? 'active' : ''}`} onClick={() => handlePriceRangeChange(300000, 500000)}>300.000đ - 500.000đ</button>
                                    <button type="button" className={`filter-tag-btn ${priceRange.min === 500000 && priceRange.max === 2000000 ? 'active' : ''}`} onClick={() => handlePriceRangeChange(500000, 2000000)}>Trên 500.000đ</button>
                                </div>
                            </div>
                        </aside>

                        <div className="results-content">
                            <h3 className="results-main-title">Kết quả lịch trình: {currentSearchRoute} ({pagination.totalElements})</h3>

                            <div className="journey-tabs" role="tablist" aria-label="Chiều chuyến đi">
                                <button
                                    type="button"
                                    role="tab"
                                    aria-selected={activeJourneyLeg === JOURNEY_LEG.OUTBOUND}
                                    className={`journey-tab ${activeJourneyLeg === JOURNEY_LEG.OUTBOUND ? 'active' : ''}`}
                                    onClick={() => handleJourneyTabChange(JOURNEY_LEG.OUTBOUND)}
                                >
                                    <span>Chuyến đi:</span> {formatJourneyDate(date)}
                                </button>
                                {tripType === TRIP_TYPE.ROUND_TRIP && (
                                    <button
                                        type="button"
                                        role="tab"
                                        aria-selected={activeJourneyLeg === JOURNEY_LEG.RETURN}
                                        className={`journey-tab ${activeJourneyLeg === JOURNEY_LEG.RETURN ? 'active' : ''}`}
                                        onClick={() => handleJourneyTabChange(JOURNEY_LEG.RETURN)}
                                        disabled={!returnDate}
                                    >
                                        <span>Chuyến về:</span> {formatJourneyDate(returnDate)}
                                    </button>
                                )}
                            </div>

                            {trips.length === 0 ? (
                                <p className="no-results-msg">{resultMessage || 'Không tìm thấy chuyến xe nào hợp lệ ứng với bộ lọc.'}</p>
                            ) : (
                                <div className="advanced-trips-list">
                                    {trips.map((trip, index) => {
                                        const routeParts = (trip.routeName || currentSearchRoute).split('-');
                                        const displayDeparture = routeParts[0]?.trim() || departure;
                                        const displayDestination = routeParts[1]?.trim() || destination;
                                        const rawVehicleType = trip.coachTypeName || 'Xe khách';
                                        const displayVehicleType = rawVehicleType;
                                        const displayPrice = Number(trip.seatPrice || 0);
                                        const parsedAvailableSeats = Number(trip.availableSeats);
                                        const parsedTotalSeats = Number(trip.totalSeats);
                                        const displaySeatsLeft = Number.isFinite(parsedAvailableSeats)
                                            ? parsedAvailableSeats
                                            : Number.isFinite(parsedTotalSeats) ? parsedTotalSeats : 0;
                                        const coachImage = getCoachImage(rawVehicleType);

                                        return (
                                            <div key={trip.tripId ?? `${trip.routeName || currentSearchRoute}-${trip.departureTime || index}-${index}`} className="advanced-trip-card">
                                                <div className="trip-timeline-header">
                                                    <div className="time-node-start">
                                                        <span className="time-bold">{formatTime(trip.departureTime)}</span>
                                                        <span className="blue-dot"></span>
                                                    </div>
                                                    <div className="timeline-duration-line">
                                                        <span className="duration-text">{trip.duration || '7 giờ 12 phút'}</span>
                                                    </div>
                                                    <div className="time-node-end">
                                                        <span className="blue-dot"></span>
                                                        <span className="time-bold">{formatTime(trip.arrivalTime || forecastArrivalTime(trip.departureTime))}</span>
                                                    </div>
                                                </div>

                                                <div className="trip-locations-grid">
                                                    <div className="loc-text text-left">{displayDeparture}</div>
                                                    <div className="loc-text text-right">{displayDestination}</div>
                                                </div>

                                                <div className="trip-detail-footer-row">
                                                    <div className="vehicle-info-block">
                                                        {coachImage && <img className="vehicle-image" src={coachImage} alt={displayVehicleType} />}
                                                        <div className="vehicle-meta">
                                                            <span className="vehicle-name-label">{displayVehicleType}</span>
                                                            <span className="vehicle-desc-sub">{getCoachDescription(trip)}</span>
                                                        </div>
                                                    </div>
                                                    <div className="price-and-action-block">
                                                        <span className="seats-counter-badge">Còn {displaySeatsLeft} chỗ trống</span>
                                                        <span className="trip-price-amount">{displayPrice.toLocaleString('vi-VN')} đ</span>
                                                    </div>
                                                </div>

                                                <div className="trip-card-actions-bar">
                                                    <button type="button" className="btn-secondary-info" onClick={() => openTripStops(trip)}>Xem điểm đón trả</button>
                                                    <button type="button" className="btn-primary-select" 
                                                        onClick={
                                                            () => navigate(`/booking/trip/${trip.tripId}`, {state: buildTripInfoFromSearchCard(trip)})
                                                        }
                                                    >Chọn chuyến</button>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}

                            {pagination.totalPages > 1 && (
                                <nav className="trip-pagination" aria-label="Phân trang lịch trình">
                                    <button
                                        type="button"
                                        className="pagination-icon-btn"
                                        onClick={() => handlePageChange(pagination.pageNumber - 1)}
                                        disabled={loading || pagination.pageNumber === 0}
                                        aria-label="Trang trước"
                                        title="Trang trước"
                                    >
                                        <SvgIcon name="previous" />
                                    </button>
                                    <span className="pagination-status">
                                        Trang {pagination.pageNumber + 1} / {pagination.totalPages}
                                    </span>
                                    <button
                                        type="button"
                                        className="pagination-icon-btn"
                                        onClick={() => handlePageChange(pagination.pageNumber + 1)}
                                        disabled={loading || pagination.pageNumber + 1 >= pagination.totalPages}
                                        aria-label="Trang sau"
                                        title="Trang sau"
                                    >
                                        <SvgIcon name="next" />
                                    </button>
                                </nav>
                            )}
                        </div>
                    </div>
                )}
            </div>

            {stopModal.trip && (
                <div className="trip-stops-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && closeTripStops()}>
                    <section className="trip-stops-modal" role="dialog" aria-modal="true" aria-labelledby="trip-stops-title">
                        <header className="trip-stops-header">
                            <div>
                                <h3 id="trip-stops-title">Điểm đón trả</h3>
                                <p>{stopModal.trip.routeName || currentSearchRoute}</p>
                            </div>
                            <button type="button" className="trip-stops-close" onClick={closeTripStops} aria-label="Đóng" title="Đóng">
                                <SvgIcon name="close" />
                            </button>
                        </header>

                        <div className="trip-stops-summary">
                            <strong>{formatTime(stopModal.trip.departureTime)}</strong>
                            <span>{stopModal.trip.duration || '7 giờ 12 phút'}</span>
                            <strong>{formatTime(stopModal.trip.arrivalTime || forecastArrivalTime(stopModal.trip.departureTime))}</strong>
                        </div>

                        <div className="trip-stops-body">
                            {stopModal.loading && <p className="trip-stops-message">Đang tải điểm đón trả...</p>}
                            {!stopModal.loading && stopModal.error && <p className="trip-stops-message error">{stopModal.error}</p>}
                            {!stopModal.loading && !stopModal.error && stopModal.stops.length === 0 && (
                                <p className="trip-stops-message">Chuyến này chưa được cấu hình điểm đón trả.</p>
                            )}
                            {!stopModal.loading && !stopModal.error && stopModal.stops.length > 0 && (
                                <ol className="trip-stops-timeline">
                                    {stopModal.stops.map((stop, index) => (
                                        <li key={`${stop.stopPointId}-${stop.stopOrder}`} className="trip-stop-item">
                                            <time>{formatTime(stop.estimatedStopTime)}</time>
                                            <span className={`trip-stop-dot ${index === 0 || index === stopModal.stops.length - 1 ? 'terminal' : ''}`}></span>
                                            <div className="trip-stop-copy">
                                                <strong>{stop.stopPointName}</strong>
                                                <span>{[stop.address, stop.city].filter(Boolean).join(', ')}</span>
                                            </div>
                                        </li>
                                    ))}
                                </ol>
                            )}
                        </div>

                        <footer className="trip-stops-actions">
                            <button type="button" className="trip-stops-dismiss" onClick={closeTripStops}>Đóng</button>
                            <button
                                type="button"
                                className="btn-primary-select"
                                onClick={() => navigate(`/booking/trip/${stopModal.trip.tripId}`, { state: buildTripInfoFromSearchCard(stopModal.trip) })}
                            >
                                Chọn chuyến
                            </button>
                        </footer>
                    </section>
                </div>
            )}
        </div>
    );
};

export default HomePage;
