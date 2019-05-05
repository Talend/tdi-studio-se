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
public interface PropertyMapper {

    public Class<?> getMappedClass();

    public String getMappedPropertyName();

    public void setValueTo(Object destination, Object value) throws LocalizedException;

    public Object getValueFrom(Object source) throws LocalizedException;

    public Object createProperty(Object value) throws LocalizedException;
    public Object createValue(Object property) throws LocalizedException;
}
