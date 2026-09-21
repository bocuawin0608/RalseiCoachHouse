const JSON_HEADERS = { 'content-type': 'application/json' };

export const DEFAULT_TRIP = {
  tripId: 301,
  routeId: 11,
  coachId: 401,
  coachTypeName: 'Limousine',
  departureTime: '2030-07-25T08:00:00',
  pickupTime: '2030-07-25T08:15:00',
  status: 'SCHEDULED',
  tripStatus: 'SCHEDULED',
  pickupStopId: 101,
  pickupStopName: 'Văn phòng Quận 1',
  pickupCity: 'TP. Hồ Chí Minh',
  dropoffStopId: 201,
  licensePlate: '51B-123.45',
  routeName: 'TP. Hồ Chí Minh - Đà Lạt',
  usedCargoVolume: 1.9,
  cargoCapacity: 2.5,
  full: false,
};

const ROUTE = {
  routeId: 11,
  routeName: DEFAULT_TRIP.routeName,
  routeStops: [
    {
      stopPointId: 101,
      stopPointName: 'Văn phòng Quận 1',
      city: 'TP. Hồ Chí Minh',
      stopOrder: 1,
    },
    {
      stopPointId: 150,
      stopPointName: 'Trạm nghỉ Đồng Nai',
      city: 'Đồng Nai',
      stopOrder: 2,
    },
    {
      stopPointId: 201,
      stopPointName: 'Văn phòng Đà Lạt',
      city: 'Đà Lạt',
      stopOrder: 3,
    },
  ],
};

const CARGO_TYPE = {
  cargoTypePriceId: 501,
  cargoTypeName: 'Bưu kiện',
  pricePerUnit: 10_000,
  unit: 'kiện',
};

function paged(content) {
  return {
    content,
    pageNumber: 0,
    pageSize: 10,
    totalElements: content.length,
    totalPages: content.length ? 1 : 0,
    last: true,
  };
}

async function fulfill(route, body, status = 200) {
  await route.fulfill({
    status,
    headers: JSON_HEADERS,
    body: JSON.stringify(body),
  });
}

function createTicket(payload, id) {
  const senderPays = payload.feePayer === 'SENDER';
  const bankPending = senderPays && payload.paymentMethod === 'BANK_TRANSFER';
  return {
    cargoTicketId: id,
    ticketCode: `CG-E2E-${id}`,
    ...payload,
    status: 'RECEIVED',
    pickupStopName: 'Văn phòng Quận 1',
    dropoffStopName: 'Văn phòng Đà Lạt',
    routeName: ROUTE.routeName,
    payment: senderPays
      ? {
          paymentMethod: payload.paymentMethod,
          status: bankPending ? 'PENDING' : 'COMPLETED',
          transactionId: `CARGO-E2E-${id}`,
        }
      : null,
    qrUrl: bankPending
      ? 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="20" height="20"/%3E'
      : null,
  };
}

