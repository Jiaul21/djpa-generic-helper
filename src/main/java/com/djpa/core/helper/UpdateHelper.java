package com.djpa.core.helper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface UpdateHelper<E, ID> {

    E updateEntity(ID id, Consumer<E> updater);

    int updateFieldByID(ID id, String fieldName, Object value);

    int updateFieldsByIDIn(Collection<ID> ids, Map<String, Object> fields);

    int updateField(String fieldName, Object fieldValue, String cName, Object cValue);

    int updateFieldsByConditions(Map<String, Object> fields, Map<String, Object> conditions);

    int bulkUpdate(String fieldName, List<? extends IdFieldValue> fields);

    Map<Object, List<Long>> groupFields(List<? extends IdFieldValue> fields);

}
