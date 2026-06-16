package com.djpa.core.dynamicfilter.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;

public record QueryFilterInfo(
        BooleanBuilder condition,
        OrderSpecifier[] orders
) {
}
