import { useCallback, useEffect, useMemo, useState } from 'react';
import { useDebounce } from '../../../hooks/useDebounce';
import { cargoTicketApi } from '../api/cargoTicketApi';

/** Loads one server-side cargo queue and applies the shared local search filter. */
export function useCargoTickets(queueStatus = '', tripId = null) {
    const [tickets, setTickets] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({ search: '', status: '' });
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0
    });
    const debouncedSearch = useDebounce(filters.search, 250);

    const fetchTickets = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await cargoTicketApi.getCargoTickets({
                page: pageInfo.page,
                size: pageInfo.size,
                status: queueStatus || undefined,
                tripId: tripId || undefined
            });
            setTickets(response.content || []);
            setPageInfo((previous) => ({
                ...previous,
                totalElements: response.totalElements ?? response.page?.totalElements ?? 0,
                totalPages: response.totalPages ?? response.page?.totalPages ?? 0
            }));
        } catch (err) {
            setTickets([]);
            setError(err.response?.data?.message || 'Không thể tải danh sách đơn gửi hàng.');
        } finally {
            setLoading(false);
        }
    }, [pageInfo.page, pageInfo.size, queueStatus, tripId]);

    useEffect(() => {
        // Fetching on pagination changes intentionally starts the hook's loading state.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        fetchTickets();
    }, [fetchTickets]);

    const filteredTickets = useMemo(() => {
        const keyword = debouncedSearch.trim().toLowerCase();
        return tickets.filter((ticket) => {
            const matchesSearch = !keyword || [
                ticket.ticketCode,
                ticket.senderName,
                ticket.senderPhone,
                ticket.receiverName,
                ticket.receiverPhone
            ].some((value) => String(value || '').toLowerCase().includes(keyword));
            const matchesStatus = !filters.status || ticket.status === filters.status;
            return matchesSearch && matchesStatus;
        });
    }, [tickets, debouncedSearch, filters.status]);

    const handleFilterChange = (event) => {
        const { name, value } = event.target;
        setFilters((previous) => ({ ...previous, [name]: value }));
    };

    const handleReset = () => setFilters({ search: '', status: '' });

    return {
        tickets: filteredTickets,
        loading,
        error,
        filters,
        pageInfo,
        setPageInfo,
        handleFilterChange,
        handleReset,
        refetch: fetchTickets
    };
}
