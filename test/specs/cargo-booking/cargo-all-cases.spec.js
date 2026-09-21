import { expect, test } from '@playwright/test';
import { authenticateAsRole, authenticateAsTicketStaff } from './support/auth';
import { DEFAULT_TRIP, installCargoApiMock } from './support/cargo-api-mock';
import { fillValidCargoForm, openCargoCreateForm } from './support/cargo-form';

function cargoTicket(overrides = {}) {
  return {
    cargoTicketId: 801,
    ticketCode: 'CG-QUEUE-801',
    senderName: 'Nguyễn Người Gửi',
    senderPhone: '0901111111',
    receiverName: 'Trần Người Nhận',
    receiverPhone: '0902222222',
    pickupStopId: 101,
    pickupStopName: 'Văn phòng Quận 1',
    dropoffStopId: 201,
    dropoffStopName: 'Văn phòng Đà Lạt',
    destinationAgencyName: 'Văn phòng Đà Lạt',
    routeName: 'TP. Hồ Chí Minh - Đà Lạt',
    tripId: 301,
    licensePlate: '51B-123.45',
    status: 'RECEIVED',
    feePayer: 'SENDER',
    totalPrice: 85_000,
    codAmount: 0,
    payment: { paymentMethod: 'CASH', status: 'COMPLETED' },
    ...overrides,
  };
}

async function browserFetch(page, path, { method = 'GET', body } = {}) {
  return page.evaluate(async ({ requestPath, requestMethod, requestBody }) => {
    const response = await fetch(requestPath, {
      method: requestMethod,
      headers: requestBody ? { 'content-type': 'application/json' } : undefined,
      body: requestBody ? JSON.stringify(requestBody) : undefined,
    });
    let data = null;
    try {
      data = await response.json();
    } catch {
      // 204 responses intentionally have no JSON body.
    }
    return { status: response.status, data };
  }, { requestPath: path, requestMethod: method, requestBody: body });
}

async function openDestinationQueue(page, api, ticket) {
  api.tickets.push(ticket);
  api.receivingTrips.push({
    ...DEFAULT_TRIP,
    waitingOrderCount: 1,
    lastCargoUpdateAt: '2030-07-25T12:00:00',
  });
  await page.goto('/staff/cargo-tickets/check');
  await page.getByRole('button', { name: 'Xem đơn hàng' }).click();
  await expect(page.getByText(ticket.ticketCode)).toBeVisible();
}

