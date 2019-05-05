/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.webservice.mapper;

import org.talend.webservice.mapper.converter.ConvertTool;

/**
 * 
 * @author rlamarche
 */
public class SimpleTypeMapper implements TypeMapper {

    private Class<?> clazz;

    public SimpleTypeMapper(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object convertToType(Object value) {
        if (value == null) {
            return null;
        } else {
            // return ConvertUtils.convert(ConvertUtils.convert(value), clazz);
            return ConvertTool.convert(value, clazz);// bug13001, for date type value
        }
    }

    public Object typeToValue(Object bean) {
        return bean;
    }
}
