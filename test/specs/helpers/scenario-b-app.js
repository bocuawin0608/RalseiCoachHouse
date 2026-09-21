import { expect } from '@playwright/test';
import { SEARCH_DATE, baseTrip, pageOf } from '../fixtures/scenario-b-fixture.js';

const json = (route, body, status = 200) =>
  route.fulfill({ status, contentType: 'application/json', body: JSON.stringify(body) });

/**
 * Isolates the React search page while preserving its real rendering, state,
 * request serialization, filtering, and navigation code.
 */
export async function mockTripSearchApi(page, responseForSearch) {
  const state = { searches: [] };

  await page.route('**/api/v1/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());

    if (url.pathname.endsWith('/routes/customer-locations')) {
      return json(route, [
        { locationName: 'Hà Nội' },
        { locationName: 'Quảng Bình' },
      ]);
    }

    if (url.pathname.endsWith('/trips/home')) {
      state.searches.push(url);
      const response = await responseForSearch(url, state.searches.length);
      return json(route, response);
    }

    if (/\/trips\/[^/]+\/stops$/.test(url.pathname)) return json(route, []);
    return json(route, {});
  });

  return state;
}

export async function openAndSearch(page) {
  await page.goto('/');
  await page.getByRole('textbox', { name: 'Điểm đi', exact: true }).fill('Hà Nội');
  await page.getByRole('textbox', { name: 'Điểm đến', exact: true }).fill('Quảng Bình');
  await page.getByLabel('Ngày đi').fill(SEARCH_DATE);
  await page.getByRole('button', { name: 'Tìm lịch trình' }).click();
  await expect(page.getByText(/Kết quả lịch trình:/)).toBeVisible();
}

export async function mockSingleTrip(page, override = {}) {
  return mockTripSearchApi(page, async () => pageOf([{ ...baseTrip, ...override }]));
}

export const tripCards = (page) => page.locator('.advanced-trip-card');
