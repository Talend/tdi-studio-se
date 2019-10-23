package org.talend.repository.model.migration.utils;

import org.eclipse.emf.common.util.EList;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class EmfUtils {

    public static <T> Stream<T> findAll(EList list, Class<T> clazz, Predicate<T> filter) {
        return list.stream().filter(clazz::isInstance).map(clazz::cast).filter(filter);
    }
}
