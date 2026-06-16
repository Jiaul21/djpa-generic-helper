package com.djpa.core.dynamicfilter.dto;


import java.util.List;

public record FilterRequest(
        String field,
        Operator operator,
        List<String> values,
        String sort
) {
}
