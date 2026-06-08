package com.ralsei.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.lang.NonNull;

import com.ralsei.dto.request.cargotype.CargoTypeRequest;
import com.ralsei.dto.response.cargotype.CargoTypeResponse;

public interface CargoTypeService {
    Page<CargoTypeResponse> getAllCargoTypes(String search, @NonNull Pageable pageable);

    CargoTypeResponse getCargoTypeById(int id);

    CargoTypeResponse createCargoType(CargoTypeRequest request);

    CargoTypeResponse updateCargoType(int id, @NonNull CargoTypeRequest request);

    void softDeleteCargoType(int id);

    void restoreCargoType(int id);
}
