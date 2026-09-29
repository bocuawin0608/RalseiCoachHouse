package com.ralsei.staff.util;

import java.time.LocalDate;

import lombok.experimental.UtilityClass;

/**
 * Provides utility helpers for get current date uti processing.
 */
@UtilityClass

public class GetCurrentDateUtility {
    public static LocalDate getRecentDate(){
        LocalDate date = LocalDate.now();
        return date;
    }
}
