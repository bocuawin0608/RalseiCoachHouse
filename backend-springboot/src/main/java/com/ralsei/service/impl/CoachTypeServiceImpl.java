package com.ralsei.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
import com.ralsei.dto.response.coachtype.CoachTypeDetailResponse;
import com.ralsei.dto.response.coachtype.CoachTypeResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.CoachType;
import com.ralsei.model.CoachTypePrice;
import com.ralsei.repository.CoachTypeRepository;
import com.ralsei.service.CoachTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoachTypeServiceImpl implements CoachTypeService {
    
    private final CoachTypeRepository coachTypeRepo;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<CoachTypeResponse> filterCoachTypes(CoachTypeFilterRequest filterRequest, Pageable pageable) {
        
        if(filterRequest.minPrice() != null && filterRequest.maxPrice() != null && filterRequest.minPrice().compareTo(filterRequest.maxPrice()) > 0) {
            throw new IllegalArgumentException("Giá tối thiểu không thể lớn hơn Giá tối đa!");
        }
        
        if(filterRequest.minSeats() != null && filterRequest.maxSeats() != null && filterRequest.minSeats() > filterRequest.maxSeats()) {
            throw new IllegalArgumentException("Số ghế tối thiểu không thể lớn hơn Số ghế tối đa!");
        }

        Page<CoachTypeProjection> projections = coachTypeRepo.searchCoachTypes(filterRequest, LocalDateTime.now(), pageable);
        
        return projections.map(type -> new CoachTypeResponse(
            type.getCoachTypeId(),
            type.getCoachTypeName(),
            type.getTotalSeat(),
            type.getCurrentPrice(),
            type.getIsActive(),
            type.getTotalCoach()
        ));
    }

    @Transactional
    @Override
    public Integer createCoachType(CoachTypeCreateRequest request) {
        if(coachTypeRepo.existsByCoachTypeNameIgnoreCase(request.coachTypeName())) {
            throw new IllegalArgumentException("Tên loại xe đã tồn tại trong hệ thống!");
        }

        int calculatedTotalSeat = 0;
        String minifiedSeatLayout = "";
        try {
            JsonNode rootNode = objectMapper.readTree(request.seatLayout());
            int declaredRows = rootNode.path("rows").asInt(0);
            int declaredCols = rootNode.path("cols").asInt(0);
            JsonNode matrixNode = rootNode.get("matrix");

            if (declaredRows < 1 || declaredRows > 11) {
                throw new IllegalArgumentException("Số hàng trong sơ đồ ghế phải nằm trong khoảng từ 1 đến 11.");
            }
            if (declaredCols < 1 || declaredCols > 5) {
                throw new IllegalArgumentException("Số cột trong sơ đồ ghế phải nằm trong khoảng từ 1 đến 5.");
            }

            if(matrixNode==null || !matrixNode.isArray()) {
                throw new IllegalArgumentException("Sơ đồ ghế bị thiếu hoặc sai định dạng.");
            }

            if(matrixNode.size() != declaredRows) {
                throw new IllegalArgumentException("Số hàng trong sơ đồ ghế không khớp với giá trị khai báo.");
            }

            for (JsonNode rowNode : matrixNode) {
                if(rowNode.size() != declaredCols) {
                    throw new IllegalArgumentException("Số cột trong sơ đồ ghế không khớp với giá trị khai báo.");
                }
            }

            for (JsonNode rowNode : matrixNode) {
                for (JsonNode cellNode : rowNode) {
                    if (cellNode.asText().equals("seat")) {
                        calculatedTotalSeat++;
                    }
                }
            }

            if(calculatedTotalSeat == 0) {
                throw new IllegalArgumentException("Sơ đồ ghế không thể có 0 ghế.");
            }

            minifiedSeatLayout = rootNode.toString();

        } catch(JsonProcessingException e) {
            throw new IllegalArgumentException("Định dạng JSON của sơ đồ ghế không hợp lệ.", e);
        }

        CoachType newCoachType = CoachType.builder()
            .coachTypeName(request.coachTypeName())
            .totalSeat(calculatedTotalSeat)
            .seatLayout(minifiedSeatLayout)
            .isActive(true)
            .build();

        CoachTypePrice price = CoachTypePrice.builder()
            .coachType(newCoachType)
            .seatPrice(request.seatPrice())
            .startEffectiveDate(LocalDateTime.now())
            .endEffectiveDate(LocalDateTime.of(9999, 12, 31, 23, 59, 59))
            .build();
        newCoachType.setCoachTypePrices(List.of(price));
        
        CoachType savedCoachType = coachTypeRepo.save(newCoachType);
        return savedCoachType.getCoachTypeId();
    }

    @Transactional
    @Override
    public CoachTypeDetailResponse getCoachTypeDetail(Integer id) {
        CoachType coachType = coachTypeRepo.findById(id).orElseThrow(
            () -> {throw new ResourceNotFoundException("Không tìm thấy Loại xe có ID là: " + id);}
        );

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
            coachType.getSeatLayout()
        );
    }

}
