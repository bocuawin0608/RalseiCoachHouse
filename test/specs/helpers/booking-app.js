import { expect } from '@playwright/test';
import { TRIP_ID, initData, pendingPayment, price, seats, trip } from '../fixtures/booking-fixture.js';

const json = (route, body, status = 200) =>
  route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(body) });

export async function mockBookingApi(page, overrides = {}) {
  const state = { confirmPayload: null, lockPayload: null, cancelCalled: false };

  await page.route('**/api/v1/bookings/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const pathname = url.pathname;

    if (pathname.endsWith(`/trips/${TRIP_ID}/seats`) && request.method() === 'GET') {
      return json(route, overrides.seats || seats);
    }
    if (pathname.endsWith('/seats/lock')) {
      state.lockPayload = request.postDataJSON();
      if (overrides.lockFailure) return json(route, { message: overrides.lockFailure }, 409);
      return json(route, { locked: true });
    }
    if (pathname.endsWith('/seats/release') || pathname.endsWith('/seats/release/beacon')) {
      return json(route, { released: true });
    }
    if (pathname.endsWith('/step2-init-data')) return json(route, overrides.initData || initData);
    if (pathname.endsWith('/calculate-price')) return json(route, overrides.price || price);
    if (pathname.endsWith('/check-phone')) {
      return json(route, overrides.phoneCheck || { isKnown: true, suggestedProfile: null });
    }
    if (pathname.endsWith('/confirm')) {
      state.confirmPayload = request.postDataJSON();
      if (overrides.confirmFailure) return json(route, { message: overrides.confirmFailure }, 400);
      return json(route, pendingPayment);
    }
    if (pathname.endsWith(`/${pendingPayment.transactionId}/cancel`)) {
      state.cancelCalled = true;
      return json(route, { ...pendingPayment, paymentStatus: 'FAILED' });
    }
    if (pathname.includes('/payments/')) return json(route, pendingPayment);
    return json(route, {});
  });

  return state;
}

export async function openBooking(page) {
  await page.addInitScript((tripState) => {
    history.replaceState({ usr: tripState, key: 'blackbox', idx: 0 }, '', `/booking/trip/${tripState.tripId}`);
  }, trip);
  await page.goto(`/booking/trip/${TRIP_ID}`);
  await expect(page.getByTitle('Ghế: A1', { exact: true })).toBeVisible();
}

export async function selectSeatAndContinue(page, seatCode = 'A1') {
  await page.getByTitle(`Ghế: ${seatCode}`, { exact: true }).click();
  await page.getByRole('button', { name: 'Tiếp tục' }).click();
  await expect(page.getByText('Thông tin hành khách')).toBeVisible();
}

export async function fillValidPassenger(page) {
  await page.getByPlaceholder('VD: Nguyễn Văn A').fill('Nguyễn Văn An');
  await page.getByPlaceholder('VD: 0912345678').fill('0912345678');
  await page.getByPlaceholder('VD: 0912345678').blur();
  await expect(page.getByText('Số điện thoại đã biết')).toBeVisible();
  await page.getByPlaceholder('VD: name@example.com').fill('an@example.com');
  await page.locator('select[name="pickupStopId"]').selectOption('11');
  await page.locator('select[name="dropoffStopId"]').selectOption('21');
}
