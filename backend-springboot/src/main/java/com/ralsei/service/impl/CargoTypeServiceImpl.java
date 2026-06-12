package com.ralsei.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import java.util.Objects;

import com.ralsei.dto.request.cargotype.CargoTypeRequest;
import com.ralsei.dto.response.cargotype.CargoTypeResponse;
import com.ralsei.dto.response.PagedResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CargoType;
import com.ralsei.repository.CargoTypeRepository;
import com.ralsei.repository.CargoTypePriceRepository;
import com.ralsei.model.CargoTypePrice;
import com.ralsei.service.CargoTypeService;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CargoTypeServiceImpl implements CargoTypeService {

    private final CargoTypeRepository cargoTypeRepository;
    private final CargoTypePriceRepository cargoTypePriceRepository;

    @Override
    public PagedResponse<CargoTypeResponse> getAllCargoTypes(String search, Boolean isActive, int page, int size) {
        Pageable sortedPageable = PageRequest.of(page, size, Sort.by("cargoTypeId").descending());
        
        String processedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<CargoType> pageResult = cargoTypeRepository.filterCargoTypes(processedSearch, isActive, sortedPageable);
        
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
    public CargoTypeResponse getCargoTypeById(int id) {
        CargoType cargoType = findCargoTypeOrThrow(id);
        return mapToResponse(cargoType);
    }

    @Override
    public CargoTypeResponse createCargoType(CargoTypeRequest request) {
        if (cargoTypeRepository.existsByCargoTypeNameIgnoreCase(request.getCargoTypeName().trim())) {
            throw new IllegalArgumentException("Loại hàng hóa đã tồn tại.");
        }

        CargoType cargoType = Objects.requireNonNull(CargoType.builder()
                .cargoTypeName(request.getCargoTypeName().trim())
                .isActive(true)
                .build());

        CargoType saved = cargoTypeRepository.save(cargoType);
        return mapToResponse(saved);
    }

    @Override
    public CargoTypeResponse updateCargoType(int id, @NonNull CargoTypeRequest request) {
        CargoType cargoType = findCargoTypeOrThrow(id);

        if (!cargoType.getCargoTypeName().equalsIgnoreCase(request.getCargoTypeName().trim())) {
            if (cargoTypeRepository.existsByCargoTypeNameIgnoreCase(request.getCargoTypeName().trim())) {
                throw new IllegalArgumentException("Loại hàng hóa đã tồn tại.");
            }
        }

        cargoType.setCargoTypeName(request.getCargoTypeName().trim());

        CargoType saved = cargoTypeRepository.save(cargoType);
        return mapToResponse(saved);
    }

    @Override
    public void softDeleteCargoType(int id) {
        CargoType cargoType = findCargoTypeOrThrow(id);
        cargoType.setActive(false);
        cargoTypeRepository.save(cargoType);
        
        java.util.List<CargoTypePrice> prices = cargoTypePriceRepository.findByCargoTypeId(id);
        if (prices != null && !prices.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (CargoTypePrice price : prices) {
                price.setEndEffectiveDate(now);
            }
            cargoTypePriceRepository.saveAll(prices);
        }
    }

    @Override
    public void restoreCargoType(int id) {
        CargoType cargoType = findCargoTypeOrThrow(id);
        cargoType.setActive(true);
        cargoTypeRepository.save(cargoType);
    }

    private CargoType findCargoTypeOrThrow(int id) {
        return cargoTypeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy loại hàng hóa có ID là: " + id));
    }

    private CargoTypeResponse mapToResponse(CargoType cargoType) {
        return CargoTypeResponse.builder()
                .cargoTypeId(cargoType.getCargoTypeId())
                .cargoTypeName(cargoType.getCargoTypeName())
                .isActive(cargoType.isActive())
                .build();
    }
}
