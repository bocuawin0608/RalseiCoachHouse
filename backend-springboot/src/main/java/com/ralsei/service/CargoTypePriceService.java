package com.ralsei.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import com.ralsei.dto.request.cargotypeprice.CargoTypePriceRequest;
import com.ralsei.dto.response.cargotypeprice.CargoTypePriceResponse;

public interface CargoTypePriceService {
    Page<CargoTypePriceResponse> getAllCargoTypePrices(int cargoTypeId, String search, @NonNull Pageable pageable);

    CargoTypePriceResponse getCargoTypePriceById(int id);

    CargoTypePriceResponse createCargoTypePrice(CargoTypePriceRequest request);

    CargoTypePriceResponse updateCargoTypePrice(int id, @NonNull CargoTypePriceRequest request);

    void deleteCargoTypePrice(int id);
}
