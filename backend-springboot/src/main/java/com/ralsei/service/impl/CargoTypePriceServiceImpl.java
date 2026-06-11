package com.ralsei.service.impl;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.ralsei.dto.request.cargotypeprice.CargoTypePriceRequest;
import com.ralsei.dto.response.cargotypeprice.CargoTypePriceResponse;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CargoTypePrice;
import com.ralsei.repository.CargoTypePriceRepository;
import com.ralsei.repository.CargoTypeRepository;
import com.ralsei.service.CargoTypePriceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CargoTypePriceServiceImpl implements CargoTypePriceService {

    private final CargoTypePriceRepository cargoTypePriceRepository;
    private final CargoTypeRepository cargoTypeRepository;

    @Override
    public PagedResponse<CargoTypePriceResponse> getAllCargoTypePrices(int cargoTypeId, String search, int page, int size) {
        Pageable sortedPageable = PageRequest.of(page, size, Sort.by("cargoTypePriceId").descending());
        Page<CargoTypePrice> pageResult;

        if (cargoTypeId > 0) {
            pageResult = cargoTypePriceRepository.findByCargoTypeId(cargoTypeId, sortedPageable);
        } else if (search != null && !search.trim().isEmpty()) {
            pageResult = cargoTypePriceRepository.findByUnitContainingIgnoreCase(search.trim(), sortedPageable);
        } else {
            pageResult = cargoTypePriceRepository.findAll(sortedPageable);
        }

        return new PagedResponse<>(
                pageResult.map(this::mapToResponse).getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    @Override
    public CargoTypePriceResponse getCargoTypePriceById(int id) {
        CargoTypePrice cargoTypePrice = findCargoTypePriceOrThrow(id);
        return mapToResponse(cargoTypePrice);
    }

    @Override
    public CargoTypePriceResponse createCargoTypePrice(CargoTypePriceRequest request) {
        // Validate cargo type exists
        cargoTypeRepository.findById(request.getCargoTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy loại hàng hóa có ID là: " + request.getCargoTypeId()));

        CargoTypePrice cargoTypePrice = Objects.requireNonNull(CargoTypePrice.builder()
                .cargoTypeId(request.getCargoTypeId())
                .unit(request.getUnit().trim())
                .pricePerUnit(request.getPricePerUnit())
                .startEffectiveDate(request.getStartEffectiveDate())
                .endEffectiveDate(request.getEndEffectiveDate())
                .build());

        CargoTypePrice saved = cargoTypePriceRepository.save(cargoTypePrice);
        return mapToResponse(saved);
    }

    @Override
    public CargoTypePriceResponse updateCargoTypePrice(int id, @NonNull CargoTypePriceRequest request) {
        CargoTypePrice cargoTypePrice = findCargoTypePriceOrThrow(id);

        // Validate cargo type exists
        if (cargoTypePrice.getCargoTypeId() != request.getCargoTypeId()) {
            cargoTypeRepository.findById(request.getCargoTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy loại hàng hóa có ID là: " + request.getCargoTypeId()));
        }

        cargoTypePrice.setCargoTypeId(request.getCargoTypeId());
        cargoTypePrice.setUnit(request.getUnit().trim());
        cargoTypePrice.setPricePerUnit(request.getPricePerUnit());
        cargoTypePrice.setStartEffectiveDate(request.getStartEffectiveDate());
        cargoTypePrice.setEndEffectiveDate(request.getEndEffectiveDate());

        CargoTypePrice saved = cargoTypePriceRepository.save(cargoTypePrice);
        return mapToResponse(saved);
    }

    @Override
    public void deleteCargoTypePrice(int id) {
        CargoTypePrice cargoTypePrice = Objects.requireNonNull(findCargoTypePriceOrThrow(id));
        cargoTypePriceRepository.delete(cargoTypePrice);
    }

    private CargoTypePrice findCargoTypePriceOrThrow(int id) {
        return cargoTypePriceRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy bảng giá loại hàng hóa có ID là: " + id));
    }

    private CargoTypePriceResponse mapToResponse(CargoTypePrice cargoTypePrice) {
        return CargoTypePriceResponse.builder()
                .cargoTypePriceId(cargoTypePrice.getCargoTypePriceId())
                .cargoTypeId(cargoTypePrice.getCargoTypeId())
                .unit(cargoTypePrice.getUnit())
                .pricePerUnit(cargoTypePrice.getPricePerUnit())
                .startEffectiveDate(cargoTypePrice.getStartEffectiveDate())
                .endEffectiveDate(cargoTypePrice.getEndEffectiveDate())
                .build();
    }
}
