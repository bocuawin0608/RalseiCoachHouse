import { test, expect } from '@playwright/test';
import {
  fillValidPassenger,
  mockBookingApi,
  openBooking,
  selectSeatAndContinue,
} from './helpers/booking-app.js';

test.beforeEach(async ({ page }) => {
  page.on('dialog', (dialog) => dialog.accept());
});

test('TC-01 guest completes booking and reaches pending QR payment', async ({ page }) => {
  const state = await mockBookingApi(page);
  await openBooking(page);
  await selectSeatAndContinue(page);
  await fillValidPassenger(page);
  await page.getByRole('button', { name: 'Thanh toán' }).click();

  await expect(page).toHaveURL(/\/booking\/payment\/TX-BOOKING-001$/);
  await expect(page.getByAltText('Mã QR thanh toán SePay')).toBeVisible();
  expect(state.confirmPayload).toMatchObject({
    pickupStopId: 11,
    dropoffStopId: 21,
    voucherId: null,
    passengers: [{ tripSeatId: 1, fullname: 'Nguyễn Văn An', phone: '0912345678' }],
  });
});

test('TC-02 continue is blocked until an available seat is selected', async ({ page }) => {
  await mockBookingApi(page);
  await openBooking(page);
  await expect(page.getByRole('button', { name: 'Tiếp tục' })).toBeDisabled();
  await page.getByTitle('Ghế: A12', { exact: true }).click();
  await expect(page.getByRole('button', { name: 'Tiếp tục' })).toBeDisabled();
});

test('TC-03 customer cannot select more than ten seats', async ({ page }) => {
  await mockBookingApi(page);
  await openBooking(page);
  for (let index = 1; index <= 11; index += 1) {
    await page.getByTitle(`Ghế: A${index}`, { exact: true }).click();
  }
  await expect(page.getByText('Đang chọn: 10 ghế')).toBeVisible();
});

test('TC-04 concurrent seat-lock conflict resets selection', async ({ page }) => {
  await mockBookingApi(page, { lockFailure: 'Ghế A1 vừa được khách khác giữ' });
  await openBooking(page);
  await page.getByTitle('Ghế: A1', { exact: true }).click();
  await page.getByRole('button', { name: 'Tiếp tục' }).click();
  await expect(page.getByRole('button', { name: 'Tiếp tục' })).toBeDisabled();
  await expect(page.getByText('Đang chọn: 1 ghế')).not.toBeVisible();
});

test('TC-05 passenger, route, and child fields enforce validation', async ({ page }) => {
  await mockBookingApi(page);
  await openBooking(page);
  await selectSeatAndContinue(page);
  await page.getByRole('button', { name: 'Thanh toán' }).click();
  await expect(page.getByText('Vui lòng nhập họ tên')).toBeVisible();
  await expect(page.getByText('Vui lòng chọn điểm đón')).toBeVisible();

  await page.getByLabel(/Có trẻ nhỏ đi cùng/).check();
  await page.getByPlaceholder('Tên của bé').fill('1');
  await page.locator('input[name="passengers.0.childBirthYear"]').fill('2000');
  await page.getByRole('button', { name: 'Thanh toán' }).click();
  await expect(page.getByText('Họ tên bé không hợp lệ!')).toBeVisible();
  await expect(page.getByText(/Năm sinh của trẻ phải/)).toBeVisible();
});

test('TC-06 an unknown guest phone requires OTP verification', async ({ page }) => {
  await mockBookingApi(page, { phoneCheck: { isKnown: false, suggestedProfile: null } });
  await openBooking(page);
  await selectSeatAndContinue(page);
  await page.getByPlaceholder('VD: 0912345678').fill('0987654321');
  await page.getByPlaceholder('VD: 0912345678').blur();
  await expect(page.getByRole('dialog')).toContainText('Xác thực số điện thoại');
});

test('TC-07 customer can cancel a pending payment', async ({ page }) => {
  const state = await mockBookingApi(page);
  await openBooking(page);
  await selectSeatAndContinue(page);
  await fillValidPassenger(page);
  await page.getByRole('button', { name: 'Thanh toán' }).click();
  await page.getByRole('button', { name: 'Hủy thanh toán' }).click();
  await expect(page.getByText(/Mã thanh toán đã hết hạn hoặc bị hủy/)).toBeVisible();
  expect(state.cancelCalled).toBe(true);
});

test('TC-08 backend booking rejection is shown without opening payment', async ({ page }) => {
  await mockBookingApi(page, { confirmFailure: 'Phiên giữ ghế đã hết hạn' });
  await openBooking(page);
  await selectSeatAndContinue(page);
  await fillValidPassenger(page);
  await page.getByRole('button', { name: 'Thanh toán' }).click();
  await expect(page.getByText('Phiên giữ ghế đã hết hạn')).toBeVisible();
  await expect(page).toHaveURL(/\/booking\/trip\/101$/);
});
