/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.talend.webservice.mapper;

import org.talend.webservice.exception.LocalizedException;

/**
 *
 * @author rlamarche
 */
public interface TypeMapper {

    public Class<?> getClazz();

    public Object convertToType(Object value) throws LocalizedException;

    public Object typeToValue(Object bean) throws LocalizedException;
}
