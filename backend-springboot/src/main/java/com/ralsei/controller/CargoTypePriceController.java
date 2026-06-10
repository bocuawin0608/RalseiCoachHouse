package com.ralsei.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ralsei.dto.request.cargotypeprice.CargoTypePriceRequest;
import com.ralsei.dto.response.cargotypeprice.CargoTypePriceResponse;
import com.ralsei.service.CargoTypePriceService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/manager/cargo-type-prices")
@RequiredArgsConstructor
@Validated
public class CargoTypePriceController {

    private final CargoTypePriceService cargoTypePriceService;

    @GetMapping()
    public ResponseEntity<Page<CargoTypePriceResponse>> getCargoTypePrices(
            @RequestParam(required = false) int cargoTypeId,
            @RequestParam(required = false) String search,
            @NonNull Pageable pageable) {
        return ResponseEntity.ok(cargoTypePriceService.getAllCargoTypePrices(cargoTypeId, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoTypePriceResponse> getCargoTypePriceById(
            @PathVariable @Min(value = 1, message = "ID của bảng giá phải lớn hơn 0.") int id) {
        return ResponseEntity.ok(cargoTypePriceService.getCargoTypePriceById(id));
    }

    @PostMapping()
    public ResponseEntity<CargoTypePriceResponse> createCargoTypePrice(
            @Valid @RequestBody CargoTypePriceRequest request) {
        CargoTypePriceResponse response = cargoTypePriceService.createCargoTypePrice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CargoTypePriceResponse> updateCargoTypePrice(
            @PathVariable @Min(value = 1, message = "ID của bảng giá phải lớn hơn 0.") int id,
            @Valid @RequestBody @NonNull CargoTypePriceRequest request) {
        CargoTypePriceResponse response = cargoTypePriceService.updateCargoTypePrice(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargoTypePrice(
            @PathVariable @Min(value = 1, message = "ID của bảng giá phải lớn hơn 0.") int id) {
        cargoTypePriceService.deleteCargoTypePrice(id);
        return ResponseEntity.noContent().build();
    }
}
