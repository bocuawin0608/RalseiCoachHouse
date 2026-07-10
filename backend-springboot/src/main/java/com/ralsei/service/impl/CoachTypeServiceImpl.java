package com.ralsei.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
import com.ralsei.dto.request.coachtype.CoachTypePriceCreateRequest;
import com.ralsei.dto.request.coachtype.CoachTypeUpdateInfoRequest;
import com.ralsei.dto.request.coachtype.CoachTypeUpdatePriceRequest;
import com.ralsei.dto.request.coachtype.CoachTypeUpdateSeatmapRequest;
import com.ralsei.dto.response.coachtype.CoachTypeDeactivationCheckResponse;
import com.ralsei.dto.response.coachtype.CoachTypeDetailResponse;
import com.ralsei.dto.response.coachtype.CoachTypeDropdownDTO;
import com.ralsei.dto.response.coachtype.CoachTypePriceResponse;
import com.ralsei.dto.response.coachtype.CoachTypeResponse;
import com.ralsei.exception.BusinessRuleException;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Coach;
import com.ralsei.model.CoachType;
import com.ralsei.model.CoachTypePrice;
import com.ralsei.model.enums.CoachStatus;
import com.ralsei.model.enums.CoachTypePriceStatus;
import com.ralsei.repository.CoachRepository;
import com.ralsei.repository.CoachTypePriceRepository;
import com.ralsei.repository.CoachTypeRepository;
import com.ralsei.repository.TripRepository;
import com.ralsei.service.CoachTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoachTypeServiceImpl implements CoachTypeService {

    private final CoachTypeRepository coachTypeRepo;
    private final CoachTypePriceRepository coachTypePriceRepo;
    private final CoachRepository coachRepo;
    private final TripRepository tripRepo;
    private final ObjectMapper objectMapper;

    private static final LocalDateTime INFINITE_END = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

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

        CoachTypeFilterRequest sanitized = sanitizeFilter(filterRequest);
        Page<CoachTypeProjection> projections = coachTypeRepo.searchCoachTypes(sanitized, LocalDateTime.now(),
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
        String name = request.coachTypeName().trim();
        if (coachTypeRepo.existsByCoachTypeNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tên loại xe đã tồn tại trong hệ thống!");
        }

        ProcessedSeatLayout verifiedSeatLayout = validateSeatLayout(request.seatLayout());

        CoachType newCoachType = CoachType.builder()
                .coachTypeName(name)
                .totalSeat(verifiedSeatLayout.totalSeat())
                .seatLayout(verifiedSeatLayout.seatLayout())
                .isActive(true)
                .build();

        CoachTypePrice price = CoachTypePrice.builder()
                .coachType(newCoachType)
                .seatPrice(request.seatPrice())
                .startEffectiveDate(LocalDateTime.now())
                .endEffectiveDate(INFINITE_END)
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

            for (JsonNode floor : floorsNode) {
                for (JsonNode rowNode : floor) {
                    for (JsonNode cellNode : rowNode) {
                        if (cellNode.asText().equals("SEAT")) {
                            calculatedTotalSeat++;
                        }
                    }
                }
            }
            //TODO: chưa có cơ chế check 1 cột/hàng bất kỳ trống hoàn toàn ghế (kiểu đang dùng cols/rows cho các tầng -> sẽ có nơi bị trống full)

            if (calculatedTotalSeat < 1 || calculatedTotalSeat > 44) {
                throw new IllegalArgumentException("Sơ đồ ghế không thể <1 hoặc >44 ghế.");
            }

            return new ProcessedSeatLayout(calculatedTotalSeat, rootNode.toString());

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Định dạng JSON của sơ đồ ghế không hợp lệ.", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public CoachTypeDetailResponse getCoachTypeDetail(Integer id) {
        CoachType coachType = findCoachTypeOrThrow(id);
        LocalDateTime now = LocalDateTime.now();
        List<CoachTypePrice> prices = coachTypePriceRepo.findByCoachType_CoachTypeIdOrderByStartEffectiveDateDesc(id);

        Optional<CoachTypePrice> activePrice = prices.stream()
                .filter(p -> isEffectiveAt(p, now))
                .findFirst();

        BigDecimal currentPrice = activePrice.map(CoachTypePrice::getSeatPrice).orElse(BigDecimal.ZERO);
        LocalDateTime currentPriceEffectiveFrom = activePrice.map(CoachTypePrice::getStartEffectiveDate).orElse(null);
        long activeCoachCount = coachRepo.countByCoachType_CoachTypeIdAndStatusNot(id, CoachStatus.RETIRED);
        boolean canEditSeatLayout = activeCoachCount == 0;

        return new CoachTypeDetailResponse(
                coachType.getCoachTypeId(),
                coachType.getCoachTypeName(),
                coachType.getTotalSeat(),
                currentPrice,
                coachType.isActive(),
                coachType.getSeatLayout(),
                (int) activeCoachCount,
                currentPriceEffectiveFrom,
                canEditSeatLayout);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CoachTypePriceResponse> getCoachTypePrices(Integer id) {
        findCoachTypeOrThrow(id);
        LocalDateTime now = LocalDateTime.now();
        return coachTypePriceRepo.findByCoachType_CoachTypeIdOrderByStartEffectiveDateDesc(id).stream()
                .map(p -> toPriceResponse(p, now))
                .toList();
    }

    @Transactional
    @Override
    public void addCoachTypePrice(Integer id, CoachTypePriceCreateRequest request) {
        CoachType coachType = findCoachTypeOrThrow(id);
        LocalDateTime start = request.startEffectiveDate();
        LocalDateTime end = INFINITE_END;

        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now.minusMinutes(5))) {
            throw new IllegalArgumentException("Ngày bắt đầu không được nằm trong quá khứ!");
        }

        List<CoachTypePrice> existingPrices = coachTypePriceRepo
                .findByCoachType_CoachTypeIdOrderByStartEffectiveDateDesc(id);

        for (CoachTypePrice existing : existingPrices) {
            LocalDateTime existingStart = existing.getStartEffectiveDate();
            LocalDateTime existingEnd = existing.getEndEffectiveDate();

            if (existingStart.isBefore(start) && existingEnd.isAfter(start)) {
                existing.setEndEffectiveDate(start);
                coachTypePriceRepo.save(existing);
                continue;
            }

            if (!existingStart.isBefore(start)) {
                throw new IllegalArgumentException(
                        "Đã có mốc giá bắt đầu từ thời điểm này trở đi. Hãy chọn ngày bắt đầu sau mốc giá sắp tới.");
            }
        }

        CoachTypePrice newPrice = CoachTypePrice.builder()
                .coachType(coachType)
                .seatPrice(request.seatPrice())
                .startEffectiveDate(start)
                .endEffectiveDate(end)
                .build();
        coachTypePriceRepo.save(newPrice);
    }

    @Transactional(readOnly = true)
    @Override
    public CoachTypeDeactivationCheckResponse getDeactivationCheck(Integer id) {
        findCoachTypeOrThrow(id);
        List<Coach> activeCoaches = coachRepo.findByCoachType_CoachTypeIdAndStatusNot(id, CoachStatus.RETIRED);

        List<CoachTypeDeactivationCheckResponse.ActiveCoachSummary> summaries = activeCoaches.stream()
                .map(c -> new CoachTypeDeactivationCheckResponse.ActiveCoachSummary(
                        c.getCoachId(),
                        c.getLicensePlate(),
                        c.getStatus(),
                        tripRepo.countUpcomingTripsByCoachId(c.getCoachId())))
                .toList();

        return new CoachTypeDeactivationCheckResponse(summaries.isEmpty(), summaries);
    }

    @Transactional
    @Override
    public void updateCoachTypeInfo(Integer id, CoachTypeUpdateInfoRequest updateRequest) {
        CoachType coachType = findCoachTypeOrThrow(id);
        String name = updateRequest.coachTypeName().trim();

        if (!coachType.getCoachTypeName().equalsIgnoreCase(name)) {
            if (coachTypeRepo.existsByCoachTypeNameIgnoreCase(name)) {
                throw new IllegalArgumentException("Tên loại xe đã tồn tại trong hệ thống!");
            }
            coachType.setCoachTypeName(name);
        }

        if (!updateRequest.isActive() && coachType.isActive()) {
            CoachTypeDeactivationCheckResponse check = getDeactivationCheck(id);
            if (!check.canDeactivate()) {
                throw new BusinessRuleException(
                        "COACH_TYPE_HAS_ACTIVE_COACHES",
                        "Không thể tắt loại xe vì vẫn còn xe đang hoạt động hoặc bảo trì!",
                        check);
            }
        }

        coachType.setActive(updateRequest.isActive());
    }

    @Transactional
    @Override
    public void updateCoachTypePrice(Integer id, CoachTypeUpdatePriceRequest updateRequest) {
        addCoachTypePrice(id, new CoachTypePriceCreateRequest(
                updateRequest.seatPrice(),
                LocalDateTime.now(),
                null));
    }

    @Transactional
    @Override
    public void updateCoachTypeSeatmap(Integer id, CoachTypeUpdateSeatmapRequest updateRequest) {
        CoachType coachType = findCoachTypeOrThrow(id);

        if (coachType.getSeatLayout().equalsIgnoreCase(updateRequest.seatLayout())) {
            return;
        }

        if (coachRepo.existsByCoachType_CoachTypeIdAndStatusNot(id, CoachStatus.RETIRED)) {
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

    private CoachTypeFilterRequest sanitizeFilter(CoachTypeFilterRequest filter) {
        String name = filter.coachTypeName() == null ? null : filter.coachTypeName().trim();
        if (name != null && name.isEmpty()) {
            name = null;
        }
        if (Objects.equals(name, filter.coachTypeName())) {
            return filter;
        }
        return new CoachTypeFilterRequest(name, filter.minPrice(), filter.maxPrice(), filter.minSeats(), filter.maxSeats(), filter.isActive());
    }

    private CoachType findCoachTypeOrThrow(Integer id) {
        return coachTypeRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy loại xe có ID là: " + id));
    }

    private boolean isEffectiveAt(CoachTypePrice price, LocalDateTime at) {
        return !price.getStartEffectiveDate().isAfter(at) && price.getEndEffectiveDate().isAfter(at);
    }

    private CoachTypePriceStatus computePriceStatus(CoachTypePrice price, LocalDateTime now) {
        if (price.getStartEffectiveDate().isAfter(now)) {
            return CoachTypePriceStatus.UPCOMING;
        }
        if (!price.getEndEffectiveDate().isAfter(now)) {
            return CoachTypePriceStatus.EXPIRED;
        }
        return CoachTypePriceStatus.ACTIVE;
    }

    private CoachTypePriceResponse toPriceResponse(CoachTypePrice price, LocalDateTime now) {
        return new CoachTypePriceResponse(
                price.getCoachTypePriceId(),
                price.getSeatPrice(),
                price.getStartEffectiveDate(),
                price.getEndEffectiveDate(),
                computePriceStatus(price, now));
    }
}
