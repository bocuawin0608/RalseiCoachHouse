// package com.ralsei.service.impl;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.stream.Collectors;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.ralsei.dto.projection.SeatLayoutProjection;
// import com.ralsei.dto.request.seatlayout.SeatLayoutCreateRequest;
// import com.ralsei.dto.request.seatlayout.SeatLayoutFilterRequest;
// import com.ralsei.dto.request.seatlayout.SeatLayoutUpdateInfoRequest;
// import com.ralsei.dto.request.seatlayout.SeatLayoutUpdatePriceRequest;
// import com.ralsei.dto.request.seatlayout.SeatLayoutUpdateSeatRequest;
// import com.ralsei.dto.request.seatlayout.SeatRequestDTO;
// import com.ralsei.dto.response.seatlayout.SeatLayoutDetailResponse;
// import com.ralsei.dto.response.seatlayout.SeatLayoutResponse;
// import com.ralsei.dto.response.seatlayout.SeatResponseDTO;
// import com.ralsei.exception.BusinessRuleException;
// import com.ralsei.exception.ResourceNotFoundException;
// import com.ralsei.model.Seat;
// import com.ralsei.model.SeatLayout;
// import com.ralsei.model.CoachTypePrice;
// import com.ralsei.repository.SeatLayoutPriceRepository;
// import com.ralsei.repository.SeatLayoutRepository;
// import com.ralsei.service.SeatLayoutService;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class SeatLayoutServiceImpl implements SeatLayoutService {
    
//     private final SeatLayoutRepository layoutRepository;
//     private final SeatLayoutPriceRepository priceRepository;

//     @Transactional(readOnly = true)
//     @Override
//     public Page<SeatLayoutResponse> filterSeatLayouts(SeatLayoutFilterRequest filterRequest, Pageable pageable) {
        
//         if(filterRequest.minPrice() != null && filterRequest.maxPrice() != null && filterRequest.minPrice().compareTo(filterRequest.maxPrice()) > 0) {
//             throw new IllegalArgumentException("Giá tối thiểu không thể lớn hơn Giá tối đa!");
//         }
        
//         if(filterRequest.minSeats() != null && filterRequest.maxSeats() != null && filterRequest.minSeats() > filterRequest.maxSeats()) {
//             throw new IllegalArgumentException("Số ghế tối thiểu không thể lớn hơn Số ghế tối đa!");
//         }

//         Page<SeatLayoutProjection> projections = layoutRepository.searchSeatLayouts(filterRequest, LocalDateTime.now(), pageable);
        
//         return projections.map(layout -> new SeatLayoutResponse(
//             layout.getSeatLayoutId(),
//             layout.getSeatLayoutName(),
//             layout.getTotalSeat(),
//             layout.getCurrentPrice(),
//             layout.getIsActive()
//         ));
//     }

//     @Transactional
//     @Override
//     public SeatLayoutResponse createSeatLayout(SeatLayoutCreateRequest request) {

//         //TODO: check trùng seatLayoutName dưới DB
//         //TODO: ALTER TABLE [seat_layout] ADD CONSTRAINT UQ_SeatLayout_Name UNIQUE ([seatLayoutName]);

//         SeatLayout layout = SeatLayout.builder()
//                 .seatLayoutName(request.seatLayoutName())
//                 .totalSeat(request.totalRows() * request.totalCols())
//                 .isActive(true)
//                 .build();

//         List<Seat> seats = new ArrayList<>();
//         for (int r = 1; r <= request.totalRows(); r++) {
//             for (int c = 1; c <= request.totalCols(); c++) {
//                 Seat seat = Seat.builder()
//                         .seatCode(generateSeatCode(r, c))
//                         .rowIndex(r)
//                         .colIndex(c)
//                         .isActive(true)
//                         .seatLayout(layout)
//                         .build();
//                 seats.add(seat);
//             }
//         }
//         layout.setSeats(seats);

