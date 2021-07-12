/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.webservice.mapper;

import org.talend.webservice.exception.LocalizedException;

import javax.xml.namespace.QName;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author rlamarche
 */
public class ComplexTypeMapper implements TypeMapper {

    private Map<String, PropertyMapper> mappers;

    private Class<?> clazz;

    private List<String> propertiesOrder;

    private Map<QName, ComplexTypeMapper> instanceComplexTypeMapper;

    private boolean hasInstance = false;

    private QName typeName = null;

    private final String ABSTRACT_TYPE_NAME = "@type";

    public boolean hasInstance() {
        return hasInstance;
    }

    protected ComplexTypeMapper(Map<String, PropertyMapper> mappers, Class<?> clazz, List<String> propertiesOrder, QName typeName) {
        this.mappers = mappers;
        this.clazz = clazz;
        this.propertiesOrder = propertiesOrder;
        this.typeName = typeName;
    }

    public void setInstanceComplexTypeMapper(Map<QName, ComplexTypeMapper> instanceComplexTypeMapper) {
        if (instanceComplexTypeMapper != null) {
            this.instanceComplexTypeMapper = instanceComplexTypeMapper;
            hasInstance = true;
        }
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object convertToType(Object value) throws LocalizedException {
        if (value == null) {
            return null;
        }
        if (hasInstance) {
            if (value instanceof Map) {
                Map<String, Object> values = (Map<String, Object>) value;
                if (values.containsKey(ABSTRACT_TYPE_NAME)) {
                    QName type = (QName) values.get(ABSTRACT_TYPE_NAME);
                    ComplexTypeMapper instanceComplexTypeMapper = findInstanceByQName(type);
                    if (instanceComplexTypeMapper != null) {
                        return instanceComplexTypeMapper.convertToType(values.get(instanceComplexTypeMapper.typeName
                                .getLocalPart()));
                    }
                }
            }
        }
        Object bean = null;
        try {
            bean = clazz.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("Unable to instantiate bean of type " + clazz.getName(), ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Unable to instantiate bean of type " + clazz.getName(), ex);
        }

        if (!(value instanceof Map)) {
            if (mappers.get("value") != null && mappers.get("value") instanceof SimpleContentPropertyMapper) {
                PropertyMapper propertyMapper = mappers.get("value");

                if (propertyMapper != null) {
                    propertyMapper.setValueTo(bean, value);
                } else {
                    // TODO log a warning ?
                }
            } else {
                throw new IllegalArgumentException("You must provide a Map to create a complexType.");
            }
        } else {
            Map<String, Object> values = (Map<String, Object>) value;

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                PropertyMapper propertyMapper = mappers.get(entry.getKey());

                if (propertyMapper != null) {
                    propertyMapper.setValueTo(bean, entry.getValue());
                } else {
                    // TODO log a warning ?
                }
            }
        }

        return bean;
    }

    public ComplexTypeMapper findInstanceByQName(QName typeQName) {
        ComplexTypeMapper complexTypeMapper = instanceComplexTypeMapper.get(typeQName);
        if (complexTypeMapper != null) {
            return complexTypeMapper;
        } else {
            for (ComplexTypeMapper childComplexTypeMapper : instanceComplexTypeMapper.values()) {
                if (childComplexTypeMapper.hasInstance()) {
                    ComplexTypeMapper iComplexTypeMapper = childComplexTypeMapper.findInstanceByQName(typeQName);
                    if (iComplexTypeMapper != null) {
                        return iComplexTypeMapper;
                    }
                }
            }
        }
        return null;
    }

    public ComplexTypeMapper findInstanceByClassName(String className) {
        for (ComplexTypeMapper complexTypeMapper : instanceComplexTypeMapper.values()) {
            if (complexTypeMapper.getClazz().getName().equals(className)) {
                return complexTypeMapper;
            } else if (complexTypeMapper.hasInstance()) {
                ComplexTypeMapper iComplexTypeMapper = complexTypeMapper.findInstanceByClassName(className);
                if (iComplexTypeMapper != null) {
                    return iComplexTypeMapper;
                }
            }
        }
        return null;
    }

    public Object typeToValue(Object bean) throws LocalizedException {
        if (bean == null) {
            return null;
        } else {
            if (!clazz.isInstance(bean)) {
                throw new IllegalArgumentException("You must provide an object of type specified by property clazz.");
            }
            if (hasInstance) {
                String beanName = bean.getClass().getName();
                if (!clazz.getName().equals(beanName)) {
                    ComplexTypeMapper instanceComplexTypeMapper = findInstanceByClassName(beanName);
                    if (instanceComplexTypeMapper != null) {
                        Map<String, Object> values = new LinkedHashMap<String, Object>();
                        values.put(ABSTRACT_TYPE_NAME, instanceComplexTypeMapper.typeName);
                        values.put(instanceComplexTypeMapper.typeName.getLocalPart(), instanceComplexTypeMapper.typeToValue(bean));
                        return values;
                    }
                }
            }

            if (mappers.get("value") != null && mappers.get("value") instanceof SimpleContentPropertyMapper) {
                PropertyMapper propertyMapper = mappers.get("value");
                if (propertyMapper != null) {
                    return propertyMapper.getValueFrom(bean);
                } else {
                    return null;
                }
            } else {
                Map<String, Object> values = new LinkedHashMap<String, Object>(mappers.size());
                for (Map.Entry<String, PropertyMapper> entry : mappers.entrySet()) {
                    Object value = entry.getValue().getValueFrom(bean);
                    if (value != null) {
                        values.put(entry.getKey(), value);
                    }
                }
                return values;
            }
        }
    }

    public Object[] convertToTypeUnwrapped(Map<String, Object> values) throws LocalizedException {
        Object[] objects = new Object[propertiesOrder.size()];
        int i = 0;
        for (String property : propertiesOrder) {
            Object value = values.get(property);
            if (value != null) {
                PropertyMapper propertyMapper = mappers.get(property);
                objects[i] = propertyMapper.createProperty(value);
            }

            i++;
        }
        return objects;
    }

    public Map<String, Object> typeToValueUnwrapped(Object[] params) throws LocalizedException {
        if (params == null) {
            return null;
        }
        Map<String, Object> values = new LinkedHashMap<String, Object>(mappers.size());

        int i = 0;
        for (Object param : params) {
            if (i >= propertiesOrder.size()) {
                throw new IllegalArgumentException("Too much params.");
            }
            String property = propertiesOrder.get(i);
            PropertyMapper propertyMapper = mappers.get(property);
            values.put(property, propertyMapper.createValue(param));
            i++;
        }

        return values;
    }
}