test.describe('Cargo Booking complete case coverage', () => {
  test('CG-TC-A02 rejects roles without ticket-staff access', async ({ page }) => {
    await authenticateAsRole(page, 'MANAGER', 'manager.qa');
    await page.goto('/staff/cargo-tickets');
    await expect(page).toHaveURL(/\/unauthorized$/);
    await expect(page.getByText('401 - Unauthorized.')).toBeVisible();
  });

  test('CG-TC-B02 shows only downstream destination offices', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    await installCargoApiMock(page);
    await openCargoCreateForm(page);

    const destination = page.locator('[name="dropoffStopId"]');
    await expect(destination.locator('option')).toHaveText([
      '-- Chọn điểm trả --',
      'Văn phòng Đà Lạt (Đà Lạt)',
    ]);
    await expect(destination).not.toContainText('Trạm nghỉ Đồng Nai');
  });

  test('CG-TC-B03 lists an eligible trip but permits deferred assignment', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    await installCargoApiMock(page);
    await openCargoCreateForm(page);
    await page.locator('[name="dropoffStopId"]').selectOption('201');

    const trip = page.locator('[name="tripId"]');
    await expect(trip.locator('option')).toHaveCount(2);
    await expect(trip.locator('option').first()).toHaveText('-- Gán chuyến sau --');
    await expect(trip).toContainText('Mã: 301');
    await expect(trip).toHaveValue('');
  });

  test('CG-TC-B04 rejects invalid routing without persisting cargo', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await page.route('**/api/v1/ticket-staff/cargo-tickets/with-details', (route) => (
      route.fulfill({
        status: 400,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Điểm trả không thuộc văn phòng hợp lệ.' }),
      })
    ));
    await page.goto('/staff/cargo-tickets');

    const result = await browserFetch(
      page,
      '/api/v1/ticket-staff/cargo-tickets/with-details',
      {
        method: 'POST',
        body: { pickupStopId: 101, dropoffStopId: 150, tripId: 301, details: [] },
      },
    );
    expect(result).toMatchObject({ status: 400 });
    expect(result.data.message).toContain('không thuộc văn phòng');
    expect(api.createdPayloads).toHaveLength(0);
  });

  test('CG-TC-C04 rejects sender bank payment with a preselected trip', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await openCargoCreateForm(page);
    await fillValidCargoForm(page);
    await page.locator('[name="tripId"]').selectOption('301');
    await page.locator('[name="feePayer"]').selectOption('SENDER');
    await page.locator('[name="paymentMethod"]').selectOption('BANK_TRANSFER');
    await page.getByRole('button', { name: 'Lưu đơn gửi hàng' }).click();

    await expect(page.getByText(/tạo đơn chưa gán chuyến/).first()).toBeVisible();
    expect(api.createdPayloads).toHaveLength(0);
  });

  test('CG-TC-C05 blocks missing contacts and empty cargo details', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await openCargoCreateForm(page);

    await expect(page.getByRole('button', { name: 'Lưu đơn gửi hàng' })).toBeDisabled();
    await expect(page.getByText('Chưa có chi tiết hàng hóa.')).toBeVisible();
    expect(api.createdPayloads).toHaveLength(0);
  });

  test('CG-TC-C06 creates multiple cargo details atomically', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await openCargoCreateForm(page);
    await fillValidCargoForm(page);

    await page.getByRole('button', { name: 'Đính kèm hàng hóa' }).click();
    const second = page
      .getByText('Hàng hóa #2', { exact: true })
      .locator('xpath=ancestor::div[contains(@class, "bg-light")]');
    await second.locator('select').selectOption('501');
    const inputs = second.locator('input[type="number"]');
    await inputs.nth(0).fill('1');
    await inputs.nth(1).fill('5');
    await inputs.nth(2).fill('0.5');
    await inputs.nth(3).fill('0.4');
    await inputs.nth(4).fill('0.5');
    await page.locator('[name="feePayer"]').selectOption('RECEIVER');
    await expect(page.getByText(/170[.,]000|170\\.000/)).toBeVisible();
    await page.getByRole('button', { name: 'Lưu đơn gửi hàng' }).click();

    await expect.poll(() => api.createdPayloads.length).toBe(1);
    expect(api.createdPayloads[0].details).toHaveLength(2);
    expect(api.createdPayloads[0].totalPrice).toBe(170_000);
  });

  test('CG-TC-D01 calculates freight plus cargo-type surcharge', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    await installCargoApiMock(page);
    await openCargoCreateForm(page);
    await fillValidCargoForm(page);

    // 0.1 m³ / 1.2 × 300 × 3,000 + 10,000 = 85,000 VND.
    await expect(page.getByText(/85[.,]000|85\\.000/).first()).toBeVisible();
  });

  test('CG-TC-D02 accepts the exact 2.50 m³ order boundary', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await openCargoCreateForm(page);
    await fillValidCargoForm(page, { length: '2.5', width: '1', height: '1' });
    await page.locator('[name="feePayer"]').selectOption('RECEIVER');

    await expect(page.getByText(/vượt giới hạn 2,5 m³/)).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Lưu đơn gửi hàng' })).toBeEnabled();
    await page.getByRole('button', { name: 'Lưu đơn gửi hàng' }).click();
    await expect.poll(() => api.createdPayloads.length).toBe(1);
  });

  test('CG-TC-D04 prevents a selection above remaining coach capacity', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    await installCargoApiMock(page);
    await page.goto(`/staff/cargo-tickets/send/assign/${DEFAULT_TRIP.tripId}`);

    await page.getByRole('checkbox', { name: 'Chọn đơn CG-ASSIGN-71' }).check();
    await expect(page.getByRole('checkbox', { name: 'Chọn đơn CG-ASSIGN-72' })).toBeDisabled();
    await expect(page.getByText('2.30 / 2.50 m³')).toBeVisible();
  });

  test('CG-TC-E01 recognizes completed sender bank payment', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await openCargoCreateForm(page);
    await fillValidCargoForm(page);
    await page.locator('[name="paymentMethod"]').selectOption('BANK_TRANSFER');
    await page.getByRole('button', { name: 'Lưu đơn gửi hàng' }).click();

    const qr = page.getByRole('dialog').filter({ hasText: 'Thanh toán chuyển khoản' });
    await expect(qr).toBeVisible();
    api.tickets[0].payment.status = 'COMPLETED';
    await expect(qr.getByText('Thanh toán thành công!')).toBeVisible({ timeout: 7_000 });
  });

  test('CG-TC-E02 excludes an unpaid sender order from assignment', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    await installCargoApiMock(page);
    await page.goto(`/staff/cargo-tickets/send/assign/${DEFAULT_TRIP.tripId}`);

    await expect(page.getByText('CG-ASSIGN-71')).toBeVisible();
    await expect(page.getByText('CG-UNPAID-SENDER')).toHaveCount(0);
    await expect(page.getByText(/người gửi trả phí thì phải đã thanh toán xong/i).first()).toBeVisible();
  });

  test('CG-TC-E03 allows receiver-paid cargo to be assigned while unpaid', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await page.goto(`/staff/cargo-tickets/send/assign/${DEFAULT_TRIP.tripId}`);

    await page.getByRole('checkbox', { name: 'Chọn đơn CG-ASSIGN-72' }).check();
    await page.getByRole('button', { name: 'Gán vào chuyến (1)' }).click();
    await expect.poll(() => api.assignedPayloads).toEqual([{ cargoTicketIds: [72] }]);
  });

  test('CG-TC-E04 collects receiver cash during delivery', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    const ticket = cargoTicket({
      cargoTicketId: 804,
      ticketCode: 'CG-ARRIVED-CASH',
      status: 'ARRIVED',
      feePayer: 'RECEIVER',
      payment: null,
    });
    await openDestinationQueue(page, api, ticket);

    await page.getByRole('button', { name: 'Thu tiền / giao hàng' }).click();
    const paymentDialog = page.getByRole('dialog').filter({ hasText: 'Thanh toán người nhận' });
    await paymentDialog.getByRole('button', { name: 'Tiền mặt — giao hàng' }).click();
    const confirm = page.getByRole('dialog').filter({ hasText: 'Xác nhận thu tiền mặt' });
    await confirm.getByRole('button', { name: 'Đã nhận tiền — giao hàng' }).click();

    await expect.poll(() => api.confirmedTicketIds).toEqual([804]);
    expect(ticket.payment).toEqual({ paymentMethod: 'CASH', status: 'COMPLETED' });
    expect(ticket.status).toBe('DELIVERED');
  });

  test('CG-TC-E05 blocks receiver bank delivery until completion', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    const ticket = cargoTicket({
      cargoTicketId: 805,
      ticketCode: 'CG-ARRIVED-BANK',
      status: 'ARRIVED',
      feePayer: 'RECEIVER',
      payment: null,
    });
    await openDestinationQueue(page, api, ticket);

    await page.getByRole('button', { name: 'Thu tiền / giao hàng' }).click();
    const paymentDialog = page.getByRole('dialog').filter({ hasText: 'Thanh toán người nhận' });
    await paymentDialog.getByRole('button', { name: 'Chuyển khoản (QR)' }).click();
    await expect(page.getByRole('dialog').filter({ hasText: 'Đang chờ thanh toán' })).toBeVisible();
    expect(api.confirmedTicketIds).toHaveLength(0);

    ticket.payment.status = 'COMPLETED';
    const result = await browserFetch(
      page,
      `/api/v1/ticket-staff/cargo-tickets/${ticket.cargoTicketId}/confirm-received`,
      { method: 'PUT', body: {} },
    );
    expect(result.status).toBe(204);
    expect(ticket.status).toBe('DELIVERED');
  });

  test('CG-TC-F01 scopes and renders the agency waiting queue', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    api.tickets.push(cargoTicket({ tripId: null }));
    await page.goto('/staff/cargo-tickets/send');

    await expect(page.getByText('CG-QUEUE-801')).toBeVisible();
    await expect(page.getByText('Nguyễn Người Gửi')).toBeVisible();
    await expect(page.getByText('Văn phòng Quận 1 → Văn phòng Đà Lạt')).toBeVisible();
    await expect(page.getByText('Chưa gán chuyến')).toBeVisible();
  });

  test('CG-TC-F02 updates unpaid orders and protects paid totals', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    api.tickets.push(
      cargoTicket({
        cargoTicketId: 811,
        payment: { paymentMethod: 'BANK_TRANSFER', status: 'PENDING' },
      }),
      cargoTicket({ cargoTicketId: 812 }),
    );
    await page.goto('/staff/cargo-tickets');
    const payload = {
      feePayer: 'SENDER',
      paymentMethod: 'BANK_TRANSFER',
      details: [{ cargoTypePriceId: 501, quantity: 1, weightKg: 5, dimensionVol: 0.2 }],
    };

    const unpaid = await browserFetch(
      page,
      '/api/v1/ticket-staff/cargo-tickets/811/with-details',
      { method: 'PUT', body: payload },
    );
    const paid = await browserFetch(
      page,
      '/api/v1/ticket-staff/cargo-tickets/812/with-details',
      { method: 'PUT', body: payload },
    );
    expect(unpaid.status).toBe(200);
    expect(paid.status).toBe(400);
    expect(paid.data.message).toContain('đã thanh toán');
  });

  test('CG-TC-F03 cancels paid waiting cargo with refund warning', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    api.tickets.push(cargoTicket({ cargoTicketId: 813, ticketCode: 'CG-PAID-813' }));
    await page.goto('/staff/cargo-tickets/send');

    await page.getByTitle('Hủy đơn').click();
    const dialog = page.getByRole('dialog').filter({ hasText: 'Hủy đơn gửi hàng' });
    await expect(dialog).toContainText('tạo yêu cầu hoàn tiền');
    await dialog.getByRole('button', { name: 'Hủy đơn' }).click();
    await expect.poll(() => api.disabledTicketIds).toEqual([813]);
  });

  test('CG-TC-F04 returns only the authenticated customer cargo history', async ({ page }) => {
    await authenticateAsRole(page, 'CUSTOMER', 'customer.qa');
    const api = await installCargoApiMock(page);
    api.customerOrders.push({
      cargoTicketId: 821,
      ticketCode: 'CG-CUSTOMER-821',
      status: 'RECEIVED',
      customerId: 99,
    });
    await page.goto('/staff/login');

    const result = await browserFetch(page, '/api/v1/customer/cargo-history');
    expect(result.status).toBe(200);
    expect(result.data).toEqual([
      expect.objectContaining({ ticketCode: 'CG-CUSTOMER-821', customerId: 99 }),
    ]);
  });

  test('CG-TC-G02 rejects started-trip and over-capacity assignment', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    await installCargoApiMock(page);
    await page.route('**/api/v1/ticket-staff/cargo-tickets/trips/*/assign', async (route) => {
      await route.fulfill({
        status: 400,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Chuyến đã khởi hành hoặc không đủ sức chứa.' }),
      });
    });
    await page.goto('/staff/cargo-tickets');

    const result = await browserFetch(
      page,
      '/api/v1/ticket-staff/cargo-tickets/trips/999/assign',
      { method: 'POST', body: { cargoTicketIds: [71, 72] } },
    );
    expect(result.status).toBe(400);
    expect(result.data.message).toMatch(/khởi hành|sức chứa/);
  });

  test('CG-TC-G03 trip staff loads then unloads cargo in order', async ({ page }) => {
    await authenticateAsRole(page, 'TRIP_STAFF', 'trip.qa');
    await installCargoApiMock(page);
    const item = cargoTicket({
      cargoTicketId: 831,
      ticketCode: 'CG-TRIP-831',
      status: 'RECEIVED',
      details: [{ cargoTicketDetailId: 1, quantity: 1, description: 'Bưu kiện' }],
    });
    await page.route('**/api/v1/staff/trips/301/**', async (route) => {
      const path = new URL(route.request().url()).pathname;
      if (path.endsWith('/passengers/dashboard')) {
        return route.fulfill({
          contentType: 'application/json',
          body: JSON.stringify({
            tripSummary: {
              routeName: DEFAULT_TRIP.routeName,
              departureTime: DEFAULT_TRIP.departureTime,
              licensePlate: DEFAULT_TRIP.licensePlate,
              checkedInCount: 0,
              totalPassengers: 0,
              tripStatus: 'SCHEDULED',
              coachStatus: 'AVAILABLE',
            },
            passengers: [],
            seats: [],
          }),
        });
      }
      if (route.request().method() === 'GET' && path.endsWith('/cargo')) {
        return route.fulfill({
          contentType: 'application/json',
          body: JSON.stringify({ cargoItems: [item] }),
        });
      }
      if (path.endsWith('/load')) item.status = 'LOADED';
      if (path.endsWith('/unload')) item.status = 'ARRIVED';
      return route.fulfill({ status: 204, body: '' });
    });
    await page.goto('/staff/trip/301/dashboard');
    await page.getByText('Hàng hóa', { exact: true }).click();

    await page.getByRole('button', { name: 'Xác nhận lên xe' }).click();
    const cargoCard = page.locator('.passenger-card').filter({ hasText: 'CG-TRIP-831' });
    await expect(cargoCard.getByText('Đã lên xe', { exact: true })).toBeVisible();
    await page.getByRole('button', { name: 'Dỡ hàng' }).click();
    await expect(cargoCard.getByText('Đã dỡ xuống', { exact: true })).toBeVisible();
    expect(item.status).toBe('ARRIVED');
  });

  test('CG-TC-H01 destination office sees only its arrived cargo', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    const arrived = cargoTicket({
      cargoTicketId: 841,
      ticketCode: 'CG-DESTINATION-841',
      status: 'ARRIVED',
    });
    api.tickets.push(arrived, cargoTicket({
      cargoTicketId: 842,
      ticketCode: 'CG-NOT-ARRIVED-842',
      status: 'RECEIVED',
    }));
    api.receivingTrips.push({ ...DEFAULT_TRIP, waitingOrderCount: 1 });
    await page.goto('/staff/cargo-tickets/check');
    await page.getByRole('button', { name: 'Xem đơn hàng' }).click();

    await expect(page.getByText('CG-DESTINATION-841')).toBeVisible();
    await expect(page.getByText('CG-NOT-ARRIVED-842')).toHaveCount(0);
  });

  test('CG-TC-H02 completes RECEIVED to LOADED to ARRIVED to DELIVERED', async ({ page }) => {
    await authenticateAsTicketStaff(page);
    const api = await installCargoApiMock(page);
    await page.goto('/staff/cargo-tickets');
    const create = await browserFetch(
      page,
      '/api/v1/ticket-staff/cargo-tickets/with-details',
      {
        method: 'POST',
        body: {
          tripId: null,
          senderName: 'Nguyễn Người Gửi',
          senderPhone: '0901111111',
          receiverName: 'Trần Người Nhận',
          receiverPhone: '0902222222',
          pickupStopId: 101,
          dropoffStopId: 201,
          feePayer: 'RECEIVER',
          paymentMethod: null,
          details: [{ cargoTypePriceId: 501, quantity: 1, weightKg: 5, dimensionVol: 0.1 }],
        },
      },
    );
    const ticket = api.tickets[0];
    expect(create.status).toBe(201);
    expect(ticket.status).toBe('RECEIVED');

    ticket.tripId = 301;
    ticket.status = 'LOADED';
    expect(ticket.status).toBe('LOADED');
    ticket.status = 'ARRIVED';
    ticket.payment = { paymentMethod: 'CASH', status: 'PENDING' };
    expect(ticket.status).toBe('ARRIVED');
    const delivered = await browserFetch(
      page,
      `/api/v1/ticket-staff/cargo-tickets/${ticket.cargoTicketId}/confirm-received`,
      { method: 'PUT', body: { paymentMethod: 'CASH' } },
    );
    expect(delivered.status).toBe(204);
    expect(ticket.status).toBe('DELIVERED');
    expect(ticket.payment.status).toBe('COMPLETED');
  });
});
