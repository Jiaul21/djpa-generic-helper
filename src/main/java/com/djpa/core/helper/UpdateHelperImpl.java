package com.djpa.core.helper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UpdateHelperImpl<E, ID> implements UpdateHelper<E, ID> {

    protected final Class<E> entityClass;
    @PersistenceContext
    private EntityManager entityManager;

    public UpdateHelperImpl(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    @Transactional
    public E updateEntity(ID id, Consumer<E> updater) {
        Objects.requireNonNull(updater, "Updater object must not be null.");

        E entity = entityManager.find(entityClass, id);
        if (entity == null)
            throw new EntityNotFoundException(entityClass.getSimpleName() + " entity reference not found for id=" + id);
        updater.accept(entity);
        return entity;
    }

    @Override
    @Transactional
    public int updateFieldByID(ID id, String fieldName, Object value) {
        String ql = "UPDATE " + entityClass.getSimpleName() + " e SET e." + fieldName + " = :value WHERE e.id = :id";
        int updated = entityManager.createQuery(ql)
                .setParameter("value", value)
                .setParameter("id", id)
                .executeUpdate();

        entityManager.clear();
        return updated;
    }

    @Override
    @Transactional
    public int updateFieldsByIDIn(Collection<ID> ids, Map<String, Object> fields) {
        if (ids == null || ids.isEmpty() || fields == null || fields.isEmpty())
            throw new IllegalArgumentException("Update query failed because of ids or fields is null or empty. ids=" + ids + ", fields=" + fields);

        StringBuilder jpql = new StringBuilder("UPDATE " + entityClass.getSimpleName() + " e SET ");

        int i = 0;
        for (String field : fields.keySet()) {
            if (i++ > 0) jpql.append(", ");
            jpql.append("e.").append(field).append(" = :").append(field);
        }
        jpql.append(" WHERE e.id IN :ids");

        Query query = entityManager.createQuery(jpql.toString());
        query.setParameter("ids", ids);
        fields.forEach(query::setParameter);

        int updated = query.executeUpdate();
        entityManager.clear();
        return updated;
    }

    @Override
    @Transactional
    public int updateField(String fieldName, Object fieldValue, String cName, Object cValue) {
        String ql = "UPDATE " + entityClass.getSimpleName() + " e SET e." + fieldName + " = :value WHERE e." + cName + " = :c";
        int updated = entityManager.createQuery(ql)
                .setParameter("value", fieldValue)
                .setParameter("c", cValue)
                .executeUpdate();

        entityManager.clear();
        return updated;
    }

    @Override
    @Transactional
    public int updateFieldsByConditions(Map<String, Object> fields, Map<String, Object> conditions) {
        if (fields == null || fields.isEmpty()) throw new IllegalArgumentException("Fields cannot be null or empty");
        if (conditions == null || conditions.isEmpty())
            throw new IllegalArgumentException("Conditions cannot be null or empty");

        StringBuilder jpql = new StringBuilder();
        jpql.append("UPDATE ").append(entityClass.getSimpleName()).append(" e SET ");

        // SET clause
        int i = 0;
        for (String field : fields.keySet()) {
            if (i++ > 0) jpql.append(", ");
            jpql.append("e.").append(field).append(" = :set_").append(field);
        }

        // WHERE clause
        jpql.append(" WHERE ");

        int j = 0;
        for (String cond : conditions.keySet()) {
            if (j++ > 0) jpql.append(" AND ");
            jpql.append("e.").append(cond).append(" = :cond_").append(cond);
        }

        Query query = entityManager.createQuery(jpql.toString());

        // set values
        fields.forEach((k, v) -> query.setParameter("set_" + k, v));
        conditions.forEach((k, v) -> query.setParameter("cond_" + k, v));

        int updated = query.executeUpdate();
        entityManager.clear();
        return updated;
    }


    @Override
    @Transactional
    public int bulkUpdate(String fieldName, List<? extends IdFieldValue> fields) {
        if (fields == null || fields.isEmpty()) return 0;
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(entityClass.getSimpleName()).append(" SET ").append(fieldName).append(" = CASE id ");

        for (IdFieldValue f : fields) sql.append(" WHEN ").append(f.getId()).append(" THEN :v").append(f.getId());

        sql.append(" END WHERE id IN (");
        String ids = fields.stream().map(f -> f.getId().toString()).collect(Collectors.joining(","));
        sql.append(ids).append(")");

        Query query = entityManager.createNativeQuery(sql.toString());
        for (IdFieldValue f : fields) query.setParameter("v" + f.getId(), f.getValue());

        return query.executeUpdate();
    }


    @Override
    public Map<Object, List<Long>> groupFields(List<? extends IdFieldValue> fields) {
        return fields.stream()
                .filter(field -> field.getId() != null)
                .collect(Collectors.groupingBy(
                        IdFieldValue::getValue,
                        Collectors.mapping(IdFieldValue::getId, Collectors.toList())
                ));
    }

}