export async function installCargoApiMock(page) {
  const state = {
    createdPayloads: [],
    disabledTicketIds: [],
    assignedPayloads: [],
    updatedPayloads: [],
    confirmedTicketIds: [],
    receiverPaymentMethods: [],
    tickets: [],
    receivingTrips: [],
    customerOrders: [],
  };

  const assignableTickets = [
    {
      cargoTicketId: 71,
      ticketCode: 'CG-ASSIGN-71',
      senderName: 'Nguyễn Người Gửi',
      senderPhone: '0901111111',
      receiverName: 'Trần Người Nhận',
      receiverPhone: '0902222222',
      pickupStopId: 101,
      pickupStopName: 'Văn phòng Quận 1',
      dropoffStopId: 201,
      dropoffStopName: 'Văn phòng Đà Lạt',
      status: 'RECEIVED',
      feePayer: 'SENDER',
      paymentMethod: 'CASH',
      paymentStatus: 'COMPLETED',
      occupiedVolume: 0.4,
      totalPrice: 310_000,
    },
    {
      cargoTicketId: 72,
      ticketCode: 'CG-ASSIGN-72',
      senderName: 'Lê Người Gửi',
      senderPhone: '0903333333',
      receiverName: 'Phạm Người Nhận',
      receiverPhone: '0904444444',
      pickupStopId: 101,
      pickupStopName: 'Văn phòng Quận 1',
      dropoffStopId: 201,
      dropoffStopName: 'Văn phòng Đà Lạt',
      status: 'RECEIVED',
      feePayer: 'RECEIVER',
      paymentMethod: null,
      paymentStatus: null,
      occupiedVolume: 0.3,
      totalPrice: 235_000,
    },
  ];

  // Restrict interception to the HTTP API. A broad **/api/** pattern would
  // also intercept Vite source modules located in frontend `api` directories.
  await page.route('**/api/v1/**', async (route) => {
    const request = route.request();
    const method = request.method();
    const url = new URL(request.url());
    const path = url.pathname;

    if (method === 'GET' && path === '/api/v1/ticket-staff/cargo-tickets/form-options') {
      return fulfill(route, {
        customers: [],
        stops: [
          {
            stopPointId: 101,
            stopPointName: 'Văn phòng Quận 1',
            city: 'TP. Hồ Chí Minh',
          },
          {
            stopPointId: 201,
            stopPointName: 'Văn phòng Đà Lạt',
            city: 'Đà Lạt',
          },
        ],
        sellers: [{ staffId: 7, staffName: 'Nhân viên QA', username: 'ticket.qa' }],
        handlers: [],
        drivers: [],
        agencyPickupStopId: 101,
        agencyPickupStopName: 'Văn phòng Quận 1',
        agencyCity: 'TP. Hồ Chí Minh',
        defaultRouteId: 11,
        defaultRouteName: ROUTE.routeName,
      });
    }

    if (method === 'GET' && path === '/api/v1/routes/dropdown') {
      return fulfill(route, [{ routeId: 11, routeName: ROUTE.routeName }]);
    }
    if (method === 'GET' && path === '/api/v1/routes/11') {
      return fulfill(route, ROUTE);
    }
    if (method === 'GET' && path === '/api/v1/manager/cargo-types') {
      return fulfill(route, {
        content: [CARGO_TYPE],
        page: { totalElements: 1, totalPages: 1 },
      });
    }
    if (method === 'GET' && path === '/api/v1/ticket-staff/cargo-tickets/contacts/search') {
      return fulfill(route, []);
    }
    if (method === 'GET' && path === '/api/v1/ticket-staff/cargo-tickets/trips-by-stops') {
      return fulfill(route, [DEFAULT_TRIP]);
    }
    if (method === 'POST' && path === '/api/v1/ticket-staff/cargo-tickets/calculate-price') {
      const payload = request.postDataJSON();
      const price = Math.round((Number(payload.dimensionVol) / 1.2) * 300 * 3000)
        + CARGO_TYPE.pricePerUnit;
      return fulfill(route, { calculatedPrice: price * Number(payload.quantity) });
    }
    if (method === 'POST' && path === '/api/v1/ticket-staff/cargo-tickets/with-details') {
      const payload = request.postDataJSON();
      if (
        payload.tripId
        && payload.feePayer === 'SENDER'
        && payload.paymentMethod === 'BANK_TRANSFER'
      ) {
        return fulfill(route, {
          message: 'Người gửi thanh toán chuyển khoản: tạo đơn chưa gán chuyến.',
        }, 400);
      }
      state.createdPayloads.push(payload);
      const ticket = createTicket(payload, 900 + state.createdPayloads.length);
      state.tickets.unshift(ticket);
      return fulfill(route, ticket, 201);
    }

    const ticketMatch = path.match(/^\/api\/v1\/ticket-staff\/cargo-tickets\/(\d+)$/);
    if (method === 'GET' && ticketMatch) {
      const ticket = state.tickets.find(
        (item) => item.cargoTicketId === Number(ticketMatch[1]),
      );
      return ticket
        ? fulfill(route, ticket)
        : fulfill(route, { message: 'Không tìm thấy đơn gửi hàng.' }, 404);
    }

    const disableMatch = path.match(
      /^\/api\/v1\/ticket-staff\/cargo-tickets\/(\d+)\/disable$/,
    );
    if (method === 'PUT' && disableMatch) {
      state.disabledTicketIds.push(Number(disableMatch[1]));
      return fulfill(route, null, 204);
    }

    const updateMatch = path.match(
      /^\/api\/v1\/ticket-staff\/cargo-tickets\/(\d+)\/with-details$/,
    );
    if (method === 'PUT' && updateMatch) {
      const id = Number(updateMatch[1]);
      const ticket = state.tickets.find((item) => item.cargoTicketId === id);
      const payload = request.postDataJSON();
      if (ticket?.payment?.status === 'COMPLETED') {
        return fulfill(route, {
          message: 'Đơn đã thanh toán, không thể thay đổi số tiền.',
        }, 400);
      }
      state.updatedPayloads.push({ id, payload });
      return fulfill(route, { ...ticket, ...payload });
    }

    const confirmMatch = path.match(
      /^\/api\/v1\/ticket-staff\/cargo-tickets\/(\d+)\/confirm-received$/,
    );
    if (method === 'PUT' && confirmMatch) {
      const id = Number(confirmMatch[1]);
      const ticket = state.tickets.find((item) => item.cargoTicketId === id);
      const payload = request.postDataJSON() || {};
      if (
        ticket?.feePayer === 'RECEIVER'
        && ticket?.payment?.paymentMethod === 'BANK_TRANSFER'
        && ticket.payment.status !== 'COMPLETED'
      ) {
        return fulfill(route, {
          message: 'Người nhận chưa chuyển khoản xong.',
        }, 400);
      }
      if (payload.paymentMethod === 'CASH') {
        ticket.payment = { paymentMethod: 'CASH', status: 'COMPLETED' };
      }
      if (ticket) ticket.status = 'DELIVERED';
      state.confirmedTicketIds.push(id);
      return fulfill(route, null, 204);
    }

    const receiverMethodMatch = path.match(
      /^\/api\/v1\/ticket-staff\/cargo-tickets\/(\d+)\/receiver-payment-method$/,
    );
    if (method === 'PUT' && receiverMethodMatch) {
      const id = Number(receiverMethodMatch[1]);
      const payload = request.postDataJSON();
      const ticket = state.tickets.find((item) => item.cargoTicketId === id);
      if (ticket) {
        ticket.payment = {
          paymentMethod: payload.paymentMethod,
          status: 'PENDING',
          transactionId: `RECEIVER-${id}`,
        };
        ticket.qrUrl = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="20" height="20"/%3E';
      }
      state.receiverPaymentMethods.push({ id, ...payload });
      return fulfill(route, ticket);
    }

    if (method === 'GET' && path === '/api/v1/ticket-staff/cargo-tickets/upcoming-trips') {
      return fulfill(route, {
        ticketAgencyId: 1,
        ticketAgencyName: 'Văn phòng Quận 1',
        stopPointId: 101,
        stopPointName: 'Văn phòng Quận 1',
        city: 'TP. Hồ Chí Minh',
        trips: paged([DEFAULT_TRIP]),
      });
    }
    if (method === 'GET' && path === '/api/v1/ticket-staff/cargo-tickets/receiving-trips') {
      return fulfill(route, {
        ticketAgencyId: 2,
        ticketAgencyName: 'Văn phòng Đà Lạt',
        stopPointId: 201,
        stopPointName: 'Văn phòng Đà Lạt',
        city: 'Đà Lạt',
        trips: paged(state.receivingTrips),
      });
    }
    if (method === 'GET' && path === '/api/v1/ticket-staff/cargo-tickets') {
      const status = url.searchParams.get('status');
      return fulfill(
        route,
        paged(state.tickets.filter((ticket) => !status || ticket.status === status)),
      );
    }

    if (method === 'GET' && path === '/api/v1/customer/cargo-history') {
      return fulfill(route, state.customerOrders);
    }

    if (
      method === 'GET'
      && path === `/api/v1/ticket-staff/cargo-tickets/trips/${DEFAULT_TRIP.tripId}/assignable`
    ) {
      return fulfill(route, {
        tripId: DEFAULT_TRIP.tripId,
        usedCargoVolume: 1.9,
        cargoCapacity: 2.5,
        tickets: assignableTickets,
      });
    }
    if (
      method === 'POST'
      && path === `/api/v1/ticket-staff/cargo-tickets/trips/${DEFAULT_TRIP.tripId}/assign`
    ) {
      const payload = request.postDataJSON();
      state.assignedPayloads.push(payload);
      return fulfill(route, {
        tripId: DEFAULT_TRIP.tripId,
        assignedCount: payload.cargoTicketIds.length,
        usedCargoVolume: 2.3,
        cargoCapacity: 2.5,
        assignedTickets: [],
      });
    }

    return fulfill(
      route,
      { message: `Unhandled E2E API route: ${method} ${path}` },
      501,
    );
  });

  return state;
}
