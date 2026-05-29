package com.ralsei.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ralsei.dto.projection.SeatLayoutProjection;
import com.ralsei.dto.request.seatlayout.SeatLayoutCreateRequest;
import com.ralsei.dto.request.seatlayout.SeatLayoutFilterRequest;
import com.ralsei.dto.response.seatlayout.SeatDTO;
import com.ralsei.dto.response.seatlayout.SeatLayoutDetailResponse;
import com.ralsei.dto.response.seatlayout.SeatLayoutResponse;
import com.ralsei.exception.ResourceNotFoundException;
import com.ralsei.model.Seat;
import com.ralsei.model.SeatLayout;
import com.ralsei.model.SeatLayoutPrice;
import com.ralsei.repository.SeatLayoutRepository;
import com.ralsei.service.SeatLayoutService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatLayoutServiceImpl implements SeatLayoutService {
    
    private final SeatLayoutRepository seatLayoutRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<SeatLayoutResponse> filterSeatLayouts(SeatLayoutFilterRequest filterRequest, Pageable pageable) {
        
        if(filterRequest.minPrice() != null && filterRequest.maxPrice() != null && filterRequest.minPrice().compareTo(filterRequest.maxPrice()) > 0) {
            throw new IllegalArgumentException("Giá tối thiểu không thể lớn hơn Giá tối đa!");
        }
        
        if(filterRequest.minSeats() != null && filterRequest.maxSeats() != null && filterRequest.minSeats() > filterRequest.maxSeats()) {
            throw new IllegalArgumentException("Số ghế tối thiểu không thể lớn hơn Số ghế tối đa!");
        }

        Page<SeatLayoutProjection> projections = seatLayoutRepository.searchSeatLayouts(filterRequest, LocalDateTime.now(), pageable);
        
        return projections.map(layout -> new SeatLayoutResponse(
            layout.getSeatLayoutId(),
            layout.getSeatLayoutName(),
            layout.getTotalSeat(),
            layout.getCurrentPrice(),
            layout.getIsActive()
        ));
    }

    @Transactional
    @Override
    public SeatLayoutResponse createSeatLayout(SeatLayoutCreateRequest request) {

        //TODO: check các đk để return exception 
        // check unique seat layout name (dưới DB sửa thành UQ luôn ddl)

        SeatLayout layout = SeatLayout.builder()
                .seatLayoutName(request.seatLayoutName())
                .totalSeat(request.totalRows() * request.totalCols())
                .isActive(true)
                .build();

        List<Seat> seats = new ArrayList<>();
        for (int r = 1; r <= request.totalRows(); r++) {
            for (int c = 1; c <= request.totalCols(); c++) {
                Seat seat = Seat.builder()
                        .seatCode(generateSeatCode(r, c))
                        .rowIndex(r)
                        .colIndex(c)
                        .isActive(true)
                        .seatLayout(layout)
                        .build();
                seats.add(seat);
            }
        }
        layout.setSeats(seats);

        SeatLayoutPrice price = SeatLayoutPrice.builder()
                .seatPrice(request.seatPrice())
                .startEffectiveDate(LocalDateTime.now())
                .endEffectiveDate(LocalDateTime.of(9999, 12, 31, 23, 59, 59))
                .seatLayout(layout)
                .build();
        layout.setSeatLayoutPrices(List.of(price));

        SeatLayout savedLayout = seatLayoutRepository.save(layout);

        return new SeatLayoutResponse(
            savedLayout.getSeatLayoutId(),
            savedLayout.getSeatLayoutName(),
            savedLayout.getTotalSeat(),
            request.seatPrice(),
            savedLayout.isActive()
        );
    }

    private String generateSeatCode(int r, int c) {
        return r + "-" + c;
    }

    @Transactional(readOnly = true)
    @Override
    public SeatLayoutDetailResponse getSeatLayoutDetail(Integer seatLayoutId) {
        SeatLayout layout = seatLayoutRepository.findByIdWithSeatsAndPrice(seatLayoutId).orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy sơ đồ ghế có ID là: " + seatLayoutId)
        );

        BigDecimal currentPrice = layout.getSeatLayoutPrices().stream()
            .filter(price -> price.getStartEffectiveDate().isBefore(LocalDateTime.now())
                  && price.getEndEffectiveDate().isAfter(LocalDateTime.now()))
            .findFirst()
            .map(price -> price.getSeatPrice())
            .orElse(BigDecimal.ZERO);

        List<SeatDTO> seatDTOs = layout.getSeats().stream()
            .map(seat -> new SeatDTO(
                seat.getSeatId(),
                seat.getSeatCode(),
                seat.getRowIndex(),
                seat.getColIndex(),
                seat.isActive()))
            .toList();

        return new SeatLayoutDetailResponse(
            layout.getSeatLayoutId(),
            layout.getSeatLayoutName(),
            layout.getTotalSeat(),
            currentPrice,
            layout.isActive(),
            seatDTOs
        );
    }
}
