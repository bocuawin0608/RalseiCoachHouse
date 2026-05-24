package com.tuanvm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tuanvm.dto.projection.TripDetailProjection;
import com.tuanvm.service.TuanMVService;


@Controller
public class WebController {
    @Autowired
    private TuanMVService tuanMVService;

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }
    @GetMapping("/home")
    public String getHomePage() {
        return "home";
    }
    @GetMapping("/search-trips")
public String searchTrips(
        @RequestParam(value = "tripType", required = false) String tripType,
        @RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "to", required = false) String to,
        @RequestParam("date") String dateStr, 
        @RequestParam(value = "route", required = false) String route,
        Model model) {

    System.out.println("====== TEST ======");
    System.out.println("Tuyến: " + from + " -> " + to);
    System.out.println("Ngày đi: " + dateStr);

    if (dateStr == null || dateStr.trim().isEmpty()) {
        return "redirect:/home";
    }

List<TripDetailProjection> ketQuaTimKiem = tuanMVService.getComplexTripDetails(dateStr, from + " - " + to  );    

// CHÈN THÊM 2 DÒNG NÀY ĐỂ DEBUG
System.out.println("--> Số lượng chuyến xe tìm thấy: " + (ketQuaTimKiem != null ? ketQuaTimKiem.size() : "NULL"));
if (ketQuaTimKiem != null && !ketQuaTimKiem.isEmpty()) {
    System.out.println("--> Tuyến đường đầu tiên: " + ketQuaTimKiem.get(0).getRouteName());
}
    

    model.addAttribute("trips", ketQuaTimKiem);
    model.addAttribute("searchDate", dateStr); 
    model.addAttribute("from", from); 
    model.addAttribute("to", to); // Đẩy điểm đến xuống để hiển thị    
    return "home"; 
}
    


}
