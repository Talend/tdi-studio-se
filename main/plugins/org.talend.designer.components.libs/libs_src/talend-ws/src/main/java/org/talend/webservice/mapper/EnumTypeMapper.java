/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.webservice.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.beanutils.ConvertUtils;
import org.talend.webservice.exception.InvalidEnumValueException;
import org.talend.webservice.exception.LocalizedException;

/**
 * 
 * @author rlamarche
 */
public class EnumTypeMapper implements TypeMapper {

    private Class<?> clazz;

    private Method value;

    private Method fromValue;

    public EnumTypeMapper(Class<?> clazz) {
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException("You must provide an enum class.");
        }
        this.clazz = clazz;
        try {
            this.fromValue = clazz.getMethod("fromValue", String.class);
            this.value = clazz.getMethod("value");
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object convertToType(Object value) throws LocalizedException {
        if (value == null) {
            return null;
        }
        if (!clazz.isInstance(value)) {
            String str = ConvertUtils.convert(value);
            try {
                return fromValue.invoke(null, str);// bug 13000
                // return valueOf.invoke(null, str);
            } catch (IllegalAccessException ex) {
                throw new LocalizedException("org.talend.ws.exception.illegalAccessValueOf", new String[] { clazz.getName() }, ex);
            } catch (InvocationTargetException ex) {
                if (ex.getTargetException() instanceof IllegalArgumentException) {
                    throw new InvalidEnumValueException(str, clazz.getName());
                } else {
                    throw new LocalizedException("org.talend.ws.exception.Unknown", ex.getTargetException());
                }
            }
        } else {
            return value;
        }
    }

    public Object typeToValue(Object bean) throws LocalizedException {
        if (bean == null) {
            return null;
        } else {
            try {
                return value.invoke(bean); // bug 13000
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
}
