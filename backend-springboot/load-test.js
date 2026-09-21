import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

/*
 * RCHS public booking load test
 *
 * SRS coverage:
 * - UC-09: find available trips
 * - UC-10: online booking wizard through seat hold, passenger-info init, price
 *          calculation, and the safe seat-release exit path
 * - PR-02: concurrent Redis seat holds
 * - PR-10: trip-facing list requests complete within two seconds
 *
 * This deliberately stops before POST /confirm and external payment. Confirming a
 * booking creates a pending ticket/payment and requires a real OTP or customer
 * identity; doing that from every VU would pollute the environment under test.
 * Every successful hold is released in a finally block.
 *
 * Examples:
 *   k6 run load-test.js
 *   k6 run -e BASE_URL=https://staging.example.com/api -e TRIP_DATE=2026-10-01 load-test.js
 *   k6 run -e K6_PROFILE=load -e BASE_URL=https://staging.example.com/api load-test.js
 */

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:9090/api').replace(/\/$/, '');
const PROFILE = __ENV.K6_PROFILE || 'smoke';
const TRIP_DATE = __ENV.TRIP_DATE || futureDate(7);
const ROUTE = __ENV.ROUTE || '';
const PAGE_SIZE = Number(__ENV.PAGE_SIZE || 10);
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || 0.25);

const flowSuccess = new Rate('booking_flow_success');
const noTripAvailable = new Counter('booking_no_trip_available');
const seatMapDuration = new Trend('booking_seat_map_duration', true);
const tripViewDuration = new Trend('booking_trip_view_duration', true);
const pricingDuration = new Trend('booking_price_calculation_duration', true);

const loadScenario = {
  executor: 'ramping-vus',
  startVUs: 0,
  stages: [
    { duration: '30s', target: Number(__ENV.RAMP_VUS || 10) },
    { duration: __ENV.HOLD_DURATION || '2m', target: Number(__ENV.RAMP_VUS || 10) },
    { duration: '30s', target: 0 },
  ],
  gracefulRampDown: '15s',
};

export const options = {
  scenarios: {
    public_booking: PROFILE === 'load'
      ? loadScenario
      : { executor: 'per-vu-iterations', vus: 1, iterations: 1, maxDuration: '30s' },
  },
  thresholds: {
    // SRS PR-10: passenger/cargo lists for a trip load within 2 seconds.
    booking_trip_view_duration: ['p(95)<2000'],
    booking_seat_map_duration: ['p(95)<2000'],
    // A separate guardrail for the rest of the booking wizard.
    booking_price_calculation_duration: ['p(95)<3000'],
    booking_flow_success: ['rate>0.95'],
    http_req_failed: ['rate<0.01'],
  },
  insecureSkipTLSVerify: __ENV.INSECURE_SKIP_TLS_VERIFY === 'true',
};

