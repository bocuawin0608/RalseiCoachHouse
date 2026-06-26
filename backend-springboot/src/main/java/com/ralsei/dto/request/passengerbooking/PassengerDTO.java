package com.ralsei.dto.request.passengerbooking;

public record PassengerDTO(
    Integer tripSeatId,
    String fullname,
    String phone,
    String email,
    AccompaniedChildDTO accompaniedChild
) {}
