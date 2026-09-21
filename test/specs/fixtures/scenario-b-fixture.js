export const SEARCH_DATE = '2030-08-20';
export const SEARCH_ROUTE = 'Hà Nội - Quảng Bình';

export const baseTrip = {
  tripId: 101,
  routeName: SEARCH_ROUTE,
  coachTypeName: 'Xe Giường Nằm Luxury 32 chỗ',
  departureTime: `${SEARCH_DATE}T08:00:00`,
  arrivalTime: `${SEARCH_DATE}T15:12:00`,
  duration: '7 giờ 12 phút',
  seatPrice: 400000,
  availableSeats: 12,
  totalSeats: 32,
};

export const pageOf = (content, pageNumber = 0, totalPages = 1) => ({
  content,
  pageNumber,
  pageSize: 5,
  totalElements: content.length,
  totalPages,
  last: pageNumber + 1 >= totalPages,
});
