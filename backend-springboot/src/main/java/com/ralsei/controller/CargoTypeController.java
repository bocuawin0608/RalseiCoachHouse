package com.ralsei.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.cargotype.CargoTypeManagementRequest;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.dto.response.cargotype.CargoTypeResponse;
import com.ralsei.service.CargoTypeService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/manager/cargo-types")
@RequiredArgsConstructor
@Validated
public class CargoTypeController {

    private final CargoTypeService cargoTypeService;

    /**
     * Displays cargo type rows with their surcharge unit and price in one result.
     */
    @GetMapping()
    public ResponseEntity<PagedResponse<CargoTypeResponse>> getCargoTypes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cargoTypeService.getAllCargoTypes(search, isActive, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoTypeResponse> getCargoTypeById(
            @PathVariable @Min(value = 1, message = "ID của Loại hàng hóa phải lớn hơn 0.") int id) {
        return ResponseEntity.ok(cargoTypeService.getCargoTypeById(id));
    }

    /**
     * Creates the staff-managed cargo type and its surcharge row together.
     */
    @PostMapping()
    public ResponseEntity<CargoTypeResponse> createCargoType(@Valid @RequestBody CargoTypeManagementRequest request) {
        CargoTypeResponse response = cargoTypeService.createCargoTypeManagement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates every field shown on the staff cargo type management screen.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CargoTypeResponse> updateCargoType(
            @PathVariable @Min(value = 1, message = "ID của Loại hàng hóa phải lớn hơn 0.") int id,
            @Valid @RequestBody @NonNull CargoTypeManagementRequest request) {
        CargoTypeResponse response = cargoTypeService.updateCargoTypeManagement(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteCargoType(
            @PathVariable @Min(value = 1, message = "ID của Loại hàng hóa phải lớn hơn 0.") int id) {
        cargoTypeService.softDeleteCargoType(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreCargoType(
            @PathVariable @Min(value = 1, message = "ID của Loại hàng hóa phải lớn hơn 0.") int id) {
        cargoTypeService.restoreCargoType(id);
        return ResponseEntity.noContent().build();
    }
}
