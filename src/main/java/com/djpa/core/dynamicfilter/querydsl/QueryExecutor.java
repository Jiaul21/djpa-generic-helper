package com.djpa.core.dynamicfilter.querydsl;

import com.djpa.core.dynamicfilter.dto.TableRequest;
import com.djpa.core.dynamicfilter.dto.TableResponse;
import com.djpa.core.dynamicfilter.querydsl.dto.QueryFilterInfo;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.*;

public class QueryExecutor {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    public QueryExecutor(JPAQueryFactory queryFactory, EntityManager entityManager) {
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
    }

    public <T, U> TableResponse<T> fetchTableResult(JPAQuery<?> baseQuery,
                                                    Class<T> projectionClass,
                                                    Map<String, SimpleExpression<?>> simpleExpressionMap,
                                                    TableRequest request,
                                                    Expression<U> countExpression) {

        QueryFilterInfo process = QueryFilter.process(request.filterRequests(), simpleExpressionMap);

        List<T> content = fetchResult(baseQuery, projectionClass, simpleExpressionMap, request, process);
        U count = fetchCount(baseQuery, countExpression);

        return tableResponse(content, (long) count, request);
    }


    public <T> List<T> fetchResult(JPAQuery<?> baseQuery,
                                   Class<T> projectionClass,
                                   Map<String, SimpleExpression<?>> simpleExpressionMap,
                                   TableRequest request,
                                   QueryFilterInfo queryFilter) {

        if (projectionClass.isRecord()) {
            return baseQuery.clone(entityManager).select(Projections.constructor(projectionClass, buildExpression(simpleExpressionMap)))
                    .offset(request.page() * request.size()).limit(request.size())
                    .orderBy(queryFilter.orders())
                    .fetch();
        }
        return baseQuery.clone(entityManager).select(Projections.bean(projectionClass, buildExpression(simpleExpressionMap)))
                .offset(request.page() * request.size()).limit(request.size())
                .orderBy(queryFilter.orders())
                .fetch();
    }

    public <U> U fetchCount(JPAQuery<?> baseQuery, Expression<U> expr) {
        return baseQuery.clone(entityManager)
                .select(expr)
                .fetchOne();
    }


    public <ID, E> Map<ID, List<E>> fetchCollection(EntityPath<?> from,
                                                    SimpleExpression<ID> idField,
                                                    CollectionExpression<?, E> collection,
                                                    Class<E> collectionElementType,
                                                    Collection<ID> ids) {

        Path<E> elementPath = Expressions.path(collectionElementType, "element");
        List<Tuple> rows = queryFactory
                .select(idField, elementPath)
                .from(from)
                .leftJoin(collection, elementPath)
                .where(idField.in(ids))
                .fetch();

        Map<ID, List<E>> result = new LinkedHashMap<>();

        for (Tuple row : rows) {
            ID id = row.get(idField);
            E element = row.get(elementPath);

            result.computeIfAbsent(id, k -> new ArrayList<>()).add(element);
        }
        return result;
    }


    public <T> TableResponse<T> tableResponse(List<T> content, Long count, TableRequest request) {

        long totalElements = count == null ? 0 : count;
        int totalPages = (int) Math.ceil((double) totalElements / request.size());
        return new TableResponse<T>(
                content,
                content.size(),
                totalElements,
                totalPages,
                request.page(),
                request.size(),
                request.page() == 0,
                request.page() >= totalPages - 1
        );
    }

    private SimpleExpression<?>[] buildExpression(Map<String, SimpleExpression<?>> fieldExp) {
        List<? extends SimpleExpression<?>> exp =
                fieldExp.entrySet()
                        .stream()
                        .map(ex -> ex.getValue().as(ex.getKey()))
                        .toList();

        return exp.toArray(new SimpleExpression<?>[0]);
    }
}
