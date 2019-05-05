package org.talend.webservice.mapper;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * 
 * @author rlamarche
 */
public interface ClassMapper {

    public Class<?> getClassForType(QName xmlSchemaTypeMapperQName, List<String> propertiesName, int tempSuffix);

    public Class<?> getClassForType(QName xmlSchemaTypeMapperQName);

    public XmlSchemaType getTypeForClass(Class<?> clazz);
}
