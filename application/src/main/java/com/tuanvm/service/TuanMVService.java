package com.tuanvm.service;

import com.tuanvm.dto.projection.TripDetailProjection;
import com.tuanvm.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service

public class TuanMVService {
     @Autowired
    private TripRepository tripRepository;

    public List<TripDetailProjection> getComplexTripDetails(String dateStr) {
        // Chặn đầu chặn đuôi ngày chạy của con xe
        String start = dateStr + " 00:00:00";
        String end = dateStr + " 23:59:59.999"; 
        
        return tripRepository.layThongTinChuyenXeComplex(start, end);
    }

}
   