export default function publicBookingFlow() {
  let heldSeat = null;

  try {
    const trip = findTrip();
    if (!trip) {
      noTripAvailable.add(1);
      // An empty result is valid for a date without scheduled trips. It is not a
      // successful booking-flow iteration, so test environments should provide
      // a future trip via TRIP_DATE (and optionally ROUTE).
      // Back off before retrying. In particular, a refused connection returns
      // immediately; without this delay k6 can generate millions of noisy
      // failed requests before its threshold stops the test.
      sleep(1);
      return;
    }

    const tripId = trip.tripId || trip.id;
    if (!tripId) {
      flowSuccess.add(false);
      return;
    }

    group('booking wizard - seat selection', () => {
      const seatsResponse = http.get(`${BASE_URL}/v1/bookings/trips/${tripId}/seats`, {
        tags: { endpoint: 'booking_seat_map' },
      });
      seatMapDuration.add(seatsResponse.timings.duration);
      if (!check(seatsResponse, { 'seat map returns 200': (r) => r.status === 200 })) {
        return;
      }

      const availableSeat = firstAvailableSeat(seatsResponse.json());
      if (!availableSeat) {
        noTripAvailable.add(1);
        return;
      }

      const sessionToken = `k6-${__VU}-${__ITER}-${randomString(20)}`;
      const lockResponse = http.post(
        `${BASE_URL}/v1/bookings/trips/${tripId}/seats/lock`,
        JSON.stringify({ tripSeatIds: [availableSeat.tripSeatId] }),
        jsonParams(sessionToken, 'booking_seat_lock'),
      );
      if (!check(lockResponse, { 'seat lock returns 200': (r) => r.status === 200 })) {
        return;
      }

      const lock = lockResponse.json();
      heldSeat = {
        tripId,
        tripSeatId: availableSeat.tripSeatId,
        holdToken: lock.holdToken || sessionToken,
      };
    });

    if (!heldSeat) return;
    sleep(THINK_TIME_SECONDS);

    group('booking wizard - passenger information and pricing', () => {
      const initResponse = http.get(
        `${BASE_URL}/v1/bookings/trips/${heldSeat.tripId}/step2-init-data`,
        jsonParams(heldSeat.holdToken, 'booking_step2_init'),
      );
      if (!check(initResponse, { 'booking init returns 200': (r) => r.status === 200 })) return;

      const init = initResponse.json();
      const itinerary = validItinerary(init.pickupStopPoints, init.dropoffStopPoints);
      if (!itinerary) {
        flowSuccess.add(false);
        return;
      }

      const priceResponse = http.post(
        `${BASE_URL}/v1/bookings/trips/${heldSeat.tripId}/calculate-price`,
        JSON.stringify({
          pickupStopId: itinerary.pickupStopId,
          dropoffStopId: itinerary.dropoffStopId,
          voucherId: null,
        }),
        jsonParams(heldSeat.holdToken, 'booking_calculate_price'),
      );
      pricingDuration.add(priceResponse.timings.duration);
      flowSuccess.add(check(priceResponse, {
        'price calculation returns 200': (r) => r.status === 200,
        'price calculation returns a total': (r) => Number(r.json('totalFinalPrice')) >= 0,
      }));
    });
  } finally {
    if (heldSeat) releaseSeat(heldSeat);
  }
}

function findTrip() {
  const query = `date=${encodeURIComponent(TRIP_DATE)}&page=0&size=${PAGE_SIZE}`
    + (ROUTE ? `&route=${encodeURIComponent(ROUTE)}` : '');
  const response = http.get(`${BASE_URL}/v1/trips/home?${query}`, {
    tags: { endpoint: 'trip_search' },
  });
  tripViewDuration.add(response.timings.duration);
  if (!check(response, { 'trip search returns 200': (r) => r.status === 200 })) return null;

  const trips = response.json('content') || [];
  return trips.length ? trips[(__VU + __ITER) % trips.length] : null;
}

function releaseSeat(heldSeat) {
  const response = http.post(
    `${BASE_URL}/v1/bookings/trips/${heldSeat.tripId}/seats/release`,
    JSON.stringify({ tripSeatIds: [heldSeat.tripSeatId] }),
    jsonParams(heldSeat.holdToken, 'booking_seat_release'),
  );
  check(response, { 'seat release returns 200': (r) => r.status === 200 });
}

function firstAvailableSeat(seats) {
  if (!Array.isArray(seats)) return null;
  return seats.find((seat) => seat.status === 'AVAILABLE' && Number(seat.tripSeatId) > 0) || null;
}

function validItinerary(pickupStops, dropoffStops) {
  if (!Array.isArray(pickupStops) || !Array.isArray(dropoffStops)) return null;
  for (const pickup of pickupStops) {
    const dropoff = dropoffStops.find((stop) => Number(stop.minutesFromStart) > Number(pickup.minutesFromStart));
    if (pickup.stopPointId && dropoff && dropoff.stopPointId) {
      return { pickupStopId: pickup.stopPointId, dropoffStopId: dropoff.stopPointId };
    }
  }
  return null;
}

function jsonParams(holdToken, endpoint) {
  return {
    headers: {
      'Content-Type': 'application/json',
      'X-Booking-Session': holdToken,
    },
    tags: { endpoint },
  };
}

function futureDate(daysAhead) {
  const date = new Date();
  date.setDate(date.getDate() + daysAhead);
  return date.toISOString().slice(0, 10);
}

function randomString(length) {
  const alphabet = 'abcdefghijklmnopqrstuvwxyz0123456789';
  let value = '';
  for (let index = 0; index < length; index += 1) {
    value += alphabet[Math.floor(Math.random() * alphabet.length)];
  }
  return value;
}
