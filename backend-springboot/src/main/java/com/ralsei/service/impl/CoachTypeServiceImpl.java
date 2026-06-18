package com.ralsei.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ralsei.dto.projection.CoachTypeProjection;
import com.ralsei.dto.request.coachtype.CoachTypeCreateRequest;
import com.ralsei.dto.request.coachtype.CoachTypeFilterRequest;
import com.ralsei.dto.request.coachtype.CoachTypeUpdateInfoRequest;
import com.ralsei.dto.request.coachtype.CoachTypeUpdatePriceRequest;
import com.ralsei.dto.request.coachtype.CoachTypeUpdateSeatmapRequest;
import com.ralsei.dto.response.coachtype.CoachTypeDetailResponse;
import com.ralsei.dto.response.coachtype.CoachTypeDropdownDTO;
import com.ralsei.dto.response.coachtype.CoachTypeResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CoachType;
import com.ralsei.model.CoachTypePrice;
import com.ralsei.model.enums.CoachStatus;
import com.ralsei.repository.CoachRepository;
import com.ralsei.repository.CoachTypeRepository;
import com.ralsei.service.CoachTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoachTypeServiceImpl implements CoachTypeService {

    private final CoachTypeRepository coachTypeRepo;
    private final CoachRepository coachRepo;
    private final ObjectMapper objectMapper;

    private final LocalDateTime infiniteDateTime = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private record ProcessedSeatLayout(int totalSeat, String seatLayout) {
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CoachTypeResponse> filterCoachTypes(CoachTypeFilterRequest filterRequest, Pageable pageable) {

        if (filterRequest.minPrice() != null && filterRequest.maxPrice() != null
                && filterRequest.minPrice().compareTo(filterRequest.maxPrice()) > 0) {
            throw new IllegalArgumentException("Giá tối thiểu không thể lớn hơn Giá tối đa!");
        }

        if (filterRequest.minSeats() != null && filterRequest.maxSeats() != null
                && filterRequest.minSeats() > filterRequest.maxSeats()) {
            throw new IllegalArgumentException("Số ghế tối thiểu không thể lớn hơn Số ghế tối đa!");
        }

        Page<CoachTypeProjection> projections = coachTypeRepo.searchCoachTypes(filterRequest, LocalDateTime.now(),
                pageable);

        return projections.map(type -> new CoachTypeResponse(
                type.getCoachTypeId(),
                type.getCoachTypeName(),
                type.getTotalSeat(),
                type.getCurrentPrice(),
                type.getIsActive(),
                type.getTotalCoach()));
    }

    @Transactional
    @Override
    public Integer createCoachType(CoachTypeCreateRequest request) {
        if (coachTypeRepo.existsByCoachTypeNameIgnoreCase(request.coachTypeName())) {
            throw new IllegalArgumentException("Tên loại xe đã tồn tại trong hệ thống!");
        }

        ProcessedSeatLayout verifiedSeatLayout = validateSeatLayout(request.seatLayout());

        CoachType newCoachType = CoachType.builder()
                .coachTypeName(request.coachTypeName())
                .totalSeat(verifiedSeatLayout.totalSeat())
                .seatLayout(verifiedSeatLayout.seatLayout())
                .isActive(true)
                .build();

        CoachTypePrice price = CoachTypePrice.builder()
                .coachType(newCoachType)
                .seatPrice(request.seatPrice())
                .startEffectiveDate(LocalDateTime.now())
                .endEffectiveDate(infiniteDateTime)
                .build();
        newCoachType.setCoachTypePrices(List.of(price));

        CoachType savedCoachType = coachTypeRepo.save(newCoachType);
        return savedCoachType.getCoachTypeId();
    }

    private ProcessedSeatLayout validateSeatLayout(String rawSeatLayout) {
        int calculatedTotalSeat = 0;
        try {
            JsonNode rootNode = objectMapper.readTree(rawSeatLayout);
            int totalFloors = rootNode.path("totalFloors").asInt(0);
            int declaredRows = rootNode.path("rows").asInt(0);
            int declaredCols = rootNode.path("cols").asInt(0);
            JsonNode floorsNode = rootNode.get("floors");

            if (declaredRows < 1 || declaredRows > 11) {
                throw new IllegalArgumentException("Số hàng trong sơ đồ ghế phải nằm trong khoảng từ 1 đến 11.");
            }
            if (declaredCols < 1 || declaredCols > 5) {
                throw new IllegalArgumentException("Số cột trong sơ đồ ghế phải nằm trong khoảng từ 1 đến 5.");
            }

            if (totalFloors < 1 || totalFloors > 2) {
                throw new IllegalArgumentException("Số tầng trong sơ đồ phải nằm trong khoảng từ 1 đến 2.");
            }

            if (floorsNode == null || !floorsNode.isArray() || floorsNode.size() != totalFloors) {
                throw new IllegalArgumentException("Dữ liệu tầng ghế bị thiếu hoặc không khớp.");
            }

            for(JsonNode floor : floorsNode) {

                for (JsonNode rowNode : floor) {
                    for (JsonNode cellNode : rowNode) {
                        if (cellNode.asText().equals("SEAT")) {
                            calculatedTotalSeat++;
                        }
                    }
                }
                //TODO: chưa có cơ chế check 1 cột/hàng bất kỳ trống hoàn toàn ghế (kiểu đang dùng cols/rows cho các tầng -> sẽ có nơi bị trống full)
            }

            if (calculatedTotalSeat < 1 || calculatedTotalSeat > 44) {
                throw new IllegalArgumentException("Sơ đồ ghế không thể <1 hoặc >44 ghế.");
            }

            return new ProcessedSeatLayout(calculatedTotalSeat, rootNode.toString());

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Định dạng JSON của sơ đồ ghế không hợp lệ.", e);
        }
    }

    @Transactional
    @Override
    public CoachTypeDetailResponse getCoachTypeDetail(Integer id) {
        CoachType coachType = coachTypeRepo.findById(id).orElseThrow(
                () -> {
                    throw new ResourceNotFoundException("Không tìm thấy loại xe có ID là: " + id);
                });

        BigDecimal currentPrice = coachType.getCoachTypePrices().stream()
                .filter(price -> price.getStartEffectiveDate().isBefore(LocalDateTime.now())
                        && price.getEndEffectiveDate().isAfter(LocalDateTime.now()))
                .findFirst()
                .map(price -> price.getSeatPrice())
                .orElse(BigDecimal.ZERO);

        return new CoachTypeDetailResponse(
                coachType.getCoachTypeId(),
                coachType.getCoachTypeName(),
                coachType.getTotalSeat(),
                currentPrice,
                coachType.isActive(),
                coachType.getSeatLayout());
    }

    @Transactional
    @Override
    public void updateCoachTypeInfo(Integer id, CoachTypeUpdateInfoRequest updateRequest) {
        CoachType coachType = coachTypeRepo.findById(id).orElseThrow(
                () -> {
                    throw new ResourceNotFoundException("Không tìm thấy loại xe có ID là: " + id);
                });

        if (!coachType.getCoachTypeName().equalsIgnoreCase(updateRequest.coachTypeName())) {
            if (coachTypeRepo.existsByCoachTypeNameIgnoreCase(updateRequest.coachTypeName())) {
                throw new IllegalArgumentException("Tên loại xe đã tồn tại trong hệ thống!");
            }
            coachType.setCoachTypeName(updateRequest.coachTypeName());
        }

        if(coachRepo.existsByCoachType_CoachTypeIdAndStatusNot(id, CoachStatus.RETIRED) && !updateRequest.isActive()) {
            throw new IllegalArgumentException("Không thể tắt loại xe vẫn còn xe hoạt động!");
        }

        coachType.setActive(updateRequest.isActive());
    }

    @Transactional
    @Override
    public void updateCoachTypePrice(Integer id, CoachTypeUpdatePriceRequest updateRequest) {
        CoachType coachType = coachTypeRepo.findById(id).orElseThrow(
                () -> {
                    throw new ResourceNotFoundException("Không tìm thấy loại xe có ID là: " + id);
                });

        Optional<CoachTypePrice> currentPrice = coachType.getCoachTypePrices().stream()
                .filter(price -> !price.getStartEffectiveDate().isAfter(LocalDateTime.now())
                        && price.getEndEffectiveDate().isAfter(LocalDateTime.now()))
                .findFirst();

        if (currentPrice.isPresent() && currentPrice.get().getSeatPrice().compareTo(updateRequest.seatPrice()) != 0) {
            currentPrice.get().setEndEffectiveDate(LocalDateTime.now());

            CoachTypePrice newPrice = CoachTypePrice.builder()
                    .coachType(coachType)
                    .seatPrice(updateRequest.seatPrice())
                    .startEffectiveDate(LocalDateTime.now())
                    .endEffectiveDate(infiniteDateTime)
                    .build();

            coachType.getCoachTypePrices().add(newPrice);
        }
    }

    @Transactional
    @Override
    public void updateCoachTypeSeatmap(Integer id, CoachTypeUpdateSeatmapRequest updateRequest) {
        CoachType coachType = coachTypeRepo.findById(id).orElseThrow(
            () -> {throw new ResourceNotFoundException("Không tìm thấy loại xe có ID là: " + id);}
        );

        
        if(coachType.getSeatLayout().equalsIgnoreCase(updateRequest.seatLayout())) {
            return;
        }
        
        if(coachRepo.existsByCoachType_CoachTypeIdAndStatusNot(id, CoachStatus.RETIRED)) {
            throw new IllegalArgumentException("Không thể thay đổi sơ đồ ghế của loại xe vẫn còn xe hoạt động!");
        }
        
        ProcessedSeatLayout newSeatLayout = validateSeatLayout(updateRequest.seatLayout());
        coachType.setTotalSeat(newSeatLayout.totalSeat());
        coachType.setSeatLayout(newSeatLayout.seatLayout());
    }

    @Override
    public List<CoachTypeDropdownDTO> findActiveCoachTypesForDropdown() {
        return coachTypeRepo.findActiveCoachTypesForDropdown();
    }

}
