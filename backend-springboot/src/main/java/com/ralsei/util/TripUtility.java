package com.ralsei.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass    
public class TripUtility {

    // Tạo sẵn một bộ format chuẩn có khoảng trắng để dùng chung cho cả hệ thống, tránh tạo đi tạo lại object
    private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd[ HH:mm:ss][ HH:mm]");

    /**
     * Format LocalDateTime thành chuỗi "yyyy-MM-dd HH:mm:ss.SSS" sạch sẽ, không có chữ T
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(CUSTOM_FORMATTER);
    }

    /**
     * Parse chuỗi thời gian (chấp nhận cả khoảng trắng lẫn dấu +) về LocalDateTime an toàn
     */
    public static LocalDateTime parseDate(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        // Dọn dẹp dấu + hoặc chữ T nếu có, đưa về khoảng trắng chuẩn trước khi parse
        String cleanedStr = dateTimeStr.replace("+", " ").replace("T", " ").trim();
        
        return LocalDateTime.parse(cleanedStr, INPUT_FORMATTER);
    }
}