//         CoachTypePrice price = CoachTypePrice.builder()
//                 .seatPrice(request.seatPrice())
//                 .startEffectiveDate(LocalDateTime.now())
//                 .endEffectiveDate(LocalDateTime.of(9999, 12, 31, 23, 59, 59))
//                 .seatLayout(layout)
//                 .build();
//         layout.setSeatLayoutPrices(List.of(price));

//         SeatLayout savedLayout = layoutRepository.save(layout);

//         return new SeatLayoutResponse(
//             savedLayout.getSeatLayoutId(),
//             savedLayout.getSeatLayoutName(),
//             savedLayout.getTotalSeat(),
//             request.seatPrice(),
//             savedLayout.isActive()
//         );
//     }

//     private String generateSeatCode(int r, int c) {
//         return r + "-" + c;
//     }

//     @Transactional(readOnly = true)
//     @Override
//     public SeatLayoutDetailResponse getSeatLayoutDetail(Integer seatLayoutId) {
//         SeatLayout layout = layoutRepository.findByIdWithSeatsAndPrice(seatLayoutId).orElseThrow(
//             () -> new ResourceNotFoundException("Không tìm thấy sơ đồ ghế có ID là: " + seatLayoutId)
//         );

//        return mapDetailResponse(layout);
//     }

//     private SeatLayoutDetailResponse mapDetailResponse(SeatLayout layout) {
//         BigDecimal currentPrice = layout.getSeatLayoutPrices().stream()
//             .filter(price -> price.getStartEffectiveDate().isBefore(LocalDateTime.now())
//                   && price.getEndEffectiveDate().isAfter(LocalDateTime.now()))
//             .findFirst()
//             .map(price -> price.getSeatPrice())
//             .orElse(BigDecimal.ZERO);

//         List<SeatResponseDTO> seatDTOs = layout.getSeats().stream()
//             .map(seat -> new SeatResponseDTO(
//                 seat.getSeatId(),
//                 seat.getSeatCode(),
//                 seat.getRowIndex(),
//                 seat.getColIndex(),
//                 seat.isActive()))
//             .toList();

//         return new SeatLayoutDetailResponse(
//             layout.getSeatLayoutId(),
//             layout.getSeatLayoutName(),
//             layout.getTotalSeat(),
//             currentPrice,
//             layout.isActive(),
//             seatDTOs
//         );
//     }

//     @Transactional
//     @Override
//     public SeatLayoutDetailResponse updateSeatLayoutInfo(Integer seatLayoutId, SeatLayoutUpdateInfoRequest request) {
        
//         //TODO: check trùng seatLayoutName dưới DB
//         //TODO: check trùng existing name or isActive status thì ko cần xuống DB update, Hibernate làm hộ r?

//         SeatLayout layout = layoutRepository.findByIdWithSeatsAndPrice(seatLayoutId).orElseThrow(
//             () -> new ResourceNotFoundException("Không tìm thấy sơ đồ ghế có ID là: " + seatLayoutId)
//         );

//         layout.setSeatLayoutName(request.seatLayoutName());
//         layout.setActive(request.isActive());

//         return mapDetailResponse(layout);
//     }

//     @Transactional
//     @Override
//     public SeatLayoutDetailResponse updateSeatLayoutPrice(Integer seatLayoutId, SeatLayoutUpdatePriceRequest request) {
        
//         //TODO: check trùng giá hiện tại thì ko cần xuống DB update, Hibernate làm hộ r?
//         //TODO: ALTER TABLE [seat_layout_price] ADD CONSTRAINT UQ_SeatPrice_Timeline UNIQUE ([seatLayoutId], [startEffectiveDate]);
//          //TODO: Bây có cải nhau thì bây cook sang mục comment dùm bố m=))))) --- IGNORE ---
//         SeatLayout layout = layoutRepository.findByIdWithSeatsAndPrice(seatLayoutId).orElseThrow(
//             () -> new ResourceNotFoundException("Không tìm thấy sơ đồ ghế có ID là: " + seatLayoutId)
//         );

