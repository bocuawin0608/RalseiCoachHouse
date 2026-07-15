package com.ralsei.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

import com.ralsei.dto.request.cargotype.CargoTypeRequest;
import com.ralsei.dto.request.cargotype.CargoTypeManagementRequest;
import com.ralsei.dto.projection.cargotype.CargoTypeManagementProjection;
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
/**
 * Provides the cargo type service impl component for the application.
 */
public class CargoTypeServiceImpl implements CargoTypeService {

    private static final LocalDateTime DEFAULT_SURCHARGE_END_DATE = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
    private static final BigDecimal MAX_SURCHARGE_PRICE = new BigDecimal("9999999999999.99");

    private final CargoTypeRepository cargoTypeRepository;
    private final CargoTypePriceRepository cargoTypePriceRepository;

    @Override
    @Transactional(readOnly = true)
    /**
     * Returns the all cargo types.
     *
     * @param search the value supplied for this operation
     * @param isActive the value supplied for this operation
     * @param page the value supplied for this operation
     * @param size the value supplied for this operation
     *
     * @return the all cargo types
     */
    public PagedResponse<CargoTypeResponse> getAllCargoTypes(String search, Boolean isActive, int page, int size) {
        Pageable sortedPageable = PageRequest.of(page, size, Sort.by("cargoTypeId").descending());
        
        String processedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<CargoTypeManagementProjection> pageResult = cargoTypeRepository.filterCargoTypeManagementRows(
                processedSearch, isActive, sortedPageable);
        
        return new PagedResponse<>(
                pageResult.map(this::mapManagementProjectionToResponse).getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Returns the cargo type by id.
     *
     * @param id the value supplied for this operation
     *
     * @return the cargo type by id
     */
    public CargoTypeResponse getCargoTypeById(int id) {
        CargoType cargoType = findCargoTypeOrThrow(id);
        CargoTypePrice surcharge = cargoTypePriceRepository.findLatestByCargoTypeId(id).orElse(null);
        return mapToResponse(cargoType, surcharge);
    }

    @Override
    @Transactional
    /**
     * Creates the cargo type.
     *
     * @param request the value supplied for this operation
     *
     * @return the created cargo type
     */
    public CargoTypeResponse createCargoType(CargoTypeRequest request) {
        if (cargoTypeRepository.existsByCargoTypeNameIgnoreCase(request.getCargoTypeName().trim())) {
            throw new IllegalArgumentException("Loại hàng hóa đã tồn tại.");
        }

        CargoType cargoType = Objects.requireNonNull(CargoType.builder()
                .cargoTypeName(request.getCargoTypeName().trim())
                .isActive(true)
                .build());

        CargoType saved = cargoTypeRepository.save(cargoType);
        return mapToResponse(saved, null);
    }

    @Override
    @Transactional
    /**
     * Creates the cargo type management.
     *
     * @param request the value supplied for this operation
     *
     * @return the created cargo type management
     */
    public CargoTypeResponse createCargoTypeManagement(CargoTypeManagementRequest request) {
        validateManagementRequest(request);

        if (cargoTypeRepository.existsByCargoTypeNameIgnoreCase(request.getCargoTypeName().trim())) {
            throw new IllegalArgumentException("Loại hàng hóa đã tồn tại.");
        }

        CargoType cargoType = CargoType.builder()
                .cargoTypeName(request.getCargoTypeName().trim())
                .isActive(true)
                .build();

        CargoType savedCargoType = cargoTypeRepository.save(cargoType);
        CargoTypePrice savedSurcharge = cargoTypePriceRepository.save(buildSurcharge(savedCargoType.getCargoTypeId(), request));

        return mapToResponse(savedCargoType, savedSurcharge);
    }

    @Override
    @Transactional
    /**
     * Updates the cargo type.
     *
     * @param id the value supplied for this operation
     * @param request the value supplied for this operation
     *
     * @return the updated cargo type
     */
    public CargoTypeResponse updateCargoType(int id, @NonNull CargoTypeRequest request) {
        CargoType cargoType = findCargoTypeOrThrow(id);

        if (!cargoType.getCargoTypeName().equalsIgnoreCase(request.getCargoTypeName().trim())) {
            if (cargoTypeRepository.existsByCargoTypeNameIgnoreCase(request.getCargoTypeName().trim())) {
                throw new IllegalArgumentException("Loại hàng hóa đã tồn tại.");
            }
        }

        cargoType.setCargoTypeName(request.getCargoTypeName().trim());

        CargoType saved = cargoTypeRepository.save(cargoType);
        CargoTypePrice surcharge = cargoTypePriceRepository.findLatestByCargoTypeId(id).orElse(null);
        return mapToResponse(saved, surcharge);
    }

    @Override
    @Transactional
    /**
     * Updates the cargo type management.
     *
     * @param id the value supplied for this operation
     * @param request the value supplied for this operation
     *
     * @return the updated cargo type management
     */
    public CargoTypeResponse updateCargoTypeManagement(int id, @NonNull CargoTypeManagementRequest request) {
        validateManagementRequest(request);

        CargoType cargoType = findCargoTypeOrThrow(id);
        String normalizedName = request.getCargoTypeName().trim();

        if (!cargoType.getCargoTypeName().equalsIgnoreCase(normalizedName)
                && cargoTypeRepository.existsByCargoTypeNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Loại hàng hóa đã tồn tại.");
        }

        cargoType.setCargoTypeName(normalizedName);
        CargoType savedCargoType = cargoTypeRepository.save(cargoType);

        CargoTypePrice surcharge = cargoTypePriceRepository.findLatestByCargoTypeId(id)
                .orElseGet(() -> buildSurcharge(id, request));
        surcharge.setUnit(request.getUnit().trim());
        surcharge.setPricePerUnit(request.getPricePerUnit());
        if (surcharge.getStartEffectiveDate() == null) {
            surcharge.setStartEffectiveDate(LocalDateTime.now());
        }
        if (surcharge.getEndEffectiveDate() == null) {
            surcharge.setEndEffectiveDate(DEFAULT_SURCHARGE_END_DATE);
        }

        CargoTypePrice savedSurcharge = cargoTypePriceRepository.save(surcharge);
        return mapToResponse(savedCargoType, savedSurcharge);
    }

    @Override
    @Transactional
    /**
     * Executes the soft delete cargo type operation.
     *
     * @param id the value supplied for this operation
     */
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
    @Transactional
    /**
     * Executes the restore cargo type operation.
     *
     * @param id the value supplied for this operation
     */
    public void restoreCargoType(int id) {
        CargoType cargoType = findCargoTypeOrThrow(id);
        cargoType.setActive(true);
        cargoTypeRepository.save(cargoType);
    }

    private CargoType findCargoTypeOrThrow(int id) {
        return cargoTypeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy loại hàng hóa có ID là: " + id));
    }

    /**
     * Ensures the combined staff form never saves incomplete surcharge data.
     */
    private void validateManagementRequest(CargoTypeManagementRequest request) {
        if (request.getCargoTypeName() == null || request.getCargoTypeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên loại hàng là bắt buộc.");
        }
        if (request.getUnit() == null || request.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Đơn vị tính là bắt buộc.");
        }
        BigDecimal price = request.getPricePerUnit();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm.");
        }
        if (price.compareTo(MAX_SURCHARGE_PRICE) > 0) {
            throw new IllegalArgumentException("Đơn giá không được vượt quá 9.999.999.999.999,99.");
        }
    }

    /**
     * Builds the default open-ended surcharge row for cargo type management.
     */
    private CargoTypePrice buildSurcharge(int cargoTypeId, CargoTypeManagementRequest request) {
        return CargoTypePrice.builder()
                .cargoTypeId(cargoTypeId)
                .unit(request.getUnit().trim())
                .pricePerUnit(request.getPricePerUnit())
                .startEffectiveDate(LocalDateTime.now())
                .endEffectiveDate(DEFAULT_SURCHARGE_END_DATE)
                .build();
    }

    private CargoTypeResponse mapToResponse(CargoType cargoType, CargoTypePrice surcharge) {
        return CargoTypeResponse.builder()
                .cargoTypeId(cargoType.getCargoTypeId())
                .cargoTypeName(cargoType.getCargoTypeName())
                .isActive(cargoType.isActive())
                .cargoTypePriceId(surcharge == null ? null : surcharge.getCargoTypePriceId())
                .unit(surcharge == null ? null : surcharge.getUnit())
                .pricePerUnit(surcharge == null ? null : surcharge.getPricePerUnit())
                .build();
    }

    private CargoTypeResponse mapManagementProjectionToResponse(CargoTypeManagementProjection projection) {
        return CargoTypeResponse.builder()
                .cargoTypeId(projection.getCargoTypeId())
                .cargoTypeName(projection.getCargoTypeName())
                .isActive(projection.getActive())
                .cargoTypePriceId(projection.getCargoTypePriceId())
                .unit(projection.getUnit())
                .pricePerUnit(projection.getPricePerUnit())
                .build();
    }
}
