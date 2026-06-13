USE vexedb
SELECT a.username, r.roleName
FROM account a JOIN account_role ar ON a.accountId = ar.accountId JOIN role r on ar.roleId = r.roleId
SELECT *
FROM coach
SELECT *
FROM coach_type
SELECT *
FROM coach_stop
SELECT *
FROM trip
SELECT *
FROM trip_seat
SELECT *
FROM route_stop
SELECT *
FROM trip_seat

-- My new view all available trip query
SELECT t.tripId,
    t.[status] AS "TRANG THAI CHUYEN DI",
    c.manufacturer,
    ct.coachTypeName,
    c.licensePlate,
    c.[status] AS "TINH TRANG XE",
    CAST(t.departureTime AS DATE) AS departureDate, 
    CAST(t.departureTime AS TIME) AS departureTime,
    (SELECT COUNT(*)
    FROM trip_seat ts
    WHERE ts.tripId = t.tripId AND ts.status = 'Available') AS availableSeats,
    (SELECT COUNT(*)
    FROM trip_seat ts
    WHERE ts.tripId = t.tripId) AS totalSeats
FROM trip t
    JOIN coach c ON c.coachId = t.coachId
    JOIN coach_type ct ON ct.coachTypeId = c.coachTypeId
    JOIN trip_seat ts ON ts.tripId = t.tripId
    LEFT JOIN coach_type_price ctp ON ctp.coachTypeId = ct.coachTypeId
    WHERE CAST(t.departureTime AS DATE) = '2026-01-02'
    ORDER BY t.departureTime ASC
