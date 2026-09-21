import { expect, test } from '@playwright/test';
import { authenticateAsTicketStaff } from './support/auth';
import { DEFAULT_TRIP, installCargoApiMock } from './support/cargo-api-mock';
import { fillValidCargoForm, openCargoCreateForm } from './support/cargo-form';

test.describe('Cargo Booking system workflow', () => {
  test('CG-TC-A01 redirects unauthenticated users to staff login', async ({ page }) => {
    await page.goto('/staff/cargo-tickets');
    await expect(page).toHaveURL(/\/staff\/login$/);
    await expect(page.getByRole('heading', { name: 'Đăng Nhập' })).toBeVisible();
  });

  test('CG-TC-A03 exposes separate send and receipt workspaces', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    await installCargoApiMock(page);
    await page.goto('/staff/cargo-tickets');

    await expect(page.getByRole('heading', { name: 'Quản lý đơn hàng' })).toBeVisible();
    await page.getByRole('button', { name: /Gửi hàng/ }).click();
    await expect(page).toHaveURL(/\/staff\/cargo-tickets\/send$/);
    await expect(page.getByRole('heading', { name: 'Đơn hàng đang chờ' })).toBeVisible();

    await page.goto('/staff/cargo-tickets');
    await page.getByRole('button', { name: /Kiểm tra hàng/ }).click();
    await expect(page).toHaveURL(/\/staff\/cargo-tickets\/check$/);
    await expect(page.getByRole('heading', { name: 'Xe đã dỡ hàng' })).toBeVisible();
  });

  test('CG-TC-B01 locks pickup to the current ticket agency', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    await installCargoApiMock(page);
    await openCargoCreateForm(page);

    await expect(page.getByLabel('Điểm nhận tại văn phòng')).toContainText(
      'Văn phòng Quận 1',
    );
  });

  test('CG-TC-C01 creates receiver-paid cargo with deferred assignment', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await openCargoCreateForm(page);
    await fillValidCargoForm(page);

    await page.locator('[name="feePayer"]').selectOption('RECEIVER');
    await expect(page.locator('[name="paymentMethod"]')).toBeDisabled();
    await page.getByRole('button', { name: 'Lưu đơn gửi hàng' }).click();

    await expect(page).toHaveURL(/\/staff\/cargo-tickets\/send$/);
    await expect.poll(() => api.createdPayloads.length).toBe(1);
    expect(api.createdPayloads[0]).toMatchObject({
      tripId: null,
      feePayer: 'RECEIVER',
      paymentMethod: null,
      pickupStopId: 101,
      dropoffStopId: 201,
    });
    expect(api.createdPayloads[0].details).toHaveLength(1);
  });

  test('CG-TC-C02 confirms cash before creating sender-paid cargo', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await openCargoCreateForm(page);
    await fillValidCargoForm(page);

    await page.locator('[name="feePayer"]').selectOption('SENDER');
    await page.locator('[name="paymentMethod"]').selectOption('CASH');
    await page.getByRole('button', { name: 'Lưu đơn gửi hàng' }).click();

    const dialog = page.getByRole('dialog').filter({ hasText: 'Xác nhận thu tiền mặt' });
    await expect(dialog).toContainText('Số tiền');
    await dialog.getByRole('button', { name: 'Đã nhận tiền' }).click();

    await expect(page).toHaveURL(/\/staff\/cargo-tickets\/send$/);
    await expect.poll(() => api.createdPayloads.length).toBe(1);
    expect(api.createdPayloads[0]).toMatchObject({
      feePayer: 'SENDER',
      paymentMethod: 'CASH',
    });
  });

  test('CG-TC-C03 shows QR and cancels an unpaid sender bank draft', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await openCargoCreateForm(page);
    await fillValidCargoForm(page);

    await page.locator('[name="feePayer"]').selectOption('SENDER');
    await page.locator('[name="paymentMethod"]').selectOption('BANK_TRANSFER');
    await page.getByRole('button', { name: 'Lưu đơn gửi hàng' }).click();

    const qrDialog = page.getByRole('dialog').filter({ hasText: 'Thanh toán chuyển khoản' });
    await expect(qrDialog).toContainText('Đang chờ thanh toán');
    await expect(qrDialog.getByAltText('SePay QR Code')).toBeVisible();
    await qrDialog.getByRole('button', { name: 'Hủy' }).click();

    const abandonDialog = page.getByRole('dialog').filter({ hasText: 'Hủy đơn nháp' });
    await abandonDialog.getByRole('button', { name: 'Hủy đơn' }).click();
    await expect(page).toHaveURL(/\/staff\/cargo-tickets\/send$/);
    await expect.poll(() => api.disabledTicketIds).toEqual([901]);
  });

  test('CG-TC-D03 blocks cargo above the 2.50 m³ limit', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await openCargoCreateForm(page);
    await fillValidCargoForm(page, {
      length: '2',
      width: '1',
      height: '1.3',
    });

    await expect(page.getByText(/vượt giới hạn 2,5 m³/)).toBeVisible();
    await expect(page.getByRole('button', { name: 'Lưu đơn gửi hàng' })).toBeDisabled();
    expect(api.createdPayloads).toHaveLength(0);
  });

  test('CG-TC-G01 assigns cargo without exceeding coach capacity', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await page.goto(`/staff/cargo-tickets/send/assign/${DEFAULT_TRIP.tripId}`);

    await expect(
      page.getByRole('heading', { name: `Chuyến #${DEFAULT_TRIP.tripId}` }),
    ).toBeVisible();
    const first = page.getByRole('checkbox', { name: 'Chọn đơn CG-ASSIGN-71' });
    const second = page.getByRole('checkbox', { name: 'Chọn đơn CG-ASSIGN-72' });
    await first.check();

    await expect(second).toBeDisabled();
    await expect(page.getByText('2.30 / 2.50 m³')).toBeVisible();
    await page.getByRole('button', { name: 'Gán vào chuyến (1)' }).click();

    await expect(page.getByText('Đã gán 1 đơn vào chuyến #301.')).toBeVisible();
    await expect.poll(() => api.assignedPayloads).toEqual([
      { cargoTicketIds: [71] },
    ]);
  });
});
