/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.webservice.mapper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.beanutils.PropertyUtils;
import org.talend.webservice.exception.IllegalPropertyAccessException;
import org.talend.webservice.exception.InvocationTargetPropertyAccessor;
import org.talend.webservice.exception.LocalizedException;

/**
 * 
 * @author rlamarche
 */
public class ListPropertyMapper implements PropertyMapper {

    private Map<QName, TypeMapper> schemaTypeMap;

    private TypeMapper xmlBeanMapper;

    private String propertyName;

    private PropertyDescriptor propertyDescriptor;

    private QName schemaTypeQName;

    public ListPropertyMapper(Class<?> clazz, QName typeQName, String propertyName, Map<QName, TypeMapper> schemaTypeMap) {
        this.schemaTypeMap = schemaTypeMap;
        this.schemaTypeQName = typeQName;

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(clazz);
        for (PropertyDescriptor descriptor : descriptors) {
            if (propertyName.equalsIgnoreCase(descriptor.getName())) {
                this.propertyName = descriptor.getName();
                propertyDescriptor = descriptor;
                break;
            }
        }
        if (propertyDescriptor == null) {
            throw new IllegalArgumentException("Unable to get propertyDescriptor for bean " + typeQName + " and property "
                    + propertyName);
        }

    }

    public Class<?> getMappedClass() {
        return xmlBeanMapper.getClazz();
    }

    public String getMappedPropertyName() {
        return propertyName;
    }

    public void setValueTo(Object destination, Object value) throws LocalizedException {
        xmlBeanMapper = schemaTypeMap.get(schemaTypeQName);
        if (value == null) {
            return;
        }
        List values;
        if (value instanceof List) {
            values = (List) value;
        } else {
            values = Arrays.asList(value);
        }

        List target;
        try {
            target = (List) propertyDescriptor.getReadMethod().invoke(destination);
        } catch (IllegalAccessException ex) {
            throw new IllegalPropertyAccessException(propertyDescriptor.getName(), destination.getClass().getName(), ex);
        } catch (InvocationTargetException ex) {
            throw new InvocationTargetPropertyAccessor(propertyDescriptor.getName(), destination.getClass().getName(), ex);
        }

        for (Object val : values) {
            target.add(xmlBeanMapper.convertToType(val));
        }
    }

    public Object getValueFrom(Object source) throws LocalizedException {
        xmlBeanMapper = schemaTypeMap.get(schemaTypeQName);
        List values;
        try {
            values = (List) propertyDescriptor.getReadMethod().invoke(source);
        } catch (IllegalAccessException ex) {
            throw new IllegalPropertyAccessException(propertyDescriptor.getName(), source.getClass().getName(), ex);
        } catch (InvocationTargetException ex) {
            throw new InvocationTargetPropertyAccessor(propertyDescriptor.getName(), source.getClass().getName(), ex);
        }

        List newValues = new ArrayList(values.size());
        for (Object value : values) {
            newValues.add(xmlBeanMapper.typeToValue(value));
        }

        return newValues;
    }

    public Object createProperty(Object value) throws LocalizedException {
        xmlBeanMapper = schemaTypeMap.get(schemaTypeQName);
        if (value == null) {
            return null;
        }
        List values;
        if (value instanceof List) {
            values = (List) value;
        } else {
            values = Arrays.asList(value);
        }

        List property = new ArrayList(values.size());
        for (Object val : values) {
            property.add(xmlBeanMapper.convertToType(val));
        }

        return property;
    }

    public Object createValue(Object property) throws LocalizedException {
        xmlBeanMapper = schemaTypeMap.get(schemaTypeQName);
        if (property == null) {
            return null;
        }
        if (!(property instanceof List)) {
            if (!(property instanceof Object[])) {// treat Object[] as List ,bug for 9900 (wrap webservice)
                throw new IllegalArgumentException("You must provide a list of properties");
            }// end
        }
        List<Object> properties = null;
        if (property instanceof Object[]) {// treat Object[] as List ,bug for 9900 (wrap webservice)
            Object[] os = (Object[]) property;
            properties = Arrays.asList(os);
        } else {// end
            properties = (List<Object>) property;
        }
        List<Object> values = new ArrayList<Object>(properties.size());

        for (Object prop : properties) {
            values.add(xmlBeanMapper.typeToValue(prop));
        }

        return values;
    }
}
