package com.djpa.core.dynamicfilter.dto;

import java.util.List;

public record TableResponse<T>(

        List<T> content,
        long contentSize,
        long totalElements,
        long totalPages,
        long page,
        long size,
        boolean first,
        boolean last

) {
}
