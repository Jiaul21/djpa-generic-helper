package com.djpa.core.dynamicfilter.dto;

import java.util.List;

public record TableRequest(
        long page,
        long size,
        List<FilterRequest> filterRequests
) {
}
