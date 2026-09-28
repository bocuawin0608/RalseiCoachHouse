package com.ralsei.driver.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ralsei.driver.dto.request.coachtype.CoachTypeCreateRequest;
import com.ralsei.driver.dto.request.coachtype.CoachTypeFilterRequest;
import com.ralsei.driver.dto.request.coachtype.CoachTypePriceCreateRequest;
import com.ralsei.driver.dto.request.coachtype.CoachTypeUpdateInfoRequest;
import com.ralsei.driver.dto.request.coachtype.CoachTypeUpdatePriceRequest;
import com.ralsei.driver.dto.request.coachtype.CoachTypeUpdateSeatmapRequest;
import com.ralsei.driver.dto.response.coachtype.CoachTypeDeactivationCheckResponse;
import com.ralsei.driver.dto.response.coachtype.CoachTypeDetailResponse;
import com.ralsei.driver.dto.response.coachtype.CoachTypeDropdownDTO;
import com.ralsei.driver.dto.response.coachtype.CoachTypePriceResponse;
import com.ralsei.driver.dto.response.coachtype.CoachTypeResponse;

/**
 * Provides the business service contract for coach type.
 */
public interface CoachTypeService {
    Page<CoachTypeResponse> filterCoachTypes(CoachTypeFilterRequest filterRequest, Pageable pageable);
    Integer createCoachType(CoachTypeCreateRequest request);
    CoachTypeDetailResponse getCoachTypeDetail(Integer id);
    void updateCoachTypeInfo(Integer id, CoachTypeUpdateInfoRequest updateRequest);
    void updateCoachTypePrice(Integer id, CoachTypeUpdatePriceRequest updateRequest);
    void updateCoachTypeSeatmap(Integer id, CoachTypeUpdateSeatmapRequest updateRequest);
    List<CoachTypePriceResponse> getCoachTypePrices(Integer id);
    void addCoachTypePrice(Integer id, CoachTypePriceCreateRequest request);
    CoachTypeDeactivationCheckResponse getDeactivationCheck(Integer id);

    List<CoachTypeDropdownDTO> findActiveCoachTypesForDropdown();
}
