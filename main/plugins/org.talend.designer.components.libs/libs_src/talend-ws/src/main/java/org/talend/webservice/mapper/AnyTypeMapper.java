/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.webservice.mapper;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaType;
import org.talend.webservice.exception.LocalizedException;

/**
 * 
 * @author rlamarche
 */
public class AnyTypeMapper implements TypeMapper {

    public static final String ANYTYPE_VALUE = "anyType";

    public static final String ANYTYPE_TYPE = "anyType_type";
    
    public static final String ANYTYPE_PREFIX = ":anyType";

    private MapperFactory mapperFactory;

    private Map<QName, TypeMapper> mappers = new HashMap<QName, TypeMapper>();

    private TypeMapper lastTypeMapper = null;

    public AnyTypeMapper(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }

    public Class<?> getClazz() {
        if (lastTypeMapper != null) {
            return lastTypeMapper.getClazz();
        } else {
            return Object.class;
        }
    }

    public Object convertToType(Object value) throws LocalizedException {
        if (value == null) {
            return null;
        }
        if (!(value instanceof Map)) {
            throw new LocalizedException("org.talend.ws.exception.InvalidParameterAnyType");
        }
        Map<String, Object> map = (Map<String, Object>) value;

        value = map.get(ANYTYPE_VALUE);
        if (value == null) {
            throw new LocalizedException("org.talend.ws.exception.InvalidParameterAnyType");
        }

        Object typeValue = map.get(ANYTYPE_TYPE);
        if (typeValue == null || !(typeValue instanceof QName)) {
            throw new LocalizedException("org.talend.ws.exception.InvalidParameterAnyType");
        }

        QName type = (QName) typeValue;
        lastTypeMapper = getTypeMapper(type);
        if (lastTypeMapper == null) {
            throw new IllegalArgumentException("Type " + type + " is invalid.");
        }
        return lastTypeMapper.convertToType(value);
    }

    public Object typeToValue(Object bean) throws LocalizedException {
        if (bean == null) {
            return null;
        }
        XmlSchemaType type = mapperFactory.getClassMapper().getTypeForClass(bean.getClass());

        lastTypeMapper = getTypeMapper(type.getQName());
        if (lastTypeMapper == null) {
            throw new IllegalArgumentException("Class " + bean.getClass().getName() + " is not mapped.");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ANYTYPE_TYPE, type.getQName());
        map.put(ANYTYPE_VALUE, lastTypeMapper.typeToValue(bean));

        return map;
    }

    private TypeMapper getTypeMapper(QName type) throws LocalizedException {
        TypeMapper typeMapper = mappers.get(type);
        if (typeMapper == null) {
            XmlSchemaType xmlSchemaType = mapperFactory.getSchemaCollection().getTypeByQName(type);
            if (xmlSchemaType == null) {
                throw new IllegalArgumentException("Unsupported schema type : " + type);
            }

            typeMapper = mapperFactory.createTypeMapper(xmlSchemaType);

            mappers.put(type, typeMapper);
        }

        return typeMapper;
    }
}
