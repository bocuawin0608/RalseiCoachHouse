package com.ralsei.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
/**
 * Represents the response payload for paged operations.
 */
public class PagedResponse<T> {
    private List<T> content;       
    private int pageNumber;        
    private int pageSize;          
    private long totalElements;    
    private int totalPages;        
    private boolean isLast;        
}