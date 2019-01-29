package org.talend.webservice.mapper;

import java.util.Map;

import javax.xml.namespace.QName;

import org.talend.webservice.exception.LocalizedException;

public class SimpleContentPropertyMapper extends SimplePropertyMapper {

    public SimpleContentPropertyMapper(Class<?> clazz, QName typeQName, String propertyName, Map<QName, TypeMapper> schemaTypeMap)
            throws LocalizedException {
        super(clazz, typeQName, propertyName, schemaTypeMap);
    }

}
