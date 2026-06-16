package com.djpa.core.config;

import com.djpa.core.dynamicfilter.querydsl.QueryExecutor;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class DjpaAutoConfiguration {

    @Bean
    @ConditionalOnBean(EntityManager.class)
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    @ConditionalOnBean({EntityManager.class, JPAQueryFactory.class})
    public QueryExecutor queryExecutor(JPAQueryFactory jpaQueryFactory, EntityManager entityManager) {
        return new QueryExecutor(jpaQueryFactory, entityManager);
    }
}