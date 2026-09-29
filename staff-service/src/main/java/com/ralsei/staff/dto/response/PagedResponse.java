package com.ralsei.staff.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * Represents the response payload for paged operations.
 */
@Data
@AllArgsConstructor

public class PagedResponse<T> {
    private List<T> content;       
    private int pageNumber;        
    private int pageSize;          
    private long totalElements;    
    private int totalPages;        
    private boolean isLast;        
}