package com.ralsei.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.ralsei.dto.request.cargotype.CargoTypeRequest;
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

    @GetMapping()
    public ResponseEntity<Page<CargoTypeResponse>> getCargoTypes(
            @RequestParam(required = false) String search,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(cargoTypeService.getAllCargoTypes(search, pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<CargoTypeResponse> getCargoTypeById(
            @PathVariable @Min(value = 1, message = "ID của Loại hàng hóa phải lớn hơn 0.") Integer id) {
        return ResponseEntity.ok(cargoTypeService.getCargoTypeById(id));
    }

    @PostMapping()
    public ResponseEntity<CargoTypeResponse> createCargoType(@Valid @RequestBody CargoTypeRequest request) {
        CargoTypeResponse response = cargoTypeService.createCargoType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CargoTypeResponse> updateCargoType(
            @PathVariable @Min(value = 1, message = "ID của Loại hàng hóa phải lớn hơn 0.") Integer id,
            @Valid @RequestBody @NonNull CargoTypeRequest request) {
        CargoTypeResponse response = cargoTypeService.updateCargoType(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteCargoType(
            @PathVariable @Min(value = 1, message = "ID của Loại hàng hóa phải lớn hơn 0.") Integer id) {
        cargoTypeService.softDeleteCargoType(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreCargoType(
            @PathVariable @Min(value = 1, message = "ID của Loại hàng hóa phải lớn hơn 0.") Integer id) {
        cargoTypeService.restoreCargoType(id);
        return ResponseEntity.noContent().build();
    }
}