//         LocalDateTime now = LocalDateTime.now();

//         layout.getSeatLayoutPrices().stream()
//             .filter(price -> price.getEndEffectiveDate().isAfter(now))
//             .findFirst()
//             .ifPresent(activePrice -> activePrice.setEndEffectiveDate(now));

//         CoachTypePrice newPrice = CoachTypePrice.builder()
//             .seatPrice(request.seatPrice())
//             .startEffectiveDate(now)
//             .endEffectiveDate(LocalDateTime.of(9999, 12, 31, 23, 59, 59))
//             .seatLayout(layout)
//             .build();

//         priceRepository.save(newPrice);
//         layout.getSeatLayoutPrices().add(newPrice);
        
//         return mapDetailResponse(layout);
//     }

//     @Transactional
//     @Override
//     public SeatLayoutDetailResponse updateSeatLayoutSeats(Integer seatLayoutId, SeatLayoutUpdateSeatRequest request) {

//         //TODO: ALTER TABLE [seat] ADD CONSTRAINT UQ_Seat_Matrix UNIQUE ([seatLayoutId], [rowIndex], [colIndex]);
//         //TODO: ALTER TABLE [seat] ADD CONSTRAINT UQ_Seat_Code UNIQUE ([seatLayoutId], [seatCode]);
        
//         SeatLayout layout = layoutRepository.findByIdWithSeatsAndPrice(seatLayoutId).orElseThrow(
//             () -> new ResourceNotFoundException("Không tìm thấy sơ đồ ghế có ID là: " + seatLayoutId)
//         );

//         Set<String> uniqueCode = new HashSet<>();
//         Set<String> uniqueCoordinate = new HashSet<>();
//         int maxRow = 0;
//         int maxCol = 0;

//         for (SeatRequestDTO seatDTO : request.seats()) {
//             if(!uniqueCode.add(seatDTO.seatCode().trim().toUpperCase()))
//                 throw new BusinessRuleException("Mã ghế bị trùng lặp: " + seatDTO.seatCode());
            
//             String coordinate = seatDTO.rowIndex() + "-" + seatDTO.colIndex();
//             if(!uniqueCoordinate.add(coordinate))
//                 throw new BusinessRuleException("Tọa độ hàng - cột bị trùng lặp: " + coordinate);

//             maxRow = Math.max(maxRow, seatDTO.rowIndex());
//             maxCol = Math.max(maxCol, seatDTO.colIndex());
//         }

//         if(uniqueCoordinate.size() != maxRow * maxCol)
//             throw new BusinessRuleException("Số lượng ghế không khớp so với số hàng và số cột đã nhập.");

//         Map<Integer, Seat> existingSeatsMap = layout.getSeats().stream()
//                 .collect(Collectors.toMap(seat -> seat.getSeatId(), seat -> seat));

//         for (SeatRequestDTO dto : request.seats()) {
//             if (dto.seatId() != null) {
//                 Seat existingSeat = existingSeatsMap.get(dto.seatId());
//                 if (existingSeat != null) {
//                     existingSeat.setSeatCode(dto.seatCode());
//                     existingSeat.setActive(dto.isActive());
//                 } else {
//                     throw new BusinessRuleException("Ghế có ID " + dto.seatId() + " không thuộc sơ đồ này.");
//                 }
//             } else {
//                 Seat newSeat = new Seat();
//                 newSeat.setSeatLayout(layout);
//                 newSeat.setSeatCode(dto.seatCode());
//                 newSeat.setRowIndex(dto.rowIndex()); 
//                 newSeat.setColIndex(dto.colIndex());
//                 newSeat.setActive(dto.isActive());
                
//                 layout.getSeats().add(newSeat);
//             }
//         }

//         long newTotalSeat = layout.getSeats().stream().filter(seat -> seat.isActive()==true).count();
//         layout.setTotalSeat((int) newTotalSeat);

//        return mapDetailResponse(layout);
//     }
// }
