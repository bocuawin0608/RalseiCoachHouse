package com.ralsei.service;

import org.springframework.lang.NonNull;

import com.ralsei.dto.request.cargotypeprice.CargoTypePriceRequest;
import com.ralsei.dto.response.cargotypeprice.CargoTypePriceResponse;
import com.ralsei.dto.response.PagedResponse;

/**
 * Provides the business service contract for cargo type price.
 */
public interface CargoTypePriceService {
    PagedResponse<CargoTypePriceResponse> getAllCargoTypePrices(int cargoTypeId, String search, int page, int size);

    CargoTypePriceResponse getCargoTypePriceById(int id);

    CargoTypePriceResponse createCargoTypePrice(CargoTypePriceRequest request);

    CargoTypePriceResponse updateCargoTypePrice(int id, @NonNull CargoTypePriceRequest request);

    void deleteCargoTypePrice(int id);
}
