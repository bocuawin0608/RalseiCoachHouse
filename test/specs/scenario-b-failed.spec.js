import { test, expect } from '@playwright/test';
import { baseTrip, pageOf } from './fixtures/scenario-b-fixture.js';
import {
  mockSingleTrip,
  mockTripSearchApi,
  openAndSearch,
  tripCards,
} from './helpers/scenario-b-app.js';

test.describe('Scenario B - failed cases only', () => {
  test('BK-TC-B02 a sold-out trip cannot be selected', async ({ page }) => {
    await mockSingleTrip(page, { availableSeats: 0, totalSeats: 32 });
    await openAndSearch(page);

    await expect(tripCards(page)).toHaveCount(0);
    await expect(page.getByText(/hết chỗ/)).toBeVisible();
  });

  test('BK-TC-B03 null available seats falls back to total seats', async ({ page }) => {
    await mockSingleTrip(page, { availableSeats: null, totalSeats: 32 });
    await openAndSearch(page);

    const card = tripCards(page).first();
    await expect(card).toContainText('Còn 32 chỗ trống');
  });

  test('BK-TC-B06 malformed timestamp displays a safe placeholder or error', async ({ page }) => {
    await mockSingleTrip(page, { departureTime: 'not-a-timestamp', arrivalTime: 'still-not-a-time' });
    await openAndSearch(page);

    const card = tripCards(page).first();
    await expect(card).toBeVisible();
    await expect(card).toContainText('--:--');
    await expect(card).not.toContainText(/not-a|still-not/);
  });

  for (const [label, invalidPrice] of [['null', null], ['negative', -1], ['text', 'free']]) {
    test(`BK-TC-B07 invalid ${label} price is rejected`, async ({ page }) => {
      await mockSingleTrip(page, { seatPrice: invalidPrice });
      await openAndSearch(page);

      await expect(tripCards(page)).toHaveCount(0);
      await expect(page.getByText(/giá.*không hợp lệ|dữ liệu.*không hợp lệ/i)).toBeVisible();
    });
  }

  test('BK-TC-B19 Under 300,000 excludes the exact 300,000 boundary', async ({ page }) => {
    await mockTripSearchApi(page, async (url) => {
      const trip = { ...baseTrip, seatPrice: 300000 };
      if (!url.searchParams.has('maxPrice')) return pageOf([trip]);
      return pageOf(trip.seatPrice <= Number(url.searchParams.get('maxPrice')) ? [trip] : []);
    });
    await openAndSearch(page);
    await page.getByRole('button', { name: 'Dưới 300.000đ' }).click();

    await expect(tripCards(page)).toHaveCount(0);
  });

  test('BK-TC-B21 Over 500,000 excludes 500,000 and keeps values above 2,000,001', async ({ page }) => {
    const trips = [
      { ...baseTrip, tripId: 500, seatPrice: 500000 },
      { ...baseTrip, tripId: 2100, seatPrice: 2000001 },
    ];
    await mockTripSearchApi(page, async (url) => {
      if (!url.searchParams.has('minPrice')) return pageOf(trips);
      // The mock returns what the current query asks for, exposing wrong bounds.
      const min = Number(url.searchParams.get('minPrice'));
      const max = url.searchParams.has('maxPrice') ? Number(url.searchParams.get('maxPrice')) : Number.POSITIVE_INFINITY;
      return pageOf(trips.filter((trip) => trip.seatPrice >= min && trip.seatPrice <= max));
    });
    await openAndSearch(page);
    await page.getByRole('button', { name: 'Trên 500.000đ' }).click();

    await expect(tripCards(page)).toHaveCount(1);
    await expect(tripCards(page).first()).toContainText('2.000.001 đ');
  });

  test('BK-TC-B26 the latest rapid filter change wins', async ({ page }) => {
    await mockTripSearchApi(page, async (url) => {
      const layouts = url.searchParams.get('layouts') || '';
      if (layouts === 'Limousine') {
        await new Promise((resolve) => setTimeout(resolve, 350));
        return pageOf([{ ...baseTrip, coachTypeName: 'Limousine stale' }]);
      }
      if (layouts.includes('luxury')) {
        return pageOf([{ ...baseTrip, tripId: 202, coachTypeName: 'Luxury latest' }]);
      }
      return pageOf([baseTrip]);
    });
    await openAndSearch(page);
    await page.getByRole('button', { name: 'Xe Limousine VIP 20 phòng' }).click();
    await page.getByRole('button', { name: 'Xe Giường Nằm Luxury 32 chỗ' }).click();

    await expect(tripCards(page).first()).toContainText('Luxury latest');
    await page.waitForTimeout(450);
    await expect(tripCards(page).first()).toContainText('Luxury latest');
  });

  test('BK-TC-B34 a trip without an ID cannot be selected', async ({ page }) => {
    await mockSingleTrip(page, { tripId: null });
    await openAndSearch(page);

    await expect(tripCards(page)).toHaveCount(0);
    await expect(page).toHaveURL(/\/$/);
  });

  test('BK-TC-B37 a trip with only inactive seats cannot be selected', async ({ page }) => {
    await mockSingleTrip(page, { availableSeats: 0, totalSeats: 32 });
    await openAndSearch(page);

    await expect(tripCards(page)).toHaveCount(0);
  });

  test('BK-TC-B40 overlapping effective prices render one trip card', async ({ page }) => {
    await mockTripSearchApi(page, async () => pageOf([
      { ...baseTrip, seatPrice: 400000 },
      { ...baseTrip, seatPrice: 450000 },
    ]));
    await openAndSearch(page);

    await expect(tripCards(page)).toHaveCount(1);
  });

  test('BK-TC-B41 route names with extra hyphens remain complete', async ({ page }) => {
    await mockSingleTrip(page, { routeName: 'A - B - C' });
    await openAndSearch(page);

    await expect(tripCards(page).first()).toContainText('A');
    await expect(tripCards(page).first()).toContainText('B - C');
  });
});
