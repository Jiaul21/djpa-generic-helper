package com.djpa.core.dynamicfilter.querydsl.dto;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;

public record QueryFilterInfo(
        BooleanBuilder condition,
        OrderSpecifier[] orders
) {
}
