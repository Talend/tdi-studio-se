package org.talend.webservice.mapper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.beanutils.PropertyUtils;
import org.talend.webservice.exception.IllegalPropertyAccessException;
import org.talend.webservice.exception.InvocationTargetPropertyAccessor;
import org.talend.webservice.exception.LocalizedException;

/**
 * 
 * @author rlamarche
 */
public class SimplePropertyMapper implements PropertyMapper {

    private Map<QName, TypeMapper> schemaTypeMap;

    private TypeMapper xmlBeanMapper;

    private String propertyName;

    private PropertyDescriptor propertyDescriptor;

    private QName schemaTypeQName;

    public SimplePropertyMapper(Class<?> clazz, QName typeQName, String propertyName, Map<QName, TypeMapper> schemaTypeMap)
            throws LocalizedException {
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

    public String getMappedPropertyName() {
        return propertyName;
    }

    public void setValueTo(Object destination, Object value) throws LocalizedException {
        xmlBeanMapper = schemaTypeMap.get(schemaTypeQName);
        try {
            Method method = propertyDescriptor.getWriteMethod();
            if (method == null) {
                method = propertyDescriptor.getReadMethod();
                if (method != null) {
                    Object returnValue = method.invoke(destination);
                    if (returnValue instanceof List) {
                        List values = (List) returnValue;
                        Object tempValue = xmlBeanMapper.convertToType(value);
                        if (tempValue != null) {
                            values.addAll((List) tempValue);
                        }
                    }
                }
            } else {
                if (method.getParameterTypes()[0].equals(JAXBElement.class)) {
                    value = new JAXBElement(new QName(getMappedPropertyName()), getMappedClass(),
                            xmlBeanMapper.convertToType(value));
                    propertyDescriptor.getWriteMethod().invoke(destination, value);
                } else {
                    propertyDescriptor.getWriteMethod().invoke(destination, xmlBeanMapper.convertToType(value));
                }
            }
        } catch (IllegalAccessException ex) {
            throw new IllegalPropertyAccessException(propertyDescriptor.getName(), destination.getClass().getName(), ex);
        } catch (InvocationTargetException ex) {
            throw new InvocationTargetPropertyAccessor(propertyDescriptor.getName(), destination.getClass().getName(),
                    ex.getTargetException());
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
    }

    public Object getValueFrom(Object source) throws LocalizedException {
        xmlBeanMapper = schemaTypeMap.get(schemaTypeQName);
        try {
            Method method = propertyDescriptor.getReadMethod();
            if (method.getReturnType().equals(JAXBElement.class)) {
                JAXBElement jAXBElement = (JAXBElement) method.invoke(source);
                return xmlBeanMapper.typeToValue(jAXBElement != null ? jAXBElement.getValue() : null);
            } else {
                return xmlBeanMapper.typeToValue(propertyDescriptor.getReadMethod().invoke(source));
            }
        } catch (IllegalAccessException ex) {
            throw new IllegalPropertyAccessException(propertyDescriptor.getName(), source.getClass().getName(), ex);
        } catch (InvocationTargetException ex) {
            throw new InvocationTargetPropertyAccessor(propertyDescriptor.getName(), source.getClass().getName(),
                    ex.getTargetException());
        }
    }

    public Class<?> getMappedClass() {
        return schemaTypeMap.get(schemaTypeQName).getClazz();
    }

    public Object createProperty(Object value) throws LocalizedException {
        xmlBeanMapper = schemaTypeMap.get(schemaTypeQName);
        return xmlBeanMapper.convertToType(value);
    }

    public Object createValue(Object property) throws LocalizedException {
        xmlBeanMapper = schemaTypeMap.get(schemaTypeQName);
        return xmlBeanMapper.typeToValue(property);
    }
}
