package com.ralsei.util;

import java.time.LocalDate;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GetCurrentDateUtility {
    public static LocalDate getRecentDate(){
        LocalDate date = LocalDate.now();
        return date;
    }
}
