export const TRIP_ID = 101;

export const trip = {
  tripId: TRIP_ID,
  routeName: 'Quảng Trị - Hà Nội',
  coachTypeName: 'Giường nằm',
  departureTime: '2030-08-20T20:00:00',
  arrivalTime: '2030-08-21T05:00:00',
  duration: '9 giờ',
  seatPrice: 250000,
  availableSeats: 12,
  totalSeats: 12,
};

export const seats = Array.from({ length: 12 }, (_, index) => ({
  tripSeatId: index + 1,
  seatCode: `A${index + 1}`,
  floorIndex: 1,
  rowIndex: Math.floor(index / 3) + 1,
  colIndex: (index % 3) + 1,
  status: index === 11 ? 'SOLD' : 'AVAILABLE',
}));

export const initData = {
  customerProfile: null,
  pickupStopPoints: [
    { stopPointId: 11, stopPointName: 'Bến xe Đông Hà', minutesFromStart: 0 },
  ],
  dropoffStopPoints: [
    { stopPointId: 21, stopPointName: 'Bến xe Nước Ngầm', minutesFromStart: 540 },
  ],
  vouchers: [],
};

export const price = {
  basePrice: 250000,
  baseSurcharge: 0,
  totalRawPrice: 250000,
  discountAmount: 0,
  totalFinalPrice: 250000,
};

export const pendingPayment = {
  ticketCode: 'RCH-TEST-001',
  transactionId: 'TX-BOOKING-001',
  amount: 250000,
  bankAccountNumber: '0123456789',
  bankName: 'Test Bank',
  paymentExpiresAt: '2030-08-20T19:30:00',
  cancelToken: 'cancel-test-token',
  paymentStatus: 'PENDING',
  tripId: TRIP_ID,
  primaryPassengerName: 'Nguyễn Văn An',
  primaryPassengerPhone: '0912345678',
  seatCodes: ['A1'],
};
