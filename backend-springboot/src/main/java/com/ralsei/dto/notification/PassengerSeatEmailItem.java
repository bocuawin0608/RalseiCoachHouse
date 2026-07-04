package com.ralsei.dto.notification;

public record PassengerSeatEmailItem(
    String seatCode,
    String passengerName,
    String passengerPhone,
    String qrImageUrlOrCid
) {}
