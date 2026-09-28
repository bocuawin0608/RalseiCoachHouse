package com.ralsei.staff.service;

import org.springframework.lang.NonNull;

import com.ralsei.staff.dto.request.cargotype.CargoTypeRequest;
import com.ralsei.staff.dto.request.cargotype.CargoTypeManagementRequest;
import com.ralsei.staff.dto.response.cargotype.CargoTypeResponse;
import com.ralsei.staff.dto.response.PagedResponse;

/**
 * Provides the business service contract for cargo type.
 */
public interface CargoTypeService {
    PagedResponse<CargoTypeResponse> getAllCargoTypes(String search, Boolean isActive, int page, int size);

    CargoTypeResponse getCargoTypeById(int id);

    CargoTypeResponse createCargoType(CargoTypeRequest request);

    CargoTypeResponse createCargoTypeManagement(CargoTypeManagementRequest request);

    CargoTypeResponse updateCargoType(int id, @NonNull CargoTypeRequest request);

    CargoTypeResponse updateCargoTypeManagement(int id, @NonNull CargoTypeManagementRequest request);

    void softDeleteCargoType(int id);

    void restoreCargoType(int id);
